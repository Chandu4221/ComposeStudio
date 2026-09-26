package io.github.chandu4221.composestudio.ui.organisms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.chandu4221.composestudio.data.AtomicCategory
import io.github.chandu4221.composestudio.data.CatalogRepository
import io.github.chandu4221.composestudio.data.ComponentCatalog
import io.github.chandu4221.composestudio.data.ComponentDefinition
import io.github.chandu4221.composestudio.data.SlotCardinality
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
import io.github.chandu4221.composestudio.ui.molecules.SidebarPosition
import io.github.chandu4221.composestudio.ui.molecules.SidebarTrigger

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
    isOpen: Boolean = true,
    onToggle: () -> Unit = {},
    onClose: (() -> Unit)? = null,
    onComponentSelected: (String) -> Unit = {}
) {
    val actualToggle = onClose ?: onToggle
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
        modifier = modifier.fillMaxHeight(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // FIXED HEADER (48.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Fixed SidebarTrigger anchored at top-left
                SidebarTrigger(
                    isOpen = isOpen,
                    onToggle = actualToggle,
                    position = SidebarPosition.Left
                )

                if (isOpen) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Components",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // BODY
            if (isOpen) {
                BoxWithConstraints(
                    modifier = Modifier.weight(1f).fillMaxWidth()
                ) {
                    val panelWidth = maxWidth
                    val minWidth = 240.dp
                    val isScrollable = panelWidth < minWidth

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(if (isScrollable) Modifier.horizontalScroll(rememberScrollState()) else Modifier)
                    ) {
                        Column(
                            modifier = Modifier
                                .then(if (isScrollable) Modifier.width(minWidth) else Modifier.fillMaxWidth())
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

                    // Active Slot Targeting Banner
                    val activeSlot = projectState.selectedSlotName
                    val selectedNode = projectState.selectedNode
                    if (activeSlot != null && selectedNode != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Targeting Slot:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = "${selectedNode.catalogId} > $activeSlot",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                IconButton(
                                    onClick = { store.dispatch(StudioIntent.SelectSlot(selectedNode.id, null)) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    StudioIcon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear slot target",
                                        size = 14.dp,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }

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
                                                            targetSlotName = projectState.selectedSlotName,
                                                            component = component
                                                        )
                                                    )
                                                },
                                                badge = {
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        if (component.isExperimental) {
                                                            StudioBadge(
                                                                text = "Exp",
                                                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                                                            )
                                                        }
                                                        val (badgeBg, badgeFg) = when (component.atomicCategory) {
                                                            AtomicCategory.TEMPLATE -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
                                                            AtomicCategory.ORGANISM -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
                                                            AtomicCategory.MOLECULE -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
                                                            AtomicCategory.ATOM -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
                                                        }
                                                        StudioBadge(
                                                            text = component.atomicCategory.name,
                                                            containerColor = badgeBg,
                                                            contentColor = badgeFg
                                                        )
                                                    }
                                                }
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
                            depth = 0,
                            catalog = store.currentCatalog
                        )
                    }
                }
            }
        }
    }
} else {
                // Collapsed slim rail body (Parts / Layers quick access)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = {
                            selectedTab = 0
                            actualToggle()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        StudioIcon(
                            imageVector = Icons.Default.Widgets,
                            contentDescription = "Parts",
                            tint = if (selectedTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            size = 20.dp
                        )
                    }
                    IconButton(
                        onClick = {
                            selectedTab = 1
                            actualToggle()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        StudioIcon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "Layers",
                            tint = if (selectedTab == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            size = 20.dp
                        )
                    }
                }
            }
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
    slotName: String? = null,
    catalog: ComponentCatalog = ComponentCatalog()
) {
    val isSelected = node.id == selectedNodeId
    val componentDef = catalog.components.find { it.id == node.catalogId }
    val definedSlots = componentDef?.slotDefinitions.orEmpty()

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
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f, fill = false)
                )
                StudioBadge(
                    text = node.category.name,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Render slots and their children
        val allSlotNames = (definedSlots.map { it.name } + node.slots.keys).distinct()
        allSlotNames.forEach { currentSlot ->
            val slotDef = definedSlots.find { it.name == currentSlot }
            val children = node.slots[currentSlot].orEmpty()

            if (children.isNotEmpty()) {
                children.forEach { child ->
                    LiveLayersTree(
                        node = child,
                        selectedNodeId = selectedNodeId,
                        onSelectNode = onSelectNode,
                        depth = depth + 1,
                        slotName = currentSlot,
                        catalog = catalog
                    )
                }
            } else if (slotDef != null && slotDef.cardinality == SlotCardinality.SINGLE) {
                // Show vacant single slot indicator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = ((depth + 1) * 12 + 8).dp, top = 2.dp, bottom = 2.dp)
                        .clickable(role = androidx.compose.ui.semantics.Role.Button) { onSelectNode(node.id) },
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$currentSlot: (Empty 0/1)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                    )
                }
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
