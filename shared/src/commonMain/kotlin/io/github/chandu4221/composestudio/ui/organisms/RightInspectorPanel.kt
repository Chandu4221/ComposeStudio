package io.github.chandu4221.composestudio.ui.organisms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.chandu4221.composestudio.state.LocalStudioStore
import io.github.chandu4221.composestudio.state.ModifierNode
import io.github.chandu4221.composestudio.state.StudioIntent
import io.github.chandu4221.composestudio.theme.AppTheme
import io.github.chandu4221.composestudio.ui.atoms.StudioIcon
import io.github.chandu4221.composestudio.ui.molecules.AccordionHeader
import io.github.chandu4221.composestudio.ui.molecules.AlignmentPicker
import io.github.chandu4221.composestudio.ui.molecules.ModifierItemRow
import io.github.chandu4221.composestudio.ui.molecules.SegmentedControl
import io.github.chandu4221.composestudio.ui.molecules.SizeStepSlider
import kotlinx.serialization.json.JsonPrimitive

/**
 * Right inspector organism allowing property and modifier customization of the selected component.
 * Connected to [StudioStore] via UDF.
 */
@Composable
fun RightInspectorPanel(
    modifier: Modifier = Modifier
) {
    val store = LocalStudioStore.current
    val projectState by store.state.collectAsState()
    val selectedNode = projectState.selectedNode

    var selectedTab by remember { mutableStateOf(0) }
    var selectedStyle by remember { mutableStateOf(0) }
    var selectedSize by remember { mutableStateOf(2) }
    var selectedAlignment by remember { mutableStateOf(Alignment.Center) }
    var paramsExpanded by remember { mutableStateOf(true) }

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
                            text = "Select a component on the canvas to inspect its properties.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    // Header with component title and actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedNode.catalogId,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            IconButton(onClick = {}, modifier = Modifier.size(32.dp)) {
                                StudioIcon(Icons.Default.MoreHoriz, "Options", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = {}, modifier = Modifier.size(32.dp)) {
                                StudioIcon(Icons.Default.ContentCopy, "Copy", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = {}, modifier = Modifier.size(32.dp)) {
                                StudioIcon(Icons.Default.Layers, "Group", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(
                                onClick = { store.dispatch(StudioIntent.DeleteNode(selectedNode.id)) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                StudioIcon(Icons.Default.DeleteOutline, "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }

                    // Mode Tabs: Design / Trigger / Semantics
                    SegmentedControl(
                        options = listOf("Design", "Trigger", "Semantics"),
                        selectedIndex = selectedTab,
                        onSelect = { selectedTab = it },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Section: Component Parameters
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        AccordionHeader(
                            title = "Component Parameters",
                            isExpanded = paramsExpanded,
                            onToggle = { paramsExpanded = !paramsExpanded }
                        )

                        if (paramsExpanded) {
                            // Text / Title Parameter field (if applicable)
                            val textValue = (selectedNode.properties["text"] as? JsonPrimitive)?.content
                            val titleValue = (selectedNode.properties["title"] as? JsonPrimitive)?.content

                            if (textValue != null) {
                                ParameterTextField(
                                    label = "Text",
                                    value = textValue,
                                    onValueChange = {
                                        store.dispatch(
                                            StudioIntent.UpdateProperty(selectedNode.id, "text", JsonPrimitive(it))
                                        )
                                    }
                                )
                            }

                            if (titleValue != null) {
                                ParameterTextField(
                                    label = "Title",
                                    value = titleValue,
                                    onValueChange = {
                                        store.dispatch(
                                            StudioIntent.UpdateProperty(selectedNode.id, "title", JsonPrimitive(it))
                                        )
                                    }
                                )
                            }

                            // Style
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Style",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                val styleOptions = listOf("Filled", "Tonal", "Outlined", "Standard")
                                SegmentedControl(
                                    options = styleOptions,
                                    selectedIndex = selectedStyle,
                                    onSelect = {
                                        selectedStyle = it
                                        store.dispatch(
                                            StudioIntent.UpdateProperty(
                                                selectedNode.id,
                                                "style",
                                                JsonPrimitive(styleOptions[it])
                                            )
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            // Size
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Size",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                SizeStepSlider(
                                    steps = listOf("XS", "S", "M", "L", "XL"),
                                    selectedIndex = selectedSize,
                                    onStepSelected = { selectedSize = it }
                                )
                            }

                            // Align
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Align",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                AlignmentPicker(
                                    selectedAlignment = selectedAlignment,
                                    onAlignmentSelected = { selectedAlignment = it }
                                )
                            }
                        }
                    }

                    // Section: Modifiers
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "Modifiers",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            TextButton(
                                onClick = {
                                    val newModifier = ModifierNode(
                                        modifierId = "padding",
                                        parameters = mapOf("all" to JsonPrimitive("16.dp"))
                                    )
                                    store.dispatch(StudioIntent.AddModifier(selectedNode.id, newModifier))
                                }
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    StudioIcon(Icons.Default.Add, "Add Modifier", tint = MaterialTheme.colorScheme.primary)
                                    Text("Add", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }

                        if (selectedNode.modifiers.isEmpty()) {
                            // Default preview modifiers
                            ModifierItemRow(
                                name = "padding",
                                value = "16.dp",
                                onRemove = {}
                            )
                            ModifierItemRow(
                                name = "background",
                                value = "Color.Primary",
                                onRemove = {},
                                leadingContent = {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                }
                            )
                        } else {
                            selectedNode.modifiers.forEach { modifierNode ->
                                ModifierItemRow(
                                    name = modifierNode.modifierId,
                                    value = modifierNode.parameters.values.firstOrNull()?.toString() ?: "",
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
