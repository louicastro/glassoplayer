package com.liquidglass.music.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liquidglass.music.ui.components.LiquidGlassBox
import com.liquidglass.music.ui.theme.LiquidAccentPrimary
import com.liquidglass.music.ui.theme.LiquidElevation
import com.liquidglass.music.ui.theme.LiquidMotion
import com.liquidglass.music.ui.theme.LiquidRadii
import com.liquidglass.music.ui.theme.LiquidSpacing
import com.liquidglass.music.ui.theme.LocalReducedMotion

enum class NavigationDestination(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    LIBRARY("Library", Icons.Default.LibraryMusic),
    SEARCH("Search", Icons.Default.Search),
    PLAYLISTS("Playlists", Icons.AutoMirrored.Filled.PlaylistPlay)
}

@Composable
fun LiquidNavigationBar(
    currentDestination: NavigationDestination,
    onNavigate: (NavigationDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    LiquidGlassBox(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = LiquidSpacing.lg, vertical = LiquidSpacing.xs),
        shape = RoundedCornerShape(LiquidRadii.xl),
        elevation = LiquidElevation.floating
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = LiquidSpacing.sm, vertical = LiquidSpacing.xs + 2.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavigationDestination.entries.forEach { destination ->
                val isSelected = currentDestination == destination
                NavigationItemPill(
                    destination = destination,
                    isSelected = isSelected,
                    onClick = { onNavigate(destination) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun NavigationItemPill(
    destination: NavigationDestination,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val isReducedMotion = LocalReducedMotion.current
    val interactionSource = remember { MutableInteractionSource() }

    val iconColor by animateColorAsState(
        targetValue = if (isSelected) LiquidAccentPrimary else (if (isDark) Color(0x99FFFFFF) else Color(0x992B3045)),
        animationSpec = tween(LiquidMotion.durationNormal),
        label = "navIconColor"
    )

    val labelColor by animateColorAsState(
        targetValue = if (isSelected) LiquidAccentPrimary else (if (isDark) Color(0x80FFFFFF) else Color(0x802B3045)),
        animationSpec = tween(LiquidMotion.durationNormal),
        label = "navLabelColor"
    )

    val pillBackground by animateColorAsState(
        targetValue = if (isSelected) LiquidAccentPrimary.copy(alpha = if (isDark) 0.16f else 0.12f) else Color.Transparent,
        animationSpec = tween(LiquidMotion.durationNormal),
        label = "navPillBg"
    )

    val scale by animateFloatAsState(
        targetValue = if (isSelected && !isReducedMotion) 1.05f else 1.0f,
        animationSpec = tween(LiquidMotion.durationNormal),
        label = "navItemScale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(LiquidRadii.md))
            .background(pillBackground)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = LiquidSpacing.xs + 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = destination.icon,
                contentDescription = destination.label,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.height(LiquidSpacing.xxs))
            Text(
                text = destination.label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    letterSpacing = 0.2.sp
                ),
                color = labelColor
            )
        }
    }
}
