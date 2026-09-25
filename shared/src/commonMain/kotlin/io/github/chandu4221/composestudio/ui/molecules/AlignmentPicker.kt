package io.github.chandu4221.composestudio.ui.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.chandu4221.composestudio.theme.AppTheme

/**
 * Accessible full-width 3x3 Alignment matrix picker molecule.
 */
@Composable
fun AlignmentPicker(
    selectedAlignment: Alignment,
    onAlignmentSelected: (Alignment) -> Unit,
    modifier: Modifier = Modifier
) {
    val alignments = listOf(
        listOf(Alignment.TopStart to "Align Top Start", Alignment.TopCenter to "Align Top Center", Alignment.TopEnd to "Align Top End"),
        listOf(Alignment.CenterStart to "Align Center Start", Alignment.Center to "Align Center", Alignment.CenterEnd to "Align Center End"),
        listOf(Alignment.BottomStart to "Align Bottom Start", Alignment.BottomCenter to "Align Bottom Center", Alignment.BottomEnd to "Align Bottom End")
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        alignments.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                row.forEach { (alignment, label) ->
                    val isSelected = alignment == selectedAlignment
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(28.dp)
                            .clip(MaterialTheme.shapes.small)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant,
                                shape = MaterialTheme.shapes.small
                            )
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .semantics {
                                contentDescription = label
                            }
                            .selectable(
                                selected = isSelected,
                                role = Role.RadioButton,
                                onClick = { onAlignmentSelected(alignment) }
                            )
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AlignmentPickerPreview() {
    AppTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            AlignmentPicker(
                selectedAlignment = Alignment.Center,
                onAlignmentSelected = {}
            )
        }
    }
}
