package io.github.chandu4221.composestudio.ui.organisms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Splitscreen
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.chandu4221.composestudio.theme.AppTheme
import io.github.chandu4221.composestudio.ui.atoms.StatusDot
import io.github.chandu4221.composestudio.ui.atoms.StudioIcon
import io.github.chandu4221.composestudio.ui.molecules.SegmentedControl

/**
 * Top application navigation bar organism containing editor tools and project details.
 */
@Composable
fun StudioTopBar(
    projectName: String = "My Awesome App",
    saveStatusText: String = "Saved just now",
    modifier: Modifier = Modifier
) {
    var selectedToolIndex by remember { mutableStateOf(0) }
    var selectedModeIndex by remember { mutableStateOf(0) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
            // Left: Compose Studio Logo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    StudioIcon(
                        imageVector = Icons.Default.Widgets,
                        contentDescription = "Compose Studio Logo",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        size = 18.dp
                    )
                }
                Text(
                    text = "Compose Studio",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Center: Editing tools pill
            Surface(
                shape = MaterialTheme.shapes.medium,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant
                ),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TopBarToolIcon(
                        icon = Icons.Default.NearMe,
                        description = "Select",
                        isSelected = selectedToolIndex == 0,
                        onClick = { selectedToolIndex = 0 }
                    )
                    TopBarToolIcon(
                        icon = Icons.Default.PanTool,
                        description = "Pan",
                        isSelected = selectedToolIndex == 1,
                        onClick = { selectedToolIndex = 1 }
                    )

                    VerticalDivider(modifier = Modifier.height(20.dp), color = MaterialTheme.colorScheme.outlineVariant)

                    TopBarToolIcon(
                        icon = Icons.Default.Visibility,
                        description = "Preview",
                        isSelected = selectedToolIndex == 2,
                        onClick = { selectedToolIndex = 2 }
                    )
                    TopBarToolIcon(
                        icon = Icons.Default.Splitscreen,
                        description = "Split View",
                        isSelected = selectedToolIndex == 3,
                        onClick = { selectedToolIndex = 3 }
                    )

                    VerticalDivider(modifier = Modifier.height(20.dp), color = MaterialTheme.colorScheme.outlineVariant)

                    SegmentedControl(
                        options = listOf("Design", "Interactive"),
                        selectedIndex = selectedModeIndex,
                        onSelect = { selectedModeIndex = it },
                        equalWidth = false
                    )

                    VerticalDivider(modifier = Modifier.height(20.dp), color = MaterialTheme.colorScheme.outlineVariant)

                    TopBarToolIcon(icon = Icons.AutoMirrored.Filled.Undo, description = "Undo", onClick = {})
                    TopBarToolIcon(icon = Icons.AutoMirrored.Filled.Redo, description = "Redo", onClick = {})

                    VerticalDivider(modifier = Modifier.height(20.dp), color = MaterialTheme.colorScheme.outlineVariant)

                    TopBarToolIcon(icon = Icons.Default.Palette, description = "Theme", onClick = {})
                    TopBarToolIcon(icon = Icons.Default.Code, description = "Code", onClick = {})
                }
            }

            // Right: User Profile & Cloud Status
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "A",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Column {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = projectName,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        StatusDot()
                    }
                    Text(
                        text = saveStatusText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}
}

@Composable
private fun TopBarToolIcon(
    icon: ImageVector,
    description: String,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else androidx.compose.ui.graphics.Color.Transparent
            )
            .clickable(
                role = androidx.compose.ui.semantics.Role.Button,
                onClickLabel = description,
                onClick = onClick
            )
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        StudioIcon(
            imageVector = icon,
            contentDescription = description,
            tint = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StudioTopBarPreview() {
    AppTheme {
        StudioTopBar()
    }
}
