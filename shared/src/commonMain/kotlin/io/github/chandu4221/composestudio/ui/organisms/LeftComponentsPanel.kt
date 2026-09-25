package io.github.chandu4221.composestudio.ui.organisms

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
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import io.github.chandu4221.composestudio.theme.AppTheme
import io.github.chandu4221.composestudio.ui.atoms.StudioBadge
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
 * Left panel organism showing the Parts/Layers tabs, component search, and palette sections
 * dynamically populated from compose-catalog.json.
 */
@Composable
fun LeftComponentsPanel(
    modifier: Modifier = Modifier,
    onComponentSelected: (String) -> Unit = {}
) {
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
                                                onClick = { onComponentSelected(component.id) },
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
                    // Layers view
                    LayersView()
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
 * Placeholder hierarchy view for component layers tab.
 */
@Composable
private fun LayersView() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "COMPONENT TREE",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        ComponentPaletteItem(
            label = "Scaffold (Root)",
            icon = Icons.Default.Dashboard,
            onClick = {}
        )
        ComponentPaletteItem(
            label = "  ↳ TopAppBar",
            icon = Icons.Default.Layers,
            onClick = {}
        )
        ComponentPaletteItem(
            label = "  ↳ Column",
            icon = Icons.Default.ViewAgenda,
            onClick = {}
        )
        ComponentPaletteItem(
            label = "    ↳ Card",
            icon = Icons.Default.AccountTree,
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LeftComponentsPanelPreview() {
    AppTheme {
        LeftComponentsPanel()
    }
}
