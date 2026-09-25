package io.github.chandu4221.composestudio.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font

actual val InterFontFamily: FontFamily = FontFamily(
    Font("inter.ttf", FontWeight.Light),
    Font("inter.ttf", FontWeight.Normal),
    Font("inter.ttf", FontWeight.Medium),
    Font("inter.ttf", FontWeight.SemiBold),
    Font("inter.ttf", FontWeight.Bold)
)
