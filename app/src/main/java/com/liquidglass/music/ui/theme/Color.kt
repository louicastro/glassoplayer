package com.liquidglass.music.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Liquid Glass Core Colors (Dark & Light)
val GlassDarkSurface = Color(0xCC12131C)
val GlassDarkSurfaceElevated = Color(0xDD1B1D2B)
val GlassDarkBorder = Color(0x33FFFFFF)
val GlassDarkSpecular = Color(0x55FFFFFF)

val GlassLightSurface = Color(0xDDF2F4F8)
val GlassLightSurfaceElevated = Color(0xEEFFFFFF)
val GlassLightBorder = Color(0x40FFFFFF)
val GlassLightSpecular = Color(0x80FFFFFF)

// Accents
val LiquidAccentPrimary = Color(0xFF6C7CFF)
val LiquidAccentSecondary = Color(0xFF8E9BFF)
val LiquidAccentGlow = Color(0x406C7CFF)

// Accessible Text Colors (WCAG AA >= 4.5:1 against glass backdrops)
val TextDarkPrimary = Color(0xFFFFFFFF)
val TextDarkSecondary = Color(0xCCFFFFFF) // >4.5:1
val TextDarkTertiary = Color(0x99FFFFFF)

val TextLightPrimary = Color(0xFF0F111A)
val TextLightSecondary = Color(0xDD2D3142)
val TextLightTertiary = Color(0xAA4B5166)

// Liquid Bevel Gradients
fun glassBorderBrush(isDark: Boolean): Brush {
    return if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0x4DFFFFFF),
                Color(0x1AFFFFFF),
                Color(0x0DFFFFFF)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0x80FFFFFF),
                Color(0x40FFFFFF),
                Color(0x20FFFFFF)
            )
        )
    }
}
