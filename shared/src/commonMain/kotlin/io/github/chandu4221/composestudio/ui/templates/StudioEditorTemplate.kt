package io.github.chandu4221.composestudio.ui.templates

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.github.chandu4221.composestudio.state.LocalStudioStore
import io.github.chandu4221.composestudio.state.StudioStore
import io.github.chandu4221.composestudio.theme.AppTheme
import io.github.chandu4221.composestudio.ui.organisms.CanvasViewport
import io.github.chandu4221.composestudio.ui.organisms.LeftComponentsPanel
import io.github.chandu4221.composestudio.ui.organisms.RightInspectorPanel
import io.github.chandu4221.composestudio.ui.organisms.StudioTopBar

/**
 * Main application layout template assembling all primary organisms.
 * Injects [StudioStore] via [LocalStudioStore] for unidirectional data flow.
 */
@Composable
fun StudioEditorTemplate(
    modifier: Modifier = Modifier,
    store: StudioStore = remember { StudioStore() }
) {
    CompositionLocalProvider(LocalStudioStore provides store) {
        Column(modifier = modifier.fillMaxSize()) {
            // Top Navigation Bar
            StudioTopBar()

            // 3-Pane Body: Left Panel | Center Viewport Canvas | Right Inspector
            Row(modifier = Modifier.fillMaxSize()) {
                LeftComponentsPanel()

                CanvasViewport(modifier = Modifier.weight(1f))

                RightInspectorPanel()
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
