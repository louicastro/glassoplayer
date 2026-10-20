package com.liquidglass.music.ui.components

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.liquidglass.music.ui.theme.GlassDarkBorder
import com.liquidglass.music.ui.theme.GlassDarkSurface
import com.liquidglass.music.ui.theme.GlassLightBorder
import com.liquidglass.music.ui.theme.GlassLightSurface
import com.liquidglass.music.ui.theme.glassBorderBrush

@Composable
fun LiquidGlassBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    blurRadius: Dp = 24.dp,
    tintColor: Color? = null,
    borderWidth: Dp = 1.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val baseSurface = if (isDark) GlassDarkSurface else GlassLightSurface
    val finalBgColor = tintColor ?: baseSurface

    val borderBrush = glassBorderBrush(isDark)

    Box(
        modifier = modifier
            .clip(shape)
            .then(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && blurRadius > 0.dp) {
                    Modifier.graphicsLayer {
                        renderEffect = RenderEffect
                            .createBlurEffect(
                                blurRadius.toPx(),
                                blurRadius.toPx(),
                                Shader.TileMode.CLAMP
                            )
                            .asComposeRenderEffect()
                    }
                } else {
                    Modifier
                }
            )
            .background(finalBgColor, shape)
            .border(borderWidth, borderBrush, shape),
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
                listOf(Color(0x33FFFFFF), Color(0x1A1B22), Color(0x2E1B22))
            } else {
                listOf(Color(0xE6FFFFFF), Color(0xCCF0F4F8))
            }
        ),
        shape = shape
    )
    .border(borderWidth, glassBorderBrush(isDark), shape)
