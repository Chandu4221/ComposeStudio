package io.github.chandu4221.composestudio.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.ViewSidebar
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.SmartButton
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

@Serializable
enum class AtomicCategory {
    ATOM,
    MOLECULE,
    ORGANISM,
    TEMPLATE
}

@Serializable
enum class SlotCardinality {
    SINGLE,
    MULTIPLE
}

@Serializable
data class SlotDefinition(
    val name: String,
    val allowedCategories: List<AtomicCategory> = listOf(AtomicCategory.ATOM, AtomicCategory.MOLECULE, AtomicCategory.ORGANISM),
    val allowedCatalogIds: List<String>? = null,
    val scopeReceiver: String? = null,
    val cardinality: SlotCardinality = SlotCardinality.MULTIPLE
)

@Serializable
data class ComponentCatalog(
    val composeMultiplatformVersion: String? = null,
    val material3Version: String? = null,
    val totalCount: Int = 0,
    val components: List<ComponentDefinition> = emptyList()
)

@Serializable
data class ComponentDefinition(
    val id: String,
    val displayName: String,
    val packageName: String? = null,
    val category: String,
    val atomicCategory: AtomicCategory = resolveAtomicCategory(id, category),
    val tier: String? = null,
    val isExperimental: Boolean = false,
    val experimentalAnnotations: List<String> = emptyList(),
    val isDeprecated: Boolean = false,
    val deprecation: String? = null,
    val receiverScope: String? = null,
    val parameters: List<ComponentParameter> = emptyList(),
    val callbacks: List<ComponentCallback> = emptyList(),
    val slots: List<ComponentSlot> = emptyList(),
    val slotDefinitions: List<SlotDefinition> = resolveSlotDefinitions(id, slots)
) {
    /**
     * Finds a strongly-typed [SlotDefinition] by name.
     */
    fun findSlot(name: String): SlotDefinition? {
        return slotDefinitions.find { it.name == name }
            ?: slots.find { it.name == name }?.let { slot ->
                SlotDefinition(
                    name = slot.name,
                    scopeReceiver = slot.receiverScope,
                    cardinality = if (slot.cardinality == "SINGLE") SlotCardinality.SINGLE else SlotCardinality.MULTIPLE
                )
            }
    }

    /**
     * Resolves the most appropriate Material icon for the component.
     */
    fun resolveIcon(): ImageVector {
        return when {
            id.contains("Button") && !id.contains("Icon") -> Icons.Default.SmartButton
            id.contains("IconButton") -> Icons.Default.RadioButtonChecked
            id.contains("FloatingActionButton") -> Icons.Default.AddCircle
            id.contains("Chip") -> Icons.AutoMirrored.Filled.Label
            id.contains("Card") -> Icons.Default.CreditCard
            id.contains("Box") || id == "Surface" -> Icons.Default.CropSquare
            id.contains("Column") || id.contains("Row") -> Icons.Default.ViewAgenda
            id.contains("TopAppBar") -> Icons.Default.Menu
            id.contains("NavigationBar") || id.contains("NavigationRail") -> Icons.Default.Navigation
            id.contains("Drawer") -> Icons.AutoMirrored.Filled.ViewSidebar
            id.contains("Tab") -> Icons.Default.Tab
            id.contains("Checkbox") || id.contains("Switch") -> Icons.Default.ToggleOn
            id.contains("Slider") -> Icons.Default.LinearScale
            id.contains("TextField") -> Icons.Default.Edit
            id.contains("Badge") -> Icons.Default.Notifications
            id == "Text" -> Icons.Default.TextFields
            id == "Icon" -> Icons.Default.Image
            id.contains("ProgressIndicator") -> Icons.Default.HourglassEmpty
            id.contains("Divider") -> Icons.Default.HorizontalRule
            category == "INPUT" -> Icons.Default.SmartButton
            category == "SURFACE" -> Icons.Default.CreditCard
            category == "LAYOUT" -> Icons.Default.CropSquare
            category == "NAVIGATION" -> Icons.Default.Navigation
            else -> Icons.Default.Widgets
        }
    }
}

/**
 * Maps component ID and category to its strict [AtomicCategory].
 */
