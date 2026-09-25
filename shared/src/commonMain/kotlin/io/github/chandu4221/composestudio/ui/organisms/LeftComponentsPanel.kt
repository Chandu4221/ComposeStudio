package io.github.chandu4221.composestudio.ui.organisms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.chandu4221.composestudio.data.CatalogRepository
import io.github.chandu4221.composestudio.data.ComponentDefinition
import io.github.chandu4221.composestudio.state.ComponentNode
import io.github.chandu4221.composestudio.state.LocalStudioStore
import io.github.chandu4221.composestudio.state.StudioIntent
import io.github.chandu4221.composestudio.theme.AppTheme
import io.github.chandu4221.composestudio.ui.atoms.StudioBadge
import io.github.chandu4221.composestudio.ui.atoms.StudioIcon
import io.github.chandu4221.composestudio.ui.molecules.AccordionHeader
import io.github.chandu4221.composestudio.ui.molecules.ComponentPaletteItem
import io.github.chandu4221.composestudio.ui.molecules.SearchField
import io.github.chandu4221.composestudio.ui.molecules.SegmentedControl

private val CATEGORY_DISPLAY_NAMES = mapOf(
    "INPUT" to "Actions & Inputs",
    "LAYOUT" to "Layout",
    "NAVIGATION" to "Navigation",
    "SURFACE" to "Surfaces & Containment",
    "DISPLAY" to "Display & Text"
)

private val CATEGORY_ORDER = listOf("INPUT", "LAYOUT", "NAVIGATION", "SURFACE", "DISPLAY")

/**
 * Left panel organism showing the Parts/Layers tabs, component search, and palette sections.
 * Connected to [StudioStore] via UDF.
 */
@Composable
fun LeftComponentsPanel(
    modifier: Modifier = Modifier,
    onComponentSelected: (String) -> Unit = {}
) {
    val store = LocalStudioStore.current
    val projectState by store.state.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var components by remember { mutableStateOf<List<ComponentDefinition>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var expandedCategories by remember {
        mutableStateOf(setOf("INPUT", "LAYOUT", "NAVIGATION", "SURFACE", "DISPLAY"))
    }

    LaunchedEffect(Unit) {
        val catalog = CatalogRepository.getComponentCatalog()
        if (catalog.components.isNotEmpty()) {
            components = catalog.components
            store.dispatch(StudioIntent.LoadCatalog(catalog))
        }
        isLoading = false
    }

    val filteredComponents = remember(components, searchQuery) {
        CatalogRepository.filterComponents(components, searchQuery)
    }

    val groupedComponents = remember(filteredComponents) {
        val groups = filteredComponents.groupBy { it.category }
        val orderedCategories = CATEGORY_ORDER.filter { it in groups } +
                (groups.keys - CATEGORY_ORDER.toSet()).sorted()
        orderedCategories.associateWith { groups[it].orEmpty() }
    }

    Surface(
        modifier = modifier
            .width(260.dp)
            .fillMaxHeight(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(modifier = Modifier.fillMaxHeight()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Tabs: Parts / Layers
                SegmentedControl(
                    options = listOf("Parts", "Layers"),
                    selectedIndex = selectedTab,
                    onSelect = { selectedTab = it },
                    modifier = Modifier.fillMaxWidth()
                )

                if (selectedTab == 0) {
                    // Search
                    SearchField(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it }
                    )

                    if (isLoading && components.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.width(28.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.dp
                            )
                        }
                    } else if (filteredComponents.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (searchQuery.isNotEmpty()) {
                                    "No components matching \"$searchQuery\""
                                } else {
                                    "No components available"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        // Render component groups dynamically
                        groupedComponents.forEach { (category, items) ->
                            if (items.isNotEmpty()) {
                                val isSearching = searchQuery.isNotBlank()
                                val isExpanded = isSearching || (category in expandedCategories)
                                val categoryTitle = CATEGORY_DISPLAY_NAMES[category]
                                    ?: category.lowercase().replaceFirstChar { it.uppercase() }

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    AccordionHeader(
                                        title = "$categoryTitle (${items.size})",
                                        isExpanded = isExpanded,
                                        onToggle = {
                                            expandedCategories = if (category in expandedCategories) {
                                                expandedCategories - category
                                            } else {
                                                expandedCategories + category
                                            }
                                        }
                                    )

                                    if (isExpanded) {
                                        items.forEach { component ->
                                            ComponentPaletteItem(
                                                label = component.displayName,
                                                icon = component.resolveIcon(),
                                                onClick = {
                                                    onComponentSelected(component.id)
                                                    val targetParentId = projectState.selectedNodeId
                                                        ?: projectState.rootNode?.id
                                                        ?: ""
                                                    store.dispatch(
                                                        StudioIntent.DropComponent(
                                                            targetParentId = targetParentId,
                                                            targetSlotName = null,
                                                            component = component
                                                        )
                                                    )
                                                },
                                                badge = if (component.isExperimental) {
                                                    {
                                                        StudioBadge(
                                                            text = "Exp",
                                                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                                                        )
                                                    }
                                                } else null
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Live Layers View
                    val root = projectState.rootNode
                    if (root == null) {
                        Text(
                            text = "Canvas is empty",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    } else {
                        LiveLayersTree(
                            node = root,
                            selectedNodeId = projectState.selectedNodeId,
                            onSelectNode = { store.dispatch(StudioIntent.SelectNode(it)) },
                            depth = 0
                        )
                    }
                }
            }

            VerticalDivider(
                modifier = Modifier.fillMaxHeight(),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}

/**
 * Recursive live component tree representing the visual document hierarchy.
 */
@Composable
private fun LiveLayersTree(
    node: ComponentNode,
    selectedNodeId: String?,
    onSelectNode: (String) -> Unit,
    depth: Int,
    slotName: String? = null
) {
    val isSelected = node.id == selectedNodeId

    Column(modifier = Modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = (depth * 12).dp)
                .clickable(role = androidx.compose.ui.semantics.Role.Button) { onSelectNode(node.id) }
                .border(
                    width = if (isSelected) 1.dp else 0.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent,
                    shape = MaterialTheme.shapes.extraSmall
                ),
            shape = MaterialTheme.shapes.extraSmall,
            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else androidx.compose.ui.graphics.Color.Transparent
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (depth > 0) {
                    StudioIcon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Child",
                        size = 14.dp,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = if (slotName != null) "$slotName: ${node.catalogId}" else node.catalogId,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Render children grouped by slot
        node.slots.forEach { (slot, children) ->
            children.forEach { child ->
                LiveLayersTree(
                    node = child,
                    selectedNodeId = selectedNodeId,
                    onSelectNode = onSelectNode,
                    depth = depth + 1,
                    slotName = slot
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LeftComponentsPanelPreview() {
    AppTheme {
        LeftComponentsPanel()
    }
}
