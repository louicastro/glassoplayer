package com.liquidglass.music.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Liquid Glass Core Surface Colors (Translucent & Refractive)
val GlassDarkSurface = Color(0xCC131522)
val GlassDarkSurfaceElevated = Color(0xEE1A1D2E)
val GlassDarkBorder = Color(0x2EFFFFFF)
val GlassDarkSpecular = Color(0x59FFFFFF)
val GlassDarkBackdrop = Color(0xFF08090E)

val GlassLightSurface = Color(0xB8FFFFFF)
val GlassLightSurfaceElevated = Color(0xF2F6F8FD)
val GlassLightBorder = Color(0x4DFFFFFF)
val GlassLightSpecular = Color(0x8CFFFFFF)
val GlassLightBackdrop = Color(0xFFF2F4F8)

// Liquid Accent Palette
val LiquidAccentPrimary = Color(0xFF6C7CFF)
val LiquidAccentSecondary = Color(0xFF8E9BFF)
val LiquidAccentGlow = Color(0x406C7CFF)
val LiquidAccentCyan = Color(0xFF38BDF8)
val LiquidAccentPink = Color(0xFFF43F5E)
val LiquidAccentAmber = Color(0xFFF59E0B)
val LiquidAccentEmerald = Color(0xFF10B981)

// High-contrast, WCAG AA Compliant Typography Colors
val TextDarkPrimary = Color(0xFFFFFFFF)
val TextDarkSecondary = Color(0xCCF1F3FB) // 80% opacity
val TextDarkTertiary = Color(0x8CFFFFFF)  // 55% opacity

val TextLightPrimary = Color(0xFF0D0F18)
val TextLightSecondary = Color(0xDD252838)
val TextLightTertiary = Color(0x99525870)

// Liquid Specular Highlight Gradients (Simulates light refraction on curved glass edges)
fun glassBorderBrush(isDark: Boolean): Brush {
    return if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0x59FFFFFF),
                Color(0x1AFFFFFF),
                Color(0x08FFFFFF)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0x99FFFFFF),
                Color(0x4DFFFFFF),
                Color(0x26FFFFFF)
            )
        )
    }
}

fun glassBevelBrush(isDark: Boolean): Brush {
    return if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0x26FFFFFF),
                Color(0x0DFFFFFF),
                Color(0x00FFFFFF)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0x66FFFFFF),
                Color(0x26FFFFFF),
                Color(0x00FFFFFF)
            )
        )
    }
}
