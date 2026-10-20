package com.liquidglass.music.lyrics

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liquidglass.music.ui.components.LiquidGlassBox
import com.liquidglass.music.ui.components.LiquidGlassCard
import com.liquidglass.music.ui.theme.GlassDarkBackdrop
import com.liquidglass.music.ui.theme.GlassLightBackdrop
import com.liquidglass.music.ui.theme.LiquidAccentPrimary
import com.liquidglass.music.ui.theme.LiquidRadii
import com.liquidglass.music.ui.theme.LiquidSpacing
import com.liquidglass.music.ui.theme.TextDarkPrimary
import com.liquidglass.music.ui.theme.TextDarkSecondary
import com.liquidglass.music.ui.theme.TextLightPrimary
import com.liquidglass.music.ui.theme.TextLightSecondary

@Composable
fun LyricsEditorSheet(
    editorLines: List<LyricLine>,
    currentPlaybackPositionMs: Long,
    onStampLine: (Int, Long) -> Unit,
    onSaveEdits: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Tap to Stamp, 1 = Raw LRC Editor
    var rawTextContent by remember {
        mutableStateOf(LrcParser.serializeToLrc(editorLines))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDark) GlassDarkBackdrop else GlassLightBackdrop)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(LiquidSpacing.lg)
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0x26FFFFFF) else Color(0x1A000000))
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Cancel",
                        tint = if (isDark) Color.White else Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = "Lyrics Timing Editor",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp
                    ),
                    color = if (isDark) TextDarkPrimary else TextLightPrimary
                )

                IconButton(
                    onClick = onSaveEdits,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(LiquidAccentPrimary)
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Save",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.height(LiquidSpacing.md))

            // Tabs: Tap to Stamp vs Raw Text Editor
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = LiquidAccentPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = LiquidAccentPrimary
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "Tap to Stamp",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 0) LiquidAccentPrimary else (if (isDark) TextDarkSecondary else TextLightSecondary)
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        rawTextContent = LrcParser.serializeToLrc(editorLines)
                        selectedTab = 1
                    },
                    text = {
                        Text(
                            "Raw LRC Text",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 1) LiquidAccentPrimary else (if (isDark) TextDarkSecondary else TextLightSecondary)
                        )
                    }
                )
            }

            Spacer(Modifier.height(LiquidSpacing.md))

            if (selectedTab == 0) {
                // Real-time "Tap to Stamp" mode
                Text(
                    text = "Tap 'Stamp' on each line as audio reaches it to record timestamps:",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) TextDarkSecondary else TextLightSecondary,
                    modifier = Modifier.padding(bottom = LiquidSpacing.sm)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(LiquidSpacing.sm)
                ) {
                    itemsIndexed(editorLines) { index, line ->
                        LiquidGlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(LiquidRadii.md)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = LiquidSpacing.md, vertical = LiquidSpacing.sm + 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    val formattedTime = if (line.timestampMs > 0) {
                                        val sec = line.timestampMs / 1000
                                        "%02d:%02d.%02d".format(sec / 60, sec % 60, (line.timestampMs % 1000) / 10)
                                    } else {
                                        "--:--.--"
                                    }
                                    Text(
                                        text = formattedTime,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (line.timestampMs > 0) LiquidAccentPrimary else (if (isDark) Color(0x66FFFFFF) else Color(0x66000000)),
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = line.text,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                        color = if (isDark) TextDarkPrimary else TextLightPrimary
                                    )
                                }

                                Spacer(Modifier.width(LiquidSpacing.sm))

                                // Stamp button
                                Button(
                                    onClick = { onStampLine(index, currentPlaybackPositionMs) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (line.timestampMs > 0) LiquidAccentPrimary.copy(alpha = 0.25f) else LiquidAccentPrimary,
                                        contentColor = if (line.timestampMs > 0) LiquidAccentPrimary else Color.White
                                    ),
                                    shape = RoundedCornerShape(LiquidRadii.pill),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TouchApp,
                                        contentDescription = "Stamp",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(LiquidSpacing.xs))
                                    Text("Stamp", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            } else {
                // Raw LRC text editor mode
                OutlinedTextField(
                    value = rawTextContent,
                    onValueChange = { rawTextContent = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = if (isDark) TextDarkPrimary else TextLightPrimary,
                        unfocusedTextColor = if (isDark) TextDarkPrimary else TextLightPrimary,
                        focusedBorderColor = LiquidAccentPrimary,
                        unfocusedBorderColor = if (isDark) Color(0x33FFFFFF) else Color(0x33000000)
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
                )
            }
        }
    }
}
