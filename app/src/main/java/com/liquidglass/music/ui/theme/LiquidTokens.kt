package com.liquidglass.music.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * iOS 26 Liquid Glass Design System Tokens
 * Strict geometric scales for spacing, corner radii, elevation, and motion.
 */
object LiquidSpacing {
    val xxs: Dp = 2.dp
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 16.dp
    val xl: Dp = 20.dp
    val xxl: Dp = 24.dp
    val xxxl: Dp = 32.dp
    val huge: Dp = 40.dp
}

object LiquidRadii {
    val xs: Dp = 6.dp
    val sm: Dp = 10.dp
    val md: Dp = 16.dp
    val lg: Dp = 20.dp
    val xl: Dp = 26.dp
    val xxl: Dp = 32.dp
    val sheet: Dp = 36.dp
    val pill: Dp = 999.dp
}

object LiquidElevation {
    val flat: Dp = 0.dp
    val subtle: Dp = 2.dp
    val medium: Dp = 6.dp
    val floating: Dp = 12.dp
    val modal: Dp = 24.dp
}

object LiquidMotion {
    const val durationFast: Int = 150
    const val durationNormal: Int = 260
    const val durationExpanded: Int = 380
    const val durationSheet: Int = 450

    val easeEmphasized: Easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    val easeStandard: Easing = CubicBezierEasing(0.3f, 0.0f, 0.2f, 1.0f)

    fun <T> springBouncy() = spring<T>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )

    fun <T> springSmooth() = spring<T>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    )
}
