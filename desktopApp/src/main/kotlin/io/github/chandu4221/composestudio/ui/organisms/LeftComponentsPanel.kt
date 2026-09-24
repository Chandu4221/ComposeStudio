package io.github.chandu4221.composestudio.ui.organisms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartButton
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material.icons.automirrored.filled.ViewSidebar
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.chandu4221.composestudio.theme.AppTheme
import io.github.chandu4221.composestudio.ui.molecules.AccordionHeader
import io.github.chandu4221.composestudio.ui.molecules.ComponentPaletteItem
import io.github.chandu4221.composestudio.ui.molecules.SearchField
import io.github.chandu4221.composestudio.ui.molecules.SegmentedControl

/**
 * Left panel organism showing the Parts/Layers tabs, component search, and palette sections.
 */
@Composable
fun LeftComponentsPanel(
    modifier: Modifier = Modifier,
    onComponentSelected: (String) -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    var actionsExpanded by remember { mutableStateOf(true) }
    var navExpanded by remember { mutableStateOf(true) }
    var containmentExpanded by remember { mutableStateOf(true) }
    var userComponentsExpanded by remember { mutableStateOf(true) }

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

            // Search
            SearchField(
                query = searchQuery,
                onQueryChange = { searchQuery = it }
            )

            // Section 1: Actions
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AccordionHeader(
                    title = "Actions",
                    isExpanded = actionsExpanded,
                    onToggle = { actionsExpanded = !actionsExpanded }
                )
                if (actionsExpanded) {
                    ComponentPaletteItem("Button", Icons.Default.SmartButton, { onComponentSelected("Button") })
                    ComponentPaletteItem("Floating Action Button", Icons.Default.AddCircle, { onComponentSelected("FAB") })
                    ComponentPaletteItem("Icon Button", Icons.Default.RadioButtonChecked, { onComponentSelected("IconButton") })
                    ComponentPaletteItem("Toggle Button", Icons.Default.ToggleOn, { onComponentSelected("ToggleButton") })
                }
            }

            // Section 2: Navigation
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AccordionHeader(
                    title = "Navigation",
                    isExpanded = navExpanded,
                    onToggle = { navExpanded = !navExpanded }
                )
                if (navExpanded) {
                    ComponentPaletteItem("Bottom Navigation", Icons.Default.Navigation, { onComponentSelected("BottomNav") })
                    ComponentPaletteItem("Navigation Bar", Icons.Default.Menu, { onComponentSelected("NavBar") })
                    ComponentPaletteItem("Drawer", Icons.AutoMirrored.Filled.ViewSidebar, { onComponentSelected("Drawer") })
                    ComponentPaletteItem("Tab Row", Icons.Default.Tab, { onComponentSelected("TabRow") })
                }
            }

            // Section 3: Containment
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AccordionHeader(
                    title = "Containment",
                    isExpanded = containmentExpanded,
                    onToggle = { containmentExpanded = !containmentExpanded }
                )
                if (containmentExpanded) {
                    ComponentPaletteItem("Card", Icons.Default.CreditCard, { onComponentSelected("Card") })
                    ComponentPaletteItem("Surface", Icons.Default.CropSquare, { onComponentSelected("Surface") })
                    ComponentPaletteItem("Scaffold", Icons.Default.Dashboard, { onComponentSelected("Scaffold") })
                    ComponentPaletteItem("Divider", Icons.Default.HorizontalRule, { onComponentSelected("Divider") })
                }
            }

            // Section 4: User Components
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AccordionHeader(
                    title = "User Components",
                    isExpanded = userComponentsExpanded,
                    onToggle = { userComponentsExpanded = !userComponentsExpanded }
                )
                if (userComponentsExpanded) {
                    ComponentPaletteItem("Profile Card", Icons.Default.Person, { onComponentSelected("ProfileCard") })
                    ComponentPaletteItem("Settings Card", Icons.Default.Settings, { onComponentSelected("SettingsCard") })
                    ComponentPaletteItem("User List Item", Icons.Default.Group, { onComponentSelected("UserListItem") })
                    ComponentPaletteItem("Custom Widget", Icons.Default.Widgets, { onComponentSelected("CustomWidget") })
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

@Preview(showBackground = true)
@Composable
private fun LeftComponentsPanelPreview() {
    AppTheme {
        LeftComponentsPanel()
    }
}
