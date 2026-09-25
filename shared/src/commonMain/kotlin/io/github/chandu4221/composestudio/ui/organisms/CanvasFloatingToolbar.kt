package io.github.chandu4221.composestudio.ui.organisms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.chandu4221.composestudio.theme.AppTheme
import io.github.chandu4221.composestudio.ui.atoms.StudioBadge
import io.github.chandu4221.composestudio.ui.atoms.StudioIcon
import io.github.chandu4221.composestudio.ui.molecules.ZoomControlGroup

/**
 * Floating bottom toolbar organism on the canvas containing AI actions, zoom, and theme controls.
 */
@Composable
fun CanvasFloatingToolbar(
    modifier: Modifier = Modifier,
    onFitToScreen: () -> Unit = {},
    onToggleTheme: () -> Unit = {}
) {
    var zoomLevel by remember { mutableStateOf(62) }

    Row(
        modifier = modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // "Ask AI" Pill
        Surface(
            shape = CircleShape,
            color = Color(0xFFE6F8F3),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBE5D8))
        ) {
            Row(
                modifier = Modifier
                    .clickable {}
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StudioIcon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Ask AI",
                    tint = Color(0xFF007A64)
                )
                Text(
                    text = "Ask AI",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF007A64)
                )
                StudioBadge(
                    text = "BETA",
                    containerColor = Color(0xFF059669),
                    contentColor = Color.White
                )
                StudioIcon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Expand AI",
                    tint = Color(0xFF007A64)
                )
            }
        }

        // "Tidy" Button
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier
                    .clickable {}
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StudioIcon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Tidy",
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Tidy",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Zoom Controls
        ZoomControlGroup(
            zoomPercentage = zoomLevel,
            onZoomIn = { zoomLevel = (zoomLevel + 10).coerceAtMost(200) },
            onZoomOut = { zoomLevel = (zoomLevel - 10).coerceAtLeast(20) }
        )

        // Fit Button
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier
                    .clickable(onClick = onFitToScreen)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StudioIcon(
                    imageVector = Icons.Default.CropFree,
                    contentDescription = "Fit",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Fit",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Theme Toggle Icon
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                .clickable(onClick = onToggleTheme)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            StudioIcon(
                imageVector = Icons.Default.LightMode,
                contentDescription = "Toggle Theme",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CanvasFloatingToolbarPreview() {
    AppTheme {
        CanvasFloatingToolbar()
    }
}
