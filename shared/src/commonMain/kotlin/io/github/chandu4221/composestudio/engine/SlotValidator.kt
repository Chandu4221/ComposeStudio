package io.github.chandu4221.composestudio.engine

import io.github.chandu4221.composestudio.data.AtomicCategory
import io.github.chandu4221.composestudio.data.ComponentCatalog
import io.github.chandu4221.composestudio.data.ComponentDefinition
import io.github.chandu4221.composestudio.data.SlotCardinality
import io.github.chandu4221.composestudio.data.SlotDefinition
import io.github.chandu4221.composestudio.state.ComponentNode

/**
 * Result of validating a drop action into a specific slot.
 */
sealed interface SlotValidationResult {

    /**
     * The drop is valid and permitted into [slotName].
     * [scopeReceiver] is the Jetpack Compose scope provided by this slot (e.g. "RowScope", "ColumnScope").
     */
    data class Valid(
        val slotName: String,
        val scopeReceiver: String? = null
    ) : SlotValidationResult

    /**
     * The drop was rejected due to structural or typing constraints.
     */
    sealed interface Rejected : SlotValidationResult {
        val reason: String

        /**
         * The requested slot does not exist on the parent component.
         */
        data class SlotNotFound(val slotName: String) : Rejected {
            override val reason: String = "Slot '$slotName' does not exist on target parent."
        }

        /**
         * The component's [AtomicCategory] is not allowed in this slot.
         */
        data class CategoryNotAllowed(
            val slotName: String,
            val attemptedCategory: AtomicCategory,
            val allowedCategories: List<AtomicCategory>
        ) : Rejected {
            override val reason: String =
                "Category $attemptedCategory is not permitted in slot '$slotName'. Allowed: ${allowedCategories.joinToString()}."
        }

        /**
         * The component ID is not in the explicit whitelist for this slot.
         */
        data class CatalogIdNotAllowed(
            val slotName: String,
            val attemptedCatalogId: String,
            val allowedCatalogIds: List<String>
        ) : Rejected {
            override val reason: String =
                "Component '$attemptedCatalogId' is not allowed in slot '$slotName'. Allowed: ${allowedCatalogIds.joinToString()}."
        }

        /**
         * The slot has single cardinality and is already occupied.
         */
        data class SlotFull(
            val slotName: String,
            val currentChildCount: Int
        ) : Rejected {
            override val reason: String =
                "Slot '$slotName' only accepts a single component and is already occupied (children: $currentChildCount)."
        }

        /**
         * Target component is a leaf element with no composable slots (e.g. Text, Icon, Divider).
         */
        data class LeafComponentNoSlots(
            val parentCatalogId: String
        ) : Rejected {
            override val reason: String =
                "Component '$parentCatalogId' is a leaf node and cannot accept children."
        }

        /**
         * Screen-level TEMPLATE components (Scaffold, Dialogs) cannot be nested inside another component.
         */
        data class CannotNestTemplate(
            val templateId: String
        ) : Rejected {
            override val reason: String =
                "Screen-level template '$templateId' cannot be nested inside another component."
        }

        /**
         * Parent component was not found in catalog.
         */
        data class ComponentNotFoundInCatalog(
            val componentId: String
        ) : Rejected {
            override val reason: String =
                "Component '$componentId' was not found in catalog."
        }
    }
}

/**
 * Pure, functional validation and auto-routing engine for Jetpack Compose component slots.
 * Enforces strict Atomic Composables hierarchy, cardinality (SINGLE vs MULTIPLE),
 * whitelists, and scope receivers.
 */
object SlotValidator {

