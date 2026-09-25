package io.github.chandu4221.composestudio.ui.templates

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
 * Supports interactive resizing for left and right panels with min/max constraints.
 */
@Composable
fun StudioEditorTemplate(
    modifier: Modifier = Modifier,
    store: StudioStore = remember { StudioStore() }
) {
    var leftPanelWidth by remember { mutableStateOf(280.dp) }
    var rightPanelWidth by remember { mutableStateOf(360.dp) }

    CompositionLocalProvider(LocalStudioStore provides store) {
        Column(modifier = modifier.fillMaxSize()) {
            // Top Navigation Bar
            StudioTopBar()

            // 3-Pane Body: Left Panel | Center Viewport Canvas | Right Inspector
            Row(modifier = Modifier.fillMaxSize()) {
                LeftComponentsPanel(
                    modifier = Modifier.width(leftPanelWidth)
                )

                PanelSplitter(
                    onResize = { deltaDp ->
                        leftPanelWidth = (leftPanelWidth + deltaDp).coerceIn(220.dp, 520.dp)
                    }
                )

                CanvasViewport(modifier = Modifier.weight(1f))

                PanelSplitter(
                    onResize = { deltaDp ->
                        rightPanelWidth = (rightPanelWidth - deltaDp).coerceIn(280.dp, 640.dp)
                    }
                )

                RightInspectorPanel(
                    modifier = Modifier.width(rightPanelWidth)
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
