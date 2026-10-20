package com.liquidglass.music.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.liquidglass.music.ui.theme.GlassDarkBorder
import com.liquidglass.music.ui.theme.GlassDarkSurface
import com.liquidglass.music.ui.theme.GlassDarkSurfaceElevated
import com.liquidglass.music.ui.theme.GlassLightBorder
import com.liquidglass.music.ui.theme.GlassLightSurface
import com.liquidglass.music.ui.theme.GlassLightSurfaceElevated
import com.liquidglass.music.ui.theme.LiquidElevation
import com.liquidglass.music.ui.theme.LiquidMotion
import com.liquidglass.music.ui.theme.LiquidRadii
import com.liquidglass.music.ui.theme.LiquidSpacing
import com.liquidglass.music.ui.theme.LocalReducedMotion
import com.liquidglass.music.ui.theme.glassBevelBrush
import com.liquidglass.music.ui.theme.glassBorderBrush

/**
 * iOS 26 Liquid Glass Container.
 * Renders a translucent, specular glass surface with refractive bevel highlights.
 * Child contents remain 100% sharp and readable.
 */
@Composable
fun LiquidGlassBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(LiquidRadii.lg),
    blurRadius: Dp = 20.dp,
    tintColor: Color? = null,
    borderWidth: Dp = 0.8.dp,
    elevation: Dp = LiquidElevation.flat,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val baseSurface = if (isDark) GlassDarkSurface else GlassLightSurface
    val finalBgColor = tintColor ?: baseSurface
    val borderBrush = glassBorderBrush(isDark)
    val bevelBrush = glassBevelBrush(isDark)

    val shadowModifier = if (elevation > 0.dp) {
        Modifier.shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = if (isDark) Color(0x33000000) else Color(0x1A000000),
            spotColor = if (isDark) Color(0x4D000000) else Color(0x26000000)
        )
    } else Modifier

    Box(
        modifier = modifier
            .then(shadowModifier)
            .clip(shape)
            .background(finalBgColor, shape)
            .background(bevelBrush, shape)
            .border(borderWidth, borderBrush, shape),
        content = content
    )
}

/**
 * Elevated Liquid Glass Card with subtle elevation depth and optional click listener.
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(LiquidRadii.lg),
    onClick: (() -> Unit)? = null,
    tintColor: Color? = null,
    borderWidth: Dp = 0.8.dp,
    elevation: Dp? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val isReducedMotion = LocalReducedMotion.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null && !isReducedMotion) 0.98f else 1.0f,
        animationSpec = tween(durationMillis = LiquidMotion.durationFast),
        label = "glassCardScale"
    )

    val surfaceColor = tintColor ?: if (isDark) GlassDarkSurfaceElevated else GlassLightSurfaceElevated
    val clickModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    } else Modifier

    val finalElevation = elevation ?: if (isDark) LiquidElevation.subtle else LiquidElevation.medium

    LiquidGlassBox(
        modifier = modifier
            .scale(scale)
            .then(clickModifier),
        shape = shape,
        tintColor = surfaceColor,
        borderWidth = borderWidth,
        elevation = finalElevation,
        content = content
    )
}

/**
 * Compact Pill for badges, category chips, and audio quality tags.
 */
@Composable
fun LiquidGlassPill(
    modifier: Modifier = Modifier,
    tintColor: Color? = null,
    borderColor: Color? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val shape = RoundedCornerShape(LiquidRadii.pill)
    val baseColor = tintColor ?: if (isDark) Color(0x33FFFFFF) else Color(0x26000000)

    Box(
        modifier = modifier
            .clip(shape)
            .background(baseColor, shape)
            .then(
                if (borderColor != null) {
                    Modifier.border(0.75.dp, borderColor, shape)
                } else {
                    Modifier.border(0.75.dp, glassBorderBrush(isDark), shape)
                }
            )
            .padding(horizontal = LiquidSpacing.sm, vertical = LiquidSpacing.xs),
        contentAlignment = Alignment.Center,
        content = content
    )
}

/**
 * Tactile Liquid Glass Interactive Button.
 */
@Composable
fun LiquidGlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(LiquidRadii.md),
    tintColor: Color? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val isReducedMotion = LocalReducedMotion.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && !isReducedMotion) 0.94f else 1.0f,
        animationSpec = tween(durationMillis = LiquidMotion.durationFast),
        label = "glassButtonScale"
    )

    LiquidGlassBox(
        modifier = modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = shape,
        tintColor = tintColor,
        content = content
    )
}

/**
 * Convenience modifier to apply liquid glass styling to any layout.
 */
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(16.dp),
    isDark: Boolean = true,
    borderWidth: Dp = 1.dp
): Modifier = this
    .clip(shape)
    .background(
        brush = Brush.verticalGradient(
            colors = if (isDark) {
                listOf(Color(0x33FFFFFF), Color(0xFF141624), Color(0xFF1C1E30))
            } else {
                listOf(Color(0xF0FFFFFF), Color(0xDDF0F4F8))
            }
        ),
        shape = shape
    )
    .border(borderWidth, glassBorderBrush(isDark), shape)
