package io.github.chandu4221.composestudio.ui.organisms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.chandu4221.composestudio.state.ComponentNode
import io.github.chandu4221.composestudio.state.LocalStudioStore
import io.github.chandu4221.composestudio.state.StudioIntent
import io.github.chandu4221.composestudio.theme.AppTheme
import io.github.chandu4221.composestudio.ui.atoms.StudioIcon
import kotlinx.serialization.json.JsonPrimitive

/**
 * Mobile device frame mockup organism representing the live interactive canvas.
 * Recursively renders the visual [ComponentNode] tree managed by [LocalStudioStore].
 */
@Composable
fun DeviceMockupFrame(
    modifier: Modifier = Modifier
) {
    val store = LocalStudioStore.current
    val projectState by store.state.collectAsState()
    val phoneShape = RoundedCornerShape(32.dp)

    Surface(
        modifier = modifier
            .width(340.dp)
            .height(680.dp)
            .border(width = 2.dp, color = MaterialTheme.colorScheme.outlineVariant, shape = phoneShape),
        shape = phoneShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "9:30",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Camera punch hole
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF334155))
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    StudioIcon(Icons.Default.Wifi, "Wifi", size = 14.dp, tint = MaterialTheme.colorScheme.onSurface)
                    StudioIcon(Icons.Default.BatteryFull, "Battery", size = 14.dp, tint = MaterialTheme.colorScheme.onSurface)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Canvas Content: Render Live Root Node Hierarchy
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                val root = projectState.rootNode
                if (root != null) {
                    RenderNode(
                        node = root,
                        selectedNodeId = projectState.selectedNodeId,
                        onSelectNode = { store.dispatch(StudioIntent.SelectNode(it)) }
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxWidth().align(Alignment.Center),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Drop a Scaffold or Layout to start",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Bottom Home Bar Indicator
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .size(width = 80.dp, height = 4.dp)
                    .clip(MaterialTheme.shapes.extraLarge)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }
    }
}

/**
 * Recursive dynamic component renderer with interactive selection bounds.
 */
