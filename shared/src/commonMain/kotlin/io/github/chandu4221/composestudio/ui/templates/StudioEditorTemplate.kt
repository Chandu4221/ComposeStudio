package io.github.chandu4221.composestudio.ui.templates

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import io.github.chandu4221.composestudio.state.LocalStudioStore
import io.github.chandu4221.composestudio.state.StudioStore
import io.github.chandu4221.composestudio.theme.AppTheme
import io.github.chandu4221.composestudio.ui.molecules.PanelSplitter
import io.github.chandu4221.composestudio.ui.organisms.CanvasViewport
import io.github.chandu4221.composestudio.ui.organisms.LeftComponentsPanel
import io.github.chandu4221.composestudio.ui.organisms.RightInspectorPanel
import io.github.chandu4221.composestudio.ui.organisms.StudioTopBar

/**
 * Main application layout template assembling all primary organisms.
 * Injects [StudioStore] via [LocalStudioStore] for unidirectional data flow.
 * Supports interactive resizing for left and right panels with min/max constraints,
 * and fixed-position sidebar toggling with smooth collapse to a 48dp rail.
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

    val animatedLeftWidth by animateDpAsState(
        targetValue = if (isLeftPanelOpen) leftPanelWidth else 48.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
    )
    val animatedRightWidth by animateDpAsState(
        targetValue = if (isRightPanelOpen) rightPanelWidth else 48.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
    )

    CompositionLocalProvider(LocalStudioStore provides store) {
        Column(modifier = modifier.fillMaxSize()) {
            // Top Navigation Bar
            StudioTopBar()

            // 3-Pane Body: Left Panel | Center Viewport Canvas | Right Inspector
            Row(modifier = Modifier.fillMaxSize()) {
                // Left Panel
                LeftComponentsPanel(
                    isOpen = isLeftPanelOpen,
                    onToggle = { isLeftPanelOpen = !isLeftPanelOpen },
                    modifier = Modifier.width(animatedLeftWidth)
                )

                // Resizable splitter only when left panel is open
                if (isLeftPanelOpen) {
                    PanelSplitter(
                        onResize = { deltaDp ->
                            leftPanelWidth = (leftPanelWidth + deltaDp).coerceIn(240.dp, 520.dp)
                        }
                    )
                }

                // Center Canvas Area - Completely clean, NO floating buttons
                Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                    CanvasViewport()
                }

                // Resizable splitter only when right panel is open
                if (isRightPanelOpen) {
                    PanelSplitter(
                        onResize = { deltaDp ->
                            rightPanelWidth = (rightPanelWidth - deltaDp).coerceIn(320.dp, 640.dp)
                        }
                    )
                }

                // Right Inspector
                RightInspectorPanel(
                    isOpen = isRightPanelOpen,
                    onToggle = { isRightPanelOpen = !isRightPanelOpen },
                    modifier = Modifier.width(animatedRightWidth)
                )
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
