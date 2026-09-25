package io.github.chandu4221.composestudio.ui.molecules

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewSidebar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.chandu4221.composestudio.ui.atoms.StudioIcon

enum class SidebarPosition {
    Left, Right
}

/**
 * shadcn-inspired SidebarTrigger button used to expand and collapse side panels.
 * Features smooth active background highlight, hover effects, and automatic icon orientation.
 */
@Composable
fun SidebarTrigger(
    isOpen: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    position: SidebarPosition = SidebarPosition.Left,
    customIcon: ImageVector? = null,
    size: Dp = 32.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val targetBgColor = when {
        isOpen && isHovered -> MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
        isOpen -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        isHovered -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        else -> MaterialTheme.colorScheme.surface
    }
    val backgroundColor by animateColorAsState(targetBgColor)

    val targetBorderColor = when {
        isOpen -> MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        isHovered -> MaterialTheme.colorScheme.outline
        else -> MaterialTheme.colorScheme.outlineVariant
    }
    val borderColor by animateColorAsState(targetBorderColor)

    val iconTint by animateColorAsState(
        if (isOpen) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurfaceVariant
    )

    Surface(
        modifier = modifier
            .size(size)
            .clip(MaterialTheme.shapes.small)
            .hoverable(interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onToggle
            ),
        shape = MaterialTheme.shapes.small,
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            val resolvedIcon = customIcon ?: Icons.AutoMirrored.Filled.ViewSidebar
            val iconModifier = if (position == SidebarPosition.Right && customIcon == null) {
                Modifier.scale(scaleX = -1f, scaleY = 1f)
            } else {
                Modifier
            }

            StudioIcon(
                imageVector = resolvedIcon,
                contentDescription = if (position == SidebarPosition.Left) "Toggle Left Sidebar" else "Toggle Right Sidebar",
                tint = iconTint,
                size = 18.dp,
                modifier = iconModifier
            )
        }
    }
}
