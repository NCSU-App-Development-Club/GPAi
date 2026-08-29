package org.appdevncsu.gpai.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val BrandPurple = Color(0xFF7876ED)
val BrandDarkPurple = Color(0xFF382540)

@Immutable
data class ExtraColors(
    val success: Color,
    val onSuccess: Color,
    val warning: Color,
    val onWarning: Color,
)

val LightExtraColors = ExtraColors(
    success = Color(0xFF7DC172),
    onSuccess = Color.White,
    warning = Color(0xFFFFC107),
    onWarning = Color.Black,
)

val DarkExtraColors = ExtraColors(
    success = Color(0xFF8BC880),
    onSuccess = Color.Black,
    warning = Color(0xFFFFD54F),
    onWarning = Color.Black,
)

val LocalExtraColors = staticCompositionLocalOf { LightExtraColors }
