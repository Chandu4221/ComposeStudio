package io.github.chandu4221.composestudio.ui.organisms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.chandu4221.composestudio.state.ComponentNode
import io.github.chandu4221.composestudio.state.LocalStudioStore
import io.github.chandu4221.composestudio.state.ModifierNode
import io.github.chandu4221.composestudio.state.StudioIntent
import io.github.chandu4221.composestudio.theme.AppTheme
import io.github.chandu4221.composestudio.ui.atoms.StudioBadge
import io.github.chandu4221.composestudio.ui.atoms.StudioIcon
import io.github.chandu4221.composestudio.ui.molecules.AccordionHeader
import io.github.chandu4221.composestudio.ui.molecules.AlignmentPicker
import io.github.chandu4221.composestudio.ui.molecules.ModifierItemRow
import io.github.chandu4221.composestudio.ui.molecules.SegmentedControl
import io.github.chandu4221.composestudio.ui.molecules.SizeStepSlider
import kotlinx.serialization.json.JsonPrimitive

/**
 * Right inspector organism allowing property and modifier customization.
 * Dynamically adapts the options based on the selected component and parent scope.
 */
@Composable
fun RightInspectorPanel(
    modifier: Modifier = Modifier
) {
    val store = LocalStudioStore.current
    val projectState by store.state.collectAsState()
    val selectedNode = projectState.selectedNode

    var paramsExpanded by remember { mutableStateOf(true) }
    var scopeExpanded by remember { mutableStateOf(true) }
    var modifiersExpanded by remember { mutableStateOf(true) }
    var showAddModifierDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .width(360.dp)
            .fillMaxHeight(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(modifier = Modifier.fillMaxHeight()) {
            VerticalDivider(
                modifier = Modifier.fillMaxHeight(),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (selectedNode == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Select a component on the canvas to inspect its parameters and modifiers.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    val parentScope = projectState.getParentScope(selectedNode.id)

                    // 1. Header with component title, scope badge, and actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedNode.catalogId,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (parentScope != null) {
                                    StudioBadge(
                                        text = "${parentScope}Scope",
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            IconButton(
                                onClick = { store.dispatch(StudioIntent.DeleteNode(selectedNode.id)) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                StudioIcon(Icons.Default.DeleteOutline, "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }

                    // 2. Dynamic Component-Specific Parameters
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        AccordionHeader(
                            title = "Component Parameters",
                            isExpanded = paramsExpanded,
                            onToggle = { paramsExpanded = !paramsExpanded }
                        )

                        if (paramsExpanded) {
                            ComponentSpecificParameters(
                                node = selectedNode,
                                onUpdateProperty = { key, value ->
                                    store.dispatch(StudioIntent.UpdateProperty(selectedNode.id, key, value))
                                }
                            )
                        }
                    }

                    // 4. Scoped Layout in Parent Section (if inside Column, Row, Box, etc.)
                    if (parentScope != null) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            AccordionHeader(
                                title = "Layout in Parent (${parentScope}Scope)",
                                isExpanded = scopeExpanded,
                                onToggle = { scopeExpanded = !scopeExpanded }
                            )

                            if (scopeExpanded) {
                                ScopedLayoutParameters(
                                    node = selectedNode,
                                    parentScope = parentScope,
                                    onUpdateProperty = { key, value ->
                                        store.dispatch(StudioIntent.UpdateProperty(selectedNode.id, key, value))
                                    }
                                )
                            }
                        }
                    }

                    // 5. Modifiers Section (Dynamically filtered by Scope)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Modifiers (${selectedNode.modifiers.size})",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            TextButton(onClick = { showAddModifierDialog = !showAddModifierDialog }) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    StudioIcon(
                                        if (showAddModifierDialog) Icons.Default.Close else Icons.Default.Add,
                                        "Add Modifier",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        if (showAddModifierDialog) "Close" else "Add",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        // Add Modifier Picker Panel (Scope-Aware)
                        if (showAddModifierDialog) {
                            AddModifierPicker(
                                parentScope = parentScope,
                                onAddModifier = { modifierNode ->
                                    store.dispatch(StudioIntent.AddModifier(selectedNode.id, modifierNode))
                                    showAddModifierDialog = false
                                }
                            )
                        }

                        // Applied Modifiers List
                        if (selectedNode.modifiers.isEmpty()) {
                            Text(
                                text = "No modifiers applied. Click '+ Add' to attach one.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        } else {
                            selectedNode.modifiers.forEach { modifierNode ->
                                ModifierItemRow(
                                    name = modifierNode.modifierId,
                                    value = modifierNode.parameters.values.firstOrNull()?.toString()?.replace("\"", "") ?: "",
                                    onRemove = {
                                        store.dispatch(StudioIntent.RemoveModifier(selectedNode.id, modifierNode.id))
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dynamically renders tailored inspector parameters based on the component's catalogId.
 */
@Composable
private fun ComponentSpecificParameters(
    node: ComponentNode,
    onUpdateProperty: (String, JsonPrimitive) -> Unit
) {
    when (node.catalogId) {
        "Text" -> {
            val textValue = (node.properties["text"] as? JsonPrimitive)?.content ?: "Text"
            val fontSize = (node.properties["fontSize"] as? JsonPrimitive)?.content ?: "14"
            val fontWeight = (node.properties["fontWeight"] as? JsonPrimitive)?.content ?: "Normal"
            val textAlign = (node.properties["textAlign"] as? JsonPrimitive)?.content ?: "Start"

            ParameterTextField(
                label = "Text Content",
                value = textValue,
                onValueChange = { onUpdateProperty("text", JsonPrimitive(it)) }
            )

            // Font Size
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Font Size (${fontSize}sp)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val fontSizes = listOf("12", "14", "16", "20", "24", "32")
                val currentIndex = fontSizes.indexOf(fontSize).coerceAtLeast(1)
                SizeStepSlider(
                    steps = fontSizes.map { "${it}sp" },
                    selectedIndex = currentIndex,
                    onStepSelected = { onUpdateProperty("fontSize", JsonPrimitive(fontSizes[it])) }
                )
            }

            // Font Weight
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Font Weight",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val weights = listOf("Normal", "Medium", "SemiBold", "Bold")
                SegmentedControl(
                    options = weights,
                    selectedIndex = weights.indexOf(fontWeight).coerceAtLeast(0),
                    onSelect = { onUpdateProperty("fontWeight", JsonPrimitive(weights[it])) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Text Align
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Text Alignment",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val aligns = listOf("Start", "Center", "End")
                SegmentedControl(
                    options = aligns,
                    selectedIndex = aligns.indexOf(textAlign).coerceAtLeast(0),
                    onSelect = { onUpdateProperty("textAlign", JsonPrimitive(aligns[it])) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        "Button", "ElevatedButton", "FilledTonalButton", "OutlinedButton", "TextButton" -> {
            val label = (node.properties["text"] as? JsonPrimitive)?.content ?: "Button"
            val style = (node.properties["style"] as? JsonPrimitive)?.content ?: "Filled"
            val enabled = (node.properties["enabled"] as? JsonPrimitive)?.content?.toBooleanStrictOrNull() ?: true

            ParameterTextField(
                label = "Button Label",
                value = label,
                onValueChange = { onUpdateProperty("text", JsonPrimitive(it)) }
            )

            // Button Variant
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Variant Style",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val styles = listOf("Filled", "Tonal", "Outlined", "Text")
                SegmentedControl(
                    options = styles,
                    selectedIndex = styles.indexOf(style).coerceAtLeast(0),
                    onSelect = { onUpdateProperty("style", JsonPrimitive(styles[it])) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Enabled Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Enabled",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Switch(
                    checked = enabled,
                    onCheckedChange = { onUpdateProperty("enabled", JsonPrimitive(it)) }
                )
            }
        }

        "TextField", "OutlinedTextField" -> {
            val value = (node.properties["value"] as? JsonPrimitive)?.content ?: ""
            val label = (node.properties["label"] as? JsonPrimitive)?.content ?: "Input Label"
            val placeholder = (node.properties["placeholder"] as? JsonPrimitive)?.content ?: "Placeholder"
            val singleLine = (node.properties["singleLine"] as? JsonPrimitive)?.content?.toBooleanStrictOrNull() ?: true
            val isError = (node.properties["isError"] as? JsonPrimitive)?.content?.toBooleanStrictOrNull() ?: false

            ParameterTextField("Value", value) { onUpdateProperty("value", JsonPrimitive(it)) }
            ParameterTextField("Label", label) { onUpdateProperty("label", JsonPrimitive(it)) }
            ParameterTextField("Placeholder", placeholder) { onUpdateProperty("placeholder", JsonPrimitive(it)) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Single Line", style = MaterialTheme.typography.bodyMedium)
                Switch(checked = singleLine, onCheckedChange = { onUpdateProperty("singleLine", JsonPrimitive(it)) })
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Error State", style = MaterialTheme.typography.bodyMedium)
                Switch(checked = isError, onCheckedChange = { onUpdateProperty("isError", JsonPrimitive(it)) })
            }
        }

        "Switch", "Checkbox", "RadioButton" -> {
            val checked = (node.properties["checked"] as? JsonPrimitive)?.content?.toBooleanStrictOrNull()
                ?: (node.properties["selected"] as? JsonPrimitive)?.content?.toBooleanStrictOrNull()
                ?: false
            val enabled = (node.properties["enabled"] as? JsonPrimitive)?.content?.toBooleanStrictOrNull() ?: true

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("State (${if (checked) "ON" else "OFF"})", style = MaterialTheme.typography.bodyMedium)
                Switch(checked = checked, onCheckedChange = { onUpdateProperty("checked", JsonPrimitive(it)) })
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Enabled", style = MaterialTheme.typography.bodyMedium)
                Switch(checked = enabled, onCheckedChange = { onUpdateProperty("enabled", JsonPrimitive(it)) })
            }
        }

        "Slider" -> {
            val value = (node.properties["value"] as? JsonPrimitive)?.content?.toFloatOrNull() ?: 0.5f
            val steps = (node.properties["steps"] as? JsonPrimitive)?.content ?: "0"

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Slider Value (${(value * 100).toInt()}%)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = value,
                    onValueChange = { onUpdateProperty("value", JsonPrimitive(it)) }
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Discrete Steps", style = MaterialTheme.typography.labelMedium)
                val stepOptions = listOf("0", "2", "4", "8")
                SegmentedControl(
                    options = stepOptions,
                    selectedIndex = stepOptions.indexOf(steps).coerceAtLeast(0),
                    onSelect = { onUpdateProperty("steps", JsonPrimitive(stepOptions[it])) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        "Card", "ElevatedCard", "OutlinedCard" -> {
            val elevation = (node.properties["elevation"] as? JsonPrimitive)?.content ?: "2"
            val shape = (node.properties["shape"] as? JsonPrimitive)?.content ?: "Medium"

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Elevation (${elevation}dp)", style = MaterialTheme.typography.labelMedium)
                val elevations = listOf("0", "2", "4", "8")
                SegmentedControl(
                    options = elevations.map { "${it}dp" },
                    selectedIndex = elevations.indexOf(elevation).coerceAtLeast(1),
                    onSelect = { onUpdateProperty("elevation", JsonPrimitive(elevations[it])) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Corner Radius", style = MaterialTheme.typography.labelMedium)
                val shapes = listOf("Small", "Medium", "Large")
                SegmentedControl(
                    options = shapes,
                    selectedIndex = shapes.indexOf(shape).coerceAtLeast(1),
                    onSelect = { onUpdateProperty("shape", JsonPrimitive(shapes[it])) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        "Column" -> {
            val arrangement = (node.properties["verticalArrangement"] as? JsonPrimitive)?.content ?: "Top"
            val alignment = (node.properties["horizontalAlignment"] as? JsonPrimitive)?.content ?: "Start"
            val spacing = (node.properties["spacing"] as? JsonPrimitive)?.content ?: "8"

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Vertical Arrangement", style = MaterialTheme.typography.labelMedium)
                val arrs = listOf("Top", "Center", "Bottom", "Between")
                SegmentedControl(
                    options = arrs,
                    selectedIndex = arrs.indexOf(arrangement).coerceAtLeast(0),
                    onSelect = { onUpdateProperty("verticalArrangement", JsonPrimitive(arrs[it])) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Horizontal Alignment", style = MaterialTheme.typography.labelMedium)
                val algns = listOf("Start", "Center", "End")
                SegmentedControl(
                    options = algns,
                    selectedIndex = algns.indexOf(alignment).coerceAtLeast(0),
                    onSelect = { onUpdateProperty("horizontalAlignment", JsonPrimitive(algns[it])) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Item Spacing (${spacing}dp)", style = MaterialTheme.typography.labelMedium)
                val spacings = listOf("0", "8", "12", "16", "24")
                SegmentedControl(
                    options = spacings.map { "${it}dp" },
                    selectedIndex = spacings.indexOf(spacing).coerceAtLeast(1),
                    onSelect = { onUpdateProperty("spacing", JsonPrimitive(spacings[it])) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        "Row" -> {
            val arrangement = (node.properties["horizontalArrangement"] as? JsonPrimitive)?.content ?: "Start"
            val alignment = (node.properties["verticalAlignment"] as? JsonPrimitive)?.content ?: "Center"

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Horizontal Arrangement", style = MaterialTheme.typography.labelMedium)
                val arrs = listOf("Start", "Center", "End", "Between")
                SegmentedControl(
                    options = arrs,
                    selectedIndex = arrs.indexOf(arrangement).coerceAtLeast(0),
                    onSelect = { onUpdateProperty("horizontalArrangement", JsonPrimitive(arrs[it])) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Vertical Alignment", style = MaterialTheme.typography.labelMedium)
                val algns = listOf("Top", "Center", "Bottom")
                SegmentedControl(
                    options = algns,
                    selectedIndex = algns.indexOf(alignment).coerceAtLeast(1),
                    onSelect = { onUpdateProperty("verticalAlignment", JsonPrimitive(algns[it])) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        "TopAppBar", "CenterAlignedTopAppBar" -> {
            val title = (node.properties["title"] as? JsonPrimitive)?.content ?: "App Bar"
            val titleAlign = (node.properties["titleAlign"] as? JsonPrimitive)?.content
                ?: if (node.catalogId == "CenterAlignedTopAppBar") "Center" else "Start"
            val navIcon = (node.properties["navIcon"] as? JsonPrimitive)?.content ?: "Back"
            val action1 = (node.properties["action1"] as? JsonPrimitive)?.content ?: "None"
            val action2 = (node.properties["action2"] as? JsonPrimitive)?.content ?: "More"
            val containerColor = (node.properties["containerColor"] as? JsonPrimitive)?.content ?: "Surface"

            ParameterTextField("Title Text (title slot)", title) { onUpdateProperty("title", JsonPrimitive(it)) }

            // Title Alignment
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Title Slot Alignment", style = MaterialTheme.typography.labelMedium)
                val aligns = listOf("Start", "Center")
                SegmentedControl(
                    options = aligns,
                    selectedIndex = aligns.indexOf(titleAlign).coerceAtLeast(0),
                    onSelect = { onUpdateProperty("titleAlign", JsonPrimitive(aligns[it])) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Navigation Icon Slot (Leading)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Navigation Icon (Leading Slot)", style = MaterialTheme.typography.labelMedium)
                val navIcons = listOf("Back", "Menu", "Close", "None")
                SegmentedControl(
                    options = navIcons,
                    selectedIndex = navIcons.indexOf(navIcon).coerceAtLeast(0),
                    onSelect = { onUpdateProperty("navIcon", JsonPrimitive(navIcons[it])) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Actions Slot 1
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Action 1 (Trailing Slot)", style = MaterialTheme.typography.labelMedium)
                val actionOptions = listOf("Search", "Share", "Favorite", "None")
                SegmentedControl(
                    options = actionOptions,
                    selectedIndex = actionOptions.indexOf(action1).coerceAtLeast(0),
                    onSelect = { onUpdateProperty("action1", JsonPrimitive(actionOptions[it])) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Actions Slot 2
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Action 2 (Trailing Slot)", style = MaterialTheme.typography.labelMedium)
                val actionOptions = listOf("More", "Settings", "Notifications", "None")
                SegmentedControl(
                    options = actionOptions,
                    selectedIndex = actionOptions.indexOf(action2).coerceAtLeast(0),
                    onSelect = { onUpdateProperty("action2", JsonPrimitive(actionOptions[it])) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Container Color
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("App Bar Color", style = MaterialTheme.typography.labelMedium)
                val colors = listOf("Surface", "PrimaryContainer", "SurfaceVariant")
                SegmentedControl(
                    options = colors,
                    selectedIndex = colors.indexOf(containerColor).coerceAtLeast(0),
                    onSelect = { onUpdateProperty("containerColor", JsonPrimitive(colors[it])) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        "HorizontalDivider", "VerticalDivider" -> {
            val thickness = (node.properties["thickness"] as? JsonPrimitive)?.content ?: "1"
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Thickness (${thickness}dp)", style = MaterialTheme.typography.labelMedium)
                val ths = listOf("1", "2", "4")
                SegmentedControl(
                    options = ths.map { "${it}dp" },
                    selectedIndex = ths.indexOf(thickness).coerceAtLeast(0),
                    onSelect = { onUpdateProperty("thickness", JsonPrimitive(ths[it])) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        else -> {
            // Fallback for general components: render editable textfields for all string properties
            if (node.properties.isEmpty()) {
                Text(
                    text = "Standard Material 3 defaults active.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                node.properties.forEach { (key, value) ->
                    val textVal = (value as? JsonPrimitive)?.content ?: value.toString()
                    ParameterTextField(key, textVal) { onUpdateProperty(key, JsonPrimitive(it)) }
                }
            }
        }
    }
}

/**
 * Scoped layout controls that only make sense inside specific containers (Column, Row, Box).
 */
@Composable
private fun ScopedLayoutParameters(
    node: ComponentNode,
    parentScope: String,
    onUpdateProperty: (String, JsonPrimitive) -> Unit
) {
    when (parentScope) {
        "COLUMN" -> {
            // align(Alignment.Horizontal) and weight()
            val align = (node.properties["scopeAlign"] as? JsonPrimitive)?.content ?: "Start"
            val weight = (node.properties["scopeWeight"] as? JsonPrimitive)?.content ?: "None"

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Horizontal Align (ColumnScope)", style = MaterialTheme.typography.labelMedium)
                val aligns = listOf("Start", "Center", "End")
                SegmentedControl(
                    options = aligns,
                    selectedIndex = aligns.indexOf(align).coerceAtLeast(0),
                    onSelect = { onUpdateProperty("scopeAlign", JsonPrimitive(aligns[it])) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Expand Weight (weight)", style = MaterialTheme.typography.labelMedium)
                val weights = listOf("None", "0.5", "1.0", "2.0")
                SegmentedControl(
                    options = weights,
                    selectedIndex = weights.indexOf(weight).coerceAtLeast(0),
                    onSelect = { onUpdateProperty("scopeWeight", JsonPrimitive(weights[it])) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        "ROW" -> {
            // align(Alignment.Vertical) and weight()
            val align = (node.properties["scopeAlign"] as? JsonPrimitive)?.content ?: "Center"
            val weight = (node.properties["scopeWeight"] as? JsonPrimitive)?.content ?: "None"

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Vertical Align (RowScope)", style = MaterialTheme.typography.labelMedium)
                val aligns = listOf("Top", "Center", "Bottom")
                SegmentedControl(
                    options = aligns,
                    selectedIndex = aligns.indexOf(align).coerceAtLeast(1),
                    onSelect = { onUpdateProperty("scopeAlign", JsonPrimitive(aligns[it])) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Expand Weight (weight)", style = MaterialTheme.typography.labelMedium)
                val weights = listOf("None", "0.5", "1.0", "2.0")
                SegmentedControl(
                    options = weights,
                    selectedIndex = weights.indexOf(weight).coerceAtLeast(0),
                    onSelect = { onUpdateProperty("scopeWeight", JsonPrimitive(weights[it])) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        "BOX" -> {
            // 9-point Box alignment
            val align = (node.properties["boxAlign"] as? JsonPrimitive)?.content ?: "Center"
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Box Alignment (BoxScope)", style = MaterialTheme.typography.labelMedium)
                val aligns = listOf("TopStart", "Center", "BottomEnd")
                SegmentedControl(
                    options = aligns,
                    selectedIndex = aligns.indexOf(align).coerceAtLeast(1),
                    onSelect = { onUpdateProperty("boxAlign", JsonPrimitive(aligns[it])) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Scope-aware Modifier Picker that presents scoped modifiers first,
 * followed by universal size, spacing, drawing, and interaction modifiers.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddModifierPicker(
    parentScope: String?,
    onAddModifier: (ModifierNode) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Scoped Section (if inside Column, Row, Box)
            if (parentScope != null) {
                Text(
                    text = "SCOPED TO ${parentScope}SCOPE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    when (parentScope) {
                        "COLUMN" -> {
                            ModifierChip("weight(1f)") {
                                onAddModifier(ModifierNode(modifierId = "weight", parameters = mapOf("weight" to JsonPrimitive("1.0f"))))
                            }
                            ModifierChip("align(CenterHorizontally)") {
                                onAddModifier(ModifierNode(modifierId = "align", parameters = mapOf("alignment" to JsonPrimitive("CenterHorizontally"))))
                            }
                        }
                        "ROW" -> {
                            ModifierChip("weight(1f)") {
                                onAddModifier(ModifierNode(modifierId = "weight", parameters = mapOf("weight" to JsonPrimitive("1.0f"))))
                            }
                            ModifierChip("align(CenterVertically)") {
                                onAddModifier(ModifierNode(modifierId = "align", parameters = mapOf("alignment" to JsonPrimitive("CenterVertically"))))
                            }
                        }
                        "BOX" -> {
                            ModifierChip("align(Center)") {
                                onAddModifier(ModifierNode(modifierId = "align", parameters = mapOf("alignment" to JsonPrimitive("Center"))))
                            }
                            ModifierChip("matchParentSize()") {
                                onAddModifier(ModifierNode(modifierId = "matchParentSize"))
                            }
                        }
                    }
                }
            }

            // Universal Size Modifiers
            Text(
                text = "SIZE & CONSTRAINTS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ModifierChip("fillMaxWidth()") {
                    onAddModifier(ModifierNode(modifierId = "fillMaxWidth"))
                }
                ModifierChip("fillMaxHeight()") {
                    onAddModifier(ModifierNode(modifierId = "fillMaxHeight"))
                }
                ModifierChip("fillMaxSize()") {
                    onAddModifier(ModifierNode(modifierId = "fillMaxSize"))
                }
                ModifierChip("height(48.dp)") {
                    onAddModifier(ModifierNode(modifierId = "height", parameters = mapOf("height" to JsonPrimitive("48.dp"))))
                }
                ModifierChip("width(120.dp)") {
                    onAddModifier(ModifierNode(modifierId = "width", parameters = mapOf("width" to JsonPrimitive("120.dp"))))
                }
            }

            // Universal Spacing Modifiers
            Text(
                text = "SPACING",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ModifierChip("padding(16.dp)") {
                    onAddModifier(ModifierNode(modifierId = "padding", parameters = mapOf("all" to JsonPrimitive("16.dp"))))
                }
                ModifierChip("padding(8.dp)") {
                    onAddModifier(ModifierNode(modifierId = "padding", parameters = mapOf("all" to JsonPrimitive("8.dp"))))
                }
                ModifierChip("offset(x=8.dp, y=8.dp)") {
                    onAddModifier(ModifierNode(modifierId = "offset", parameters = mapOf("x" to JsonPrimitive("8.dp"), "y" to JsonPrimitive("8.dp"))))
                }
            }

            // Appearance & Drawing
            Text(
                text = "APPEARANCE",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ModifierChip("background(Primary)") {
                    onAddModifier(ModifierNode(modifierId = "background", parameters = mapOf("color" to JsonPrimitive("Color.Primary"))))
                }
                ModifierChip("border(1.dp, Outline)") {
                    onAddModifier(ModifierNode(modifierId = "border", parameters = mapOf("width" to JsonPrimitive("1.dp"), "color" to JsonPrimitive("Outline"))))
                }
                ModifierChip("clickable()") {
                    onAddModifier(ModifierNode(modifierId = "clickable"))
                }
            }
        }
    }
}

/**
 * Clickable pill chip used in the Add Modifier picker.
 */
@Composable
private fun ModifierChip(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = MaterialTheme.shapes.extraSmall,
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/**
 * Compact parameter text input row for properties like text, title, etc.
 */
@Composable
private fun ParameterTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Surface(
            modifier = Modifier.fillMaxWidth().height(36.dp),
            shape = MaterialTheme.shapes.small,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            color = MaterialTheme.colorScheme.surface
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RightInspectorPanelPreview() {
    AppTheme {
        RightInspectorPanel()
    }
}
