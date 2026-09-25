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
    val tier: String? = null,
    val isExperimental: Boolean = false,
    val experimentalAnnotations: List<String> = emptyList(),
    val isDeprecated: Boolean = false,
    val deprecation: String? = null,
    val receiverScope: String? = null,
    val parameters: List<ComponentParameter> = emptyList(),
    val callbacks: List<ComponentCallback> = emptyList(),
    val slots: List<ComponentSlot> = emptyList()
) {
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
