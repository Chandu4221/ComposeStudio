package io.github.chandu4221.composestudio.ui.molecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.chandu4221.composestudio.theme.AppTheme

/**
 * Discrete step slider molecule (e.g. XS, S, M, L, XL sizes in the Inspector).
 */
@Composable
fun SizeStepSlider(
    steps: List<String>,
    selectedIndex: Int,
    onStepSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val maxIndex = (steps.size - 1).coerceAtLeast(1)
    val currentStepName = steps.getOrNull(selectedIndex) ?: ""

    Column(modifier = modifier.fillMaxWidth()) {
        Slider(
            value = selectedIndex.toFloat(),
            onValueChange = { onStepSelected(it.toInt()) },
            valueRange = 0f..maxIndex.toFloat(),
            steps = if (steps.size > 2) steps.size - 2 else 0,
            modifier = Modifier.semantics {
                contentDescription = "Component Size"
                stateDescription = currentStepName
            },
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
            )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            steps.forEachIndexed { index, label ->
                val isSelected = index == selectedIndex
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SizeStepSliderPreview() {
    AppTheme {
        SizeStepSlider(
            steps = listOf("XS", "S", "M", "L", "XL"),
            selectedIndex = 2,
            onStepSelected = {}
        )
    }
}
