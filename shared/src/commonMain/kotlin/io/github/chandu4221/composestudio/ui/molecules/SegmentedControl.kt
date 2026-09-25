package io.github.chandu4221.composestudio.ui.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.chandu4221.composestudio.theme.AppTheme

/**
 * Standardized SegmentedControl molecule with consistent border, separators, and polished active pill.
 * Responsive padding and typography based on item count to avoid text wrapping.
 */
@Composable
fun SegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    labels: List<String> = options,
    equalWidth: Boolean = true,
    shape: Shape = MaterialTheme.shapes.small
) {
    val hPadding = when {
        options.size >= 4 -> 4.dp
        options.size == 3 -> 6.dp
        else -> 10.dp
    }
    val typography = when {
        options.size >= 4 -> MaterialTheme.typography.labelSmall
        else -> MaterialTheme.typography.labelMedium
    }

    Row(
        modifier = modifier
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = shape
            )
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .padding(2.dp)
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = index == selectedIndex
            val labelText = labels.getOrElse(index) { option }

            // Vertical separator between adjacent unselected items
            if (index > 0 && index != selectedIndex && index - 1 != selectedIndex) {
                VerticalDivider(
                    modifier = Modifier
                        .height(14.dp)
                        .width(1.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }

            val itemModifier = if (equalWidth) Modifier.weight(1f) else Modifier

            Box(
                modifier = itemModifier
                    .clip(shape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else Color.Transparent
                    )
                    .selectable(
                        selected = isSelected,
                        role = androidx.compose.ui.semantics.Role.Tab,
                        onClick = { onSelect(index) }
                    )
                    .padding(horizontal = hPadding, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = labelText,
                    style = typography,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SegmentedControlPreview() {
    AppTheme {
        SegmentedControl(
            options = listOf("Design", "Trigger", "Semantics"),
            selectedIndex = 0,
            onSelect = {},
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        )
    }
}
