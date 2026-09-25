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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.chandu4221.composestudio.theme.AppTheme

/**
 * Standardized SegmentedControl molecule with consistent border, separators, and polished active pill.
 */
@Composable
fun SegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    equalWidth: Boolean = true,
    shape: Shape = MaterialTheme.shapes.small
) {
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
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant
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
