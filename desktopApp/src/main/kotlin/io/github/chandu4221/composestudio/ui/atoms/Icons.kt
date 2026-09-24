package io.github.chandu4221.composestudio.ui.atoms

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.chandu4221.composestudio.theme.AppTheme

/**
 * Standard Studio icon atom with configurable size and color tint.
 */
@Composable
fun StudioIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    size: Dp = 18.dp
) {
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = modifier.size(size),
        tint = tint
    )
}

@Preview(showBackground = true)
@Composable
private fun IconsPreview() {
    AppTheme {
        StudioIcon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search"
        )
    }
}
