package io.github.chandu4221.composestudio

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.chandu4221.composestudio.theme.AppTheme
import io.github.chandu4221.composestudio.ui.templates.StudioEditorTemplate

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "ComposeStudio",
    ) {
        AppTheme {
            Surface(color = MaterialTheme.colorScheme.background) {
                StudioEditorTemplate()
            }
        }
    }
}