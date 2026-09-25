package io.github.chandu4221.composestudio.ui.templates

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import io.github.chandu4221.composestudio.state.LocalStudioStore
import io.github.chandu4221.composestudio.state.StudioStore
import io.github.chandu4221.composestudio.theme.AppTheme
import io.github.chandu4221.composestudio.ui.molecules.PanelSplitter
import io.github.chandu4221.composestudio.ui.molecules.SidebarPosition
import io.github.chandu4221.composestudio.ui.molecules.SidebarTrigger
import io.github.chandu4221.composestudio.ui.organisms.CanvasViewport
import io.github.chandu4221.composestudio.ui.organisms.LeftComponentsPanel
import io.github.chandu4221.composestudio.ui.organisms.RightInspectorPanel
import io.github.chandu4221.composestudio.ui.organisms.StudioTopBar

/**
 * Main application layout template assembling all primary organisms.
 * Injects [StudioStore] via [LocalStudioStore] for unidirectional data flow.
 * Supports interactive resizing for left and right panels with min/max constraints,
 * and shadcn-style collapsible sidebar toggling with animated transitions.
 */
@Composable
fun StudioEditorTemplate(
    modifier: Modifier = Modifier,
    store: StudioStore = remember { StudioStore() }
) {
    var isLeftPanelOpen by remember { mutableStateOf(true) }
    var isRightPanelOpen by remember { mutableStateOf(true) }
    var leftPanelWidth by remember { mutableStateOf(280.dp) }
    var rightPanelWidth by remember { mutableStateOf(360.dp) }

    CompositionLocalProvider(LocalStudioStore provides store) {
        Column(modifier = modifier.fillMaxSize()) {
            // Top Navigation Bar with Sidebar Triggers
            StudioTopBar(
                isLeftPanelOpen = isLeftPanelOpen,
                onToggleLeftPanel = { isLeftPanelOpen = !isLeftPanelOpen },
                isRightPanelOpen = isRightPanelOpen,
                onToggleRightPanel = { isRightPanelOpen = !isRightPanelOpen }
            )

            // 3-Pane Body: Collapsible Left Panel | Center Viewport Canvas | Collapsible Right Inspector
            Row(modifier = Modifier.fillMaxSize()) {
                // Left Panel with Splitter
                AnimatedVisibility(
                    visible = isLeftPanelOpen,
                    enter = expandHorizontally(),
                    exit = shrinkHorizontally()
                ) {
                    Row {
                        LeftComponentsPanel(
                            modifier = Modifier.width(leftPanelWidth)
                        )

                        PanelSplitter(
                            onResize = { deltaDp ->
                                leftPanelWidth = (leftPanelWidth + deltaDp).coerceIn(240.dp, 520.dp)
                            }
                        )
                    }
                }

                // Center Canvas Area with floating edge triggers when sidebars are collapsed
                Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                    CanvasViewport()

                    // Floating Edge Trigger for Left Sidebar (when collapsed)
                    if (!isLeftPanelOpen) {
                        SidebarTrigger(
                            isOpen = false,
                            onToggle = { isLeftPanelOpen = true },
                            position = SidebarPosition.Left,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp)
                        )
                    }

                    // Floating Edge Trigger for Right Sidebar (when collapsed)
                    if (!isRightPanelOpen) {
                        SidebarTrigger(
                            isOpen = false,
                            onToggle = { isRightPanelOpen = true },
                            position = SidebarPosition.Right,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                        )
                    }
                }

                // Right Inspector with Splitter
                AnimatedVisibility(
                    visible = isRightPanelOpen,
                    enter = expandHorizontally(expandFrom = Alignment.End),
                    exit = shrinkHorizontally(shrinkTowards = Alignment.End)
                ) {
                    Row {
                        PanelSplitter(
                            onResize = { deltaDp ->
                                rightPanelWidth = (rightPanelWidth - deltaDp).coerceIn(320.dp, 640.dp)
                            }
                        )

                        RightInspectorPanel(
                            modifier = Modifier.width(rightPanelWidth)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StudioEditorTemplatePreview() {
    AppTheme {
        StudioEditorTemplate()
    }
}