fun resolveAtomicCategory(id: String, category: String): AtomicCategory {
    return when {
        // TEMPLATES: Screen-level scaffolds & dialogs
        id in setOf(
            "Scaffold", "BottomSheetScaffold",
            "ModalNavigationDrawer", "PermanentNavigationDrawer", "DismissibleNavigationDrawer",
            "AlertDialog", "BasicAlertDialog"
        ) -> AtomicCategory.TEMPLATE

        // ORGANISMS: Complex composite sections & layouts
        id.contains("TopAppBar") || id in setOf(
            "NavigationBar", "NavigationRail", "BottomAppBar",
            "Card", "ElevatedCard", "OutlinedCard",
            "Row", "Column", "Box", "LazyColumn", "LazyRow", "Surface"
        ) -> AtomicCategory.ORGANISM

        // MOLECULES: Combinations of atoms (composite inputs, list rows, chips, search)
        id.contains("Chip") || id.contains("TextField") || id in setOf(
            "ListItem", "SearchBar", "DockedSearchBar",
            "NavigationBarItem", "NavigationRailItem", "Tab", "LeadingIconTab"
        ) -> AtomicCategory.MOLECULE

        // ATOMS: Base UI elements & simple controls
        else -> AtomicCategory.ATOM
    }
}

/**
 * Builds strongly-typed [SlotDefinition]s for a component, defining explicit
 * categories, whitelists, cardinality, and scope receivers per slot.
 */
fun resolveSlotDefinitions(id: String, existingSlots: List<ComponentSlot>): List<SlotDefinition> {
    return when (id) {
        "Scaffold" -> listOf(
            SlotDefinition(
                name = "topBar",
                allowedCategories = listOf(AtomicCategory.ORGANISM),
                allowedCatalogIds = listOf("TopAppBar", "CenterAlignedTopAppBar", "MediumTopAppBar", "LargeTopAppBar"),
                cardinality = SlotCardinality.SINGLE
            ),
            SlotDefinition(
                name = "bottomBar",
                allowedCategories = listOf(AtomicCategory.ORGANISM),
                allowedCatalogIds = listOf("NavigationBar", "BottomAppBar"),
                cardinality = SlotCardinality.SINGLE
            ),
            SlotDefinition(
                name = "floatingActionButton",
                allowedCategories = listOf(AtomicCategory.ATOM),
                allowedCatalogIds = listOf("FloatingActionButton", "SmallFloatingActionButton", "LargeFloatingActionButton", "ExtendedFloatingActionButton"),
                cardinality = SlotCardinality.SINGLE
            ),
            SlotDefinition(
                name = "snackbarHost",
                allowedCategories = listOf(AtomicCategory.ATOM, AtomicCategory.MOLECULE),
                allowedCatalogIds = listOf("SnackbarHost", "Snackbar"),
                cardinality = SlotCardinality.SINGLE
            ),
            SlotDefinition(
                name = "content",
                allowedCategories = listOf(AtomicCategory.ATOM, AtomicCategory.MOLECULE, AtomicCategory.ORGANISM),
                cardinality = SlotCardinality.MULTIPLE
            )
        )

        "CenterAlignedTopAppBar", "TopAppBar", "MediumTopAppBar", "LargeTopAppBar" -> listOf(
            SlotDefinition(
                name = "title",
                allowedCategories = listOf(AtomicCategory.ATOM),
                allowedCatalogIds = listOf("Text"),
                cardinality = SlotCardinality.SINGLE
            ),
            SlotDefinition(
                name = "navigationIcon",
                allowedCategories = listOf(AtomicCategory.ATOM),
                allowedCatalogIds = listOf("IconButton", "Icon"),
                cardinality = SlotCardinality.SINGLE
            ),
            SlotDefinition(
                name = "actions",
                allowedCategories = listOf(AtomicCategory.ATOM),
                allowedCatalogIds = listOf("IconButton", "Icon", "TextButton"),
                scopeReceiver = "RowScope",
                cardinality = SlotCardinality.MULTIPLE
            )
        )

        "NavigationBar" -> listOf(
            SlotDefinition(
                name = "content",
                allowedCategories = listOf(AtomicCategory.MOLECULE),
                allowedCatalogIds = listOf("NavigationBarItem"),
                scopeReceiver = "RowScope",
                cardinality = SlotCardinality.MULTIPLE
            )
        )

        "NavigationRail" -> listOf(
            SlotDefinition(
                name = "header",
                allowedCategories = listOf(AtomicCategory.ATOM),
                allowedCatalogIds = listOf("FloatingActionButton", "IconButton", "Icon"),
                cardinality = SlotCardinality.SINGLE
            ),
            SlotDefinition(
                name = "content",
                allowedCategories = listOf(AtomicCategory.MOLECULE),
                allowedCatalogIds = listOf("NavigationRailItem"),
                scopeReceiver = "ColumnScope",
                cardinality = SlotCardinality.MULTIPLE
            )
        )

        "Button", "ElevatedButton", "FilledTonalButton", "OutlinedButton", "TextButton" -> listOf(
            SlotDefinition(
                name = "content",
                allowedCategories = listOf(AtomicCategory.ATOM),
                allowedCatalogIds = listOf("Text", "Icon", "CircularProgressIndicator"),
                scopeReceiver = "RowScope",
                cardinality = SlotCardinality.MULTIPLE
            )
        )

        "IconButton", "FilledIconButton", "FilledTonalIconButton", "OutlinedIconButton" -> listOf(
            SlotDefinition(
                name = "content",
                allowedCategories = listOf(AtomicCategory.ATOM),
                allowedCatalogIds = listOf("Icon", "Text"),
                cardinality = SlotCardinality.SINGLE
            )
        )

        "Column", "LazyColumn" -> listOf(
            SlotDefinition(
                name = "content",
                allowedCategories = listOf(AtomicCategory.ATOM, AtomicCategory.MOLECULE, AtomicCategory.ORGANISM),
                scopeReceiver = "ColumnScope",
                cardinality = SlotCardinality.MULTIPLE
            )
        )

        "Row", "LazyRow" -> listOf(
            SlotDefinition(
                name = "content",
                allowedCategories = listOf(AtomicCategory.ATOM, AtomicCategory.MOLECULE, AtomicCategory.ORGANISM),
                scopeReceiver = "RowScope",
                cardinality = SlotCardinality.MULTIPLE
            )
        )

        "Box" -> listOf(
            SlotDefinition(
                name = "content",
                allowedCategories = listOf(AtomicCategory.ATOM, AtomicCategory.MOLECULE, AtomicCategory.ORGANISM),
                scopeReceiver = "BoxScope",
                cardinality = SlotCardinality.MULTIPLE
            )
        )

        "Card", "ElevatedCard", "OutlinedCard", "Surface" -> listOf(
            SlotDefinition(
                name = "content",
                allowedCategories = listOf(AtomicCategory.ATOM, AtomicCategory.MOLECULE, AtomicCategory.ORGANISM),
                cardinality = SlotCardinality.MULTIPLE
            )
        )

        else -> {
            if (existingSlots.isEmpty()) emptyList()
            else existingSlots.filter { it.isComposable }.map { slot ->
                SlotDefinition(
                    name = slot.name,
                    allowedCategories = listOf(AtomicCategory.ATOM, AtomicCategory.MOLECULE, AtomicCategory.ORGANISM),
                    scopeReceiver = slot.receiverScope,
                    cardinality = if (slot.cardinality == "SINGLE") SlotCardinality.SINGLE else SlotCardinality.MULTIPLE
                )
            }
        }
    }
}