    /**
     * Pure validation function checking whether [draggedComponent] can be dropped
     * into [targetSlotName] of [parentNode].
     */
    fun validateDrop(
        parentNode: ComponentNode,
        targetSlotName: String,
        draggedComponent: ComponentDefinition,
        catalog: ComponentCatalog
    ): SlotValidationResult {
        // Rule 1: Templates (Scaffold, Dialogs, etc.) cannot be nested inside any component slot
        if (draggedComponent.atomicCategory == AtomicCategory.TEMPLATE) {
            return SlotValidationResult.Rejected.CannotNestTemplate(draggedComponent.id)
        }

        // Rule 2: Lookup parent component definition
        val parentDef = catalog.components.find { it.id == parentNode.catalogId }
            ?: return SlotValidationResult.Rejected.ComponentNotFoundInCatalog(parentNode.catalogId)

        // Rule 3: Check if parent is a leaf component
        if (parentDef.slotDefinitions.isEmpty()) {
            return SlotValidationResult.Rejected.LeafComponentNoSlots(parentNode.catalogId)
        }

        // Rule 4: Slot existence
        val slotDef = parentDef.findSlot(targetSlotName)
            ?: return SlotValidationResult.Rejected.SlotNotFound(targetSlotName)

        // Rule 5: Atomic Category hierarchy check
        if (draggedComponent.atomicCategory !in slotDef.allowedCategories) {
            return SlotValidationResult.Rejected.CategoryNotAllowed(
                slotName = targetSlotName,
                attemptedCategory = draggedComponent.atomicCategory,
                allowedCategories = slotDef.allowedCategories
            )
        }

        // Rule 6: Whitelist check (if explicit whitelist defined)
        if (slotDef.allowedCatalogIds != null && draggedComponent.id !in slotDef.allowedCatalogIds) {
            return SlotValidationResult.Rejected.CatalogIdNotAllowed(
                slotName = targetSlotName,
                attemptedCatalogId = draggedComponent.id,
                allowedCatalogIds = slotDef.allowedCatalogIds
            )
        }

        // Rule 7: Cardinality check (SINGLE vs MULTIPLE)
        if (slotDef.cardinality == SlotCardinality.SINGLE) {
            val existingChildren = parentNode.slots[targetSlotName].orEmpty()
            if (existingChildren.isNotEmpty()) {
                return SlotValidationResult.Rejected.SlotFull(
                    slotName = targetSlotName,
                    currentChildCount = existingChildren.size
                )
            }
        }

        // All constraints satisfied
        return SlotValidationResult.Valid(
            slotName = targetSlotName,
            scopeReceiver = slotDef.scopeReceiver
        )
    }

    /**
     * Resolves the target slot name for dropping [draggedComponent] into [parentNode].
     * If [targetSlotName] is explicitly supplied, validates it directly.
     * If null, executes auto-routing through priority candidate slots.
     * Returns the slot name if valid, or null if strictly rejected.
     */
    fun resolveDrop(
        parentNode: ComponentNode,
        draggedComponent: ComponentDefinition,
        catalog: ComponentCatalog,
        targetSlotName: String? = null
    ): String? {
        if (targetSlotName != null) {
            val result = validateDrop(parentNode, targetSlotName, draggedComponent, catalog)
            return if (result is SlotValidationResult.Valid) result.slotName else null
        }

        val parentDef = catalog.components.find { it.id == parentNode.catalogId } ?: return null
        if (parentDef.slotDefinitions.isEmpty()) return null

        // Scaffold auto-routing priority
        val candidateSlots: List<SlotDefinition> = if (parentDef.id == "Scaffold") {
            when {
                draggedComponent.id.contains("TopAppBar") -> listOfNotNull(parentDef.findSlot("topBar"))
                draggedComponent.id.contains("NavigationBar") || draggedComponent.id.contains("BottomAppBar") ->
                    listOfNotNull(parentDef.findSlot("bottomBar"))
                draggedComponent.id.contains("FloatingActionButton") ->
                    listOfNotNull(parentDef.findSlot("floatingActionButton"))
                draggedComponent.id.contains("Snackbar") ->
                    listOfNotNull(parentDef.findSlot("snackbarHost"))
                else -> emptyList()
            } + parentDef.slotDefinitions
        } else {
            val directMatches = parentDef.slotDefinitions.filter { slot ->
                slot.name.equals(draggedComponent.id, ignoreCase = true) ||
                draggedComponent.id.startsWith(slot.name, ignoreCase = true)
            }
            val contentSlot = parentDef.slotDefinitions.filter { it.name == "content" }
            directMatches + contentSlot + parentDef.slotDefinitions
        }

        for (candidate in candidateSlots.distinctBy { it.name }) {
            val result = validateDrop(parentNode, candidate.name, draggedComponent, catalog)
            if (result is SlotValidationResult.Valid) {
                return result.slotName
            }
        }

        return null
    }

    /**
     * Convenience helper to check if a component is compatible with a slot.
     */
    fun isSlotCompatible(
        parentId: String,
        slotName: String,
        childId: String,
        childCategory: String,
        catalog: ComponentCatalog? = null
    ): Boolean {
        if (childId == "Scaffold") return false
        val cat = catalog ?: ComponentCatalog(
            components = listOf(
                ComponentDefinition(id = parentId, displayName = parentId, category = "LAYOUT"),
                ComponentDefinition(id = childId, displayName = childId, category = childCategory)
            )
        )
        val parentNode = ComponentNode(catalogId = parentId)
        val childDef = cat.components.find { it.id == childId }
            ?: ComponentDefinition(id = childId, displayName = childId, category = childCategory)

        val result = validateDrop(parentNode, slotName, childDef, cat)
        return result is SlotValidationResult.Valid
    }
}
