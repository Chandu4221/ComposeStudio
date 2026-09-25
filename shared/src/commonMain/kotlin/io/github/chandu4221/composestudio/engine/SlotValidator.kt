package io.github.chandu4221.composestudio.engine

import io.github.chandu4221.composestudio.data.ComponentCatalog
import io.github.chandu4221.composestudio.data.ComponentDefinition
import io.github.chandu4221.composestudio.state.ComponentNode

/**
 * Slot validation and auto-routing engine.
 * Enforces strict parent-child structural rules and intelligently auto-routes
 * dropped components to the correct slot (e.g., TopAppBar -> topBar in Scaffold).
 */
object SlotValidator {

    /**
     * Resolves the target slot name for dropping [draggedComponent] into [parentNode].
     * Returns null if the drop is invalid or strictly rejected.
     */
    fun resolveDrop(
        parentNode: ComponentNode,
        draggedComponent: ComponentDefinition,
        catalog: ComponentCatalog,
        targetSlotName: String? = null
    ): String? {
        val parentDef = catalog.components.find { it.id == parentNode.catalogId } ?: return null
        val availableSlots = parentDef.slots.filter { it.isComposable }
        if (availableSlots.isEmpty()) {
            // Leaf components (e.g. Text, Icon, Divider) cannot contain children
            return null
        }

        val availableSlotNames = availableSlots.map { it.name }

        // If target slot is explicitly requested, validate compatibility
        if (targetSlotName != null) {
            return if (targetSlotName in availableSlotNames &&
                isSlotCompatible(parentDef.id, targetSlotName, draggedComponent.id, draggedComponent.category)
            ) {
                targetSlotName
            } else {
                null
            }
        }

        // Auto-route logic for Scaffold
        if (parentDef.id == "Scaffold") {
            return when {
                draggedComponent.id.contains("TopAppBar") && "topBar" in availableSlotNames -> "topBar"
                (draggedComponent.id.contains("NavigationBar") || draggedComponent.id.contains("BottomAppBar")) && "bottomBar" in availableSlotNames -> "bottomBar"
                draggedComponent.id.contains("FloatingActionButton") && "floatingActionButton" in availableSlotNames -> "floatingActionButton"
                draggedComponent.id.contains("Snackbar") && "snackbarHost" in availableSlotNames -> "snackbarHost"
                "content" in availableSlotNames -> "content"
                else -> null
            }
        }

        // Priority 1: Match component type directly to slot name
        val directMatch = availableSlotNames.firstOrNull { slotName ->
            slotName.equals(draggedComponent.id, ignoreCase = true) ||
            draggedComponent.id.startsWith(slotName, ignoreCase = true) ||
            slotName.startsWith(draggedComponent.id.replace("Bar", ""), ignoreCase = true)
        }
        if (directMatch != null && isSlotCompatible(parentDef.id, directMatch, draggedComponent.id, draggedComponent.category)) {
            return directMatch
        }

        // Priority 2: Standard "content" container slot
        if ("content" in availableSlotNames && isSlotCompatible(parentDef.id, "content", draggedComponent.id, draggedComponent.category)) {
            return "content"
        }

        // Priority 3: First compatible slot
        return availableSlotNames.firstOrNull { slotName ->
            isSlotCompatible(parentDef.id, slotName, draggedComponent.id, draggedComponent.category)
        }
    }

    /**
     * Checks if a component can be placed inside a specific slot of a parent component.
     */
    fun isSlotCompatible(
        parentId: String,
        slotName: String,
        childId: String,
        childCategory: String
    ): Boolean {
        // Disallow dropping a Scaffold inside any other component
        if (childId == "Scaffold") return false

        return when (parentId) {
            "Scaffold" -> when (slotName) {
                "topBar" -> childId.contains("TopAppBar") || childId.contains("AppBar")
                "bottomBar" -> childId.contains("NavigationBar") || childId.contains("BottomAppBar")
                "floatingActionButton" -> childId.contains("FloatingActionButton")
                "snackbarHost" -> childId.contains("Snackbar")
                "content" -> !childId.contains("TopAppBar") &&
                             !childId.contains("BottomAppBar") &&
                             !childId.contains("FloatingActionButton")
                else -> true
            }

            "CenterAlignedTopAppBar", "TopAppBar" -> when (slotName) {
                "title" -> childId == "Text"
                "navigationIcon", "actions" -> childId in setOf("IconButton", "Icon", "TextButton")
                else -> true
            }

            "NavigationBar" -> slotName == "content" && childId in setOf("NavigationBarItem", "NavigationRailItem")

            "NavigationRail" -> when (slotName) {
                "header" -> childId in setOf("FloatingActionButton", "IconButton", "Icon")
                "content" -> childId in setOf("NavigationRailItem", "NavigationBarItem")
                else -> true
            }

            "Button", "ElevatedButton", "FilledTonalButton", "OutlinedButton", "TextButton" -> {
                slotName == "content" && childId in setOf("Text", "Icon", "Row", "CircularProgressIndicator")
            }

            "IconButton" -> slotName == "content" && childId in setOf("Icon", "Text")

            "Card", "ElevatedCard", "OutlinedCard", "Surface" -> {
                slotName == "content"
            }

            "Column", "Row", "Box", "LazyColumn", "LazyRow" -> {
                slotName == "content"
            }

            else -> true
        }
    }
}