@Composable
private fun RenderNode(
    node: ComponentNode,
    selectedNodeId: String?,
    onSelectNode: (String) -> Unit
) {
    val isSelected = node.id == selectedNodeId

    when (node.catalogId) {
        "Scaffold" -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableWrapper(node.id, isSelected, onSelectNode)
            ) {
                // TopBar Slot
                val topBars = node.slots["topBar"].orEmpty()
                if (topBars.isNotEmpty()) {
                    topBars.forEach { child ->
                        RenderNode(child, selectedNodeId, onSelectNode)
                    }
                }

                // Main Content Slot
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    val contentNodes = node.slots["content"].orEmpty()
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        contentNodes.forEach { child ->
                            RenderNode(child, selectedNodeId, onSelectNode)
                        }
                    }
                }

                // FloatingActionButton Slot
                val fabs = node.slots["floatingActionButton"].orEmpty()
                if (fabs.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        fabs.forEach { child ->
                            RenderNode(child, selectedNodeId, onSelectNode)
                        }
                    }
                }

                // BottomBar Slot
                val bottomBars = node.slots["bottomBar"].orEmpty()
                if (bottomBars.isNotEmpty()) {
                    bottomBars.forEach { child ->
                        RenderNode(child, selectedNodeId, onSelectNode)
                    }
                }
            }
        }

        "TopAppBar", "CenterAlignedTopAppBar" -> {
            val titleText = (node.properties["title"] as? JsonPrimitive)?.content ?: "App Bar"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableWrapper(node.id, isSelected, onSelectNode)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StudioIcon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface)
                    Text(
                        text = titleText,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    StudioIcon(Icons.Default.MoreVert, "More", tint = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        "Column" -> {
            val arrangement = (node.properties["verticalArrangement"] as? JsonPrimitive)?.content ?: "Top"
            val alignment = (node.properties["horizontalAlignment"] as? JsonPrimitive)?.content ?: "Start"
            val spacing = (node.properties["spacing"] as? JsonPrimitive)?.content?.toIntOrNull()?.dp ?: 8.dp

            val verticalArrangement = when (arrangement) {
                "Center" -> Arrangement.Center
                "Bottom" -> Arrangement.Bottom
                "Between" -> Arrangement.SpaceBetween
                else -> Arrangement.spacedBy(spacing)
            }
            val horizontalAlignment = when (alignment) {
                "Center" -> Alignment.CenterHorizontally
                "End" -> Alignment.End
                else -> Alignment.Start
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableWrapper(node.id, isSelected, onSelectNode),
                verticalArrangement = verticalArrangement,
                horizontalAlignment = horizontalAlignment
            ) {
                val children = node.slots["content"].orEmpty()
                if (children.isEmpty()) {
                    Text(
                        text = "Empty Column",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(8.dp)
                    )
                } else {
                    children.forEach { child ->
                        RenderNode(child, selectedNodeId, onSelectNode)
                    }
                }
            }
        }

        "Row" -> {
            val arrangement = (node.properties["horizontalArrangement"] as? JsonPrimitive)?.content ?: "Start"
            val alignment = (node.properties["verticalAlignment"] as? JsonPrimitive)?.content ?: "Center"

            val horizontalArrangement = when (arrangement) {
                "Center" -> Arrangement.Center
                "End" -> Arrangement.End
                "Between" -> Arrangement.SpaceBetween
                else -> Arrangement.spacedBy(8.dp)
            }
            val verticalAlignment = when (alignment) {
                "Top" -> Alignment.Top
                "Bottom" -> Alignment.Bottom
                else -> Alignment.CenterVertically
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableWrapper(node.id, isSelected, onSelectNode),
                horizontalArrangement = horizontalArrangement,
                verticalAlignment = verticalAlignment
            ) {
                val children = node.slots["content"].orEmpty()
                children.forEach { child ->
                    RenderNode(child, selectedNodeId, onSelectNode)
                }
            }
        }

        "Card", "ElevatedCard", "OutlinedCard" -> {
            val elevation = (node.properties["elevation"] as? JsonPrimitive)?.content?.toIntOrNull()?.dp ?: 2.dp
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableWrapper(node.id, isSelected, onSelectNode),
                elevation = CardDefaults.cardElevation(defaultElevation = elevation),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val children = node.slots["content"].orEmpty()
                    if (children.isEmpty()) {
                        Text(
                            text = "Card Content",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        children.forEach { child ->
                            RenderNode(child, selectedNodeId, onSelectNode)
                        }
                    }
                }
            }
        }

        "Button", "ElevatedButton", "FilledTonalButton", "OutlinedButton", "TextButton" -> {
            val label = (node.properties["text"] as? JsonPrimitive)?.content ?: "Button"
            val style = (node.properties["style"] as? JsonPrimitive)?.content ?: "Filled"
            val enabled = (node.properties["enabled"] as? JsonPrimitive)?.content?.toBooleanStrictOrNull() ?: true

            Box(
                modifier = Modifier.selectableWrapper(node.id, isSelected, onSelectNode)
            ) {
                when (style) {
                    "Tonal" -> {
                        FilledTonalButton(
                            onClick = { onSelectNode(node.id) },
                            enabled = enabled,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(label)
                        }
                    }
                    "Outlined" -> {
                        OutlinedButton(
                            onClick = { onSelectNode(node.id) },
                            enabled = enabled,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(label)
                        }
                    }
                    "Text" -> {
                        TextButton(
                            onClick = { onSelectNode(node.id) },
                            enabled = enabled,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(label)
                        }
                    }
                    else -> {
                        Button(
                            onClick = { onSelectNode(node.id) },
                            enabled = enabled,
                            shape = MaterialTheme.shapes.small,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }

        "FloatingActionButton" -> {
            Box(
                modifier = Modifier.selectableWrapper(node.id, isSelected, onSelectNode)
            ) {
                FloatingActionButton(
                    onClick = { onSelectNode(node.id) },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    StudioIcon(Icons.Default.Add, "Add")
                }
            }
        }

        "Text" -> {
            val textContent = (node.properties["text"] as? JsonPrimitive)?.content ?: "Text"
            val fontSize = (node.properties["fontSize"] as? JsonPrimitive)?.content?.toFloatOrNull()?.sp ?: 14.sp
            val fontWeight = when ((node.properties["fontWeight"] as? JsonPrimitive)?.content) {
                "Bold" -> FontWeight.Bold
                "SemiBold" -> FontWeight.SemiBold
                "Medium" -> FontWeight.Medium
                else -> FontWeight.Normal
            }
            val textAlign = when ((node.properties["textAlign"] as? JsonPrimitive)?.content) {
                "Center" -> TextAlign.Center
                "End" -> TextAlign.End
                else -> TextAlign.Start
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableWrapper(node.id, isSelected, onSelectNode)
            ) {
                Text(
                    text = textContent,
                    fontSize = fontSize,
                    fontWeight = fontWeight,
                    textAlign = textAlign,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        "TextField", "OutlinedTextField" -> {
            val value = (node.properties["value"] as? JsonPrimitive)?.content ?: ""
            val label = (node.properties["label"] as? JsonPrimitive)?.content ?: "Label"
            val placeholder = (node.properties["placeholder"] as? JsonPrimitive)?.content ?: "Placeholder"
            val isError = (node.properties["isError"] as? JsonPrimitive)?.content?.toBooleanStrictOrNull() ?: false
            val singleLine = (node.properties["singleLine"] as? JsonPrimitive)?.content?.toBooleanStrictOrNull() ?: true

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableWrapper(node.id, isSelected, onSelectNode)
            ) {
                OutlinedTextField(
                    value = value,
                    onValueChange = {},
                    label = { Text(label) },
                    placeholder = { Text(placeholder) },
                    isError = isError,
                    singleLine = singleLine,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = true
                )
            }
        }

        "Switch" -> {
            val checked = (node.properties["checked"] as? JsonPrimitive)?.content?.toBooleanStrictOrNull() ?: false
            val enabled = (node.properties["enabled"] as? JsonPrimitive)?.content?.toBooleanStrictOrNull() ?: true
            Box(
                modifier = Modifier.selectableWrapper(node.id, isSelected, onSelectNode)
            ) {
                Switch(checked = checked, onCheckedChange = { onSelectNode(node.id) }, enabled = enabled)
            }
        }

        "Checkbox" -> {
            val checked = (node.properties["checked"] as? JsonPrimitive)?.content?.toBooleanStrictOrNull() ?: true
            val enabled = (node.properties["enabled"] as? JsonPrimitive)?.content?.toBooleanStrictOrNull() ?: true
            Box(
                modifier = Modifier.selectableWrapper(node.id, isSelected, onSelectNode)
            ) {
                Checkbox(checked = checked, onCheckedChange = { onSelectNode(node.id) }, enabled = enabled)
            }
        }

        "Slider" -> {
            val value = (node.properties["value"] as? JsonPrimitive)?.content?.toFloatOrNull() ?: 0.5f
            val enabled = (node.properties["enabled"] as? JsonPrimitive)?.content?.toBooleanStrictOrNull() ?: true
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableWrapper(node.id, isSelected, onSelectNode)
            ) {
                Slider(value = value, onValueChange = {}, enabled = enabled)
            }
        }

        else -> {
            // Generic fallback rendering for all other catalog components
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableWrapper(node.id, isSelected, onSelectNode),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = node.catalogId,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

/**
 * Modifier wrapper that applies selection borders, clickable selection, and tag badges.
 */
private fun Modifier.selectableWrapper(
    nodeId: String,
    isSelected: Boolean,
    onSelectNode: (String) -> Unit
): Modifier = this
    .clickable(role = androidx.compose.ui.semantics.Role.Button) { onSelectNode(nodeId) }
    .border(
        width = if (isSelected) 1.5.dp else 0.dp,
        color = if (isSelected) Color(0xFF6366F1) else Color.Transparent,
        shape = RoundedCornerShape(4.dp)
    )
    .padding(if (isSelected) 2.dp else 0.dp)

@Preview(showBackground = true)
@Composable
private fun DeviceMockupFramePreview() {
    AppTheme {
        DeviceMockupFrame()
    }
}