@Serializable
data class ComponentParameter(
    val name: String,
    val type: String,
    val rawKotlinType: String? = null,
    val isNullable: Boolean = false,
    val hasDefault: Boolean = true
)

@Serializable
data class ComponentCallback(
    val name: String,
    val signature: String? = null,
    val hasDefault: Boolean = false
)

@Serializable
data class ComponentSlot(
    val name: String,
    val receiverScope: String? = null,
    val cardinality: String? = "SINGLE",
    val takesParameter: Boolean = false,
    val parameterName: String? = null,
    val parameterType: String? = null,
    val hasDefault: Boolean = false,
    val isComposable: Boolean = true
)

@Serializable
data class ModifierCatalog(
    val composeMultiplatformVersion: String? = null,
    val material3Version: String? = null,
    val totalCount: Int = 0,
    val modifiers: List<ModifierDefinition> = emptyList()
)

@Serializable
data class ModifierDefinition(
    val id: String,
    val displayName: String,
    val packageName: String? = null,
    val category: String,
    val tier: String? = null,
    val applicableScopes: List<String> = emptyList(),
    val isExperimental: Boolean = false,
    val experimentalAnnotations: List<String> = emptyList(),
    val isDeprecated: Boolean = false,
    val deprecation: String? = null,
    val parameters: List<ModifierParameter> = emptyList(),
    val callbacks: List<ModifierCallback> = emptyList()
)

@Serializable
data class ModifierParameter(
    val name: String,
    val type: String,
    val rawKotlinType: String? = null,
    val isNullable: Boolean = false,
    val hasDefault: Boolean = true
)

@Serializable
data class ModifierCallback(
    val name: String,
    val signature: String? = null,
    val hasDefault: Boolean = false
)
