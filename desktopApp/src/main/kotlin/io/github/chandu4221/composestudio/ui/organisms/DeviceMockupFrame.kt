package io.github.chandu4221.composestudio.ui.organisms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.chandu4221.composestudio.theme.AppTheme
import io.github.chandu4221.composestudio.ui.atoms.StudioBadge
import io.github.chandu4221.composestudio.ui.atoms.StudioIcon

/**
 * Mobile device frame mockup organism representing the target preview canvas.
 */
@Composable
fun DeviceMockupFrame(
    modifier: Modifier = Modifier
) {
    val phoneShape = RoundedCornerShape(32.dp)

    Surface(
        modifier = modifier
            .width(340.dp)
            .height(680.dp)
            .border(width = 2.dp, color = MaterialTheme.colorScheme.outlineVariant, shape = phoneShape),
        shape = phoneShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "9:30",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Camera punch hole
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF334155))
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    StudioIcon(Icons.Default.Wifi, "Wifi", size = 14.dp, tint = MaterialTheme.colorScheme.onSurface)
                    StudioIcon(Icons.Default.BatteryFull, "Battery", size = 14.dp, tint = MaterialTheme.colorScheme.onSurface)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // App Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StudioIcon(Icons.Default.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface)
                Text(
                    text = "Compose App",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                StudioIcon(Icons.Default.MoreVert, "More", tint = MaterialTheme.colorScheme.onSurface)
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Hero Placeholder illustration
            Box(
                modifier = Modifier
                    .size(120.dp, 80.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                StudioIcon(
                    imageVector = Icons.Default.Image,
                    contentDescription = "Illustration",
                    tint = MaterialTheme.colorScheme.primary,
                    size = 40.dp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Content Headings
            Text(
                text = "Welcome to Compose",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Build beautiful apps with Jetpack Compose and Material 3.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Selected Button with Bounding Box & Badge
            Box(contentAlignment = Alignment.TopStart) {
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(4.dp)
                ) {
                    Button(
                        onClick = {},
                        shape = MaterialTheme.shapes.small,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = "Get Started",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                // Component Tag Badge "Button"
                StudioBadge(
                    text = "Button",
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bottom Home Bar Indicator
            Box(
                modifier = Modifier
                    .size(width = 80.dp, height = 4.dp)
                    .clip(MaterialTheme.shapes.extraLarge)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DeviceMockupFramePreview() {
    AppTheme {
        DeviceMockupFrame()
    }
}
