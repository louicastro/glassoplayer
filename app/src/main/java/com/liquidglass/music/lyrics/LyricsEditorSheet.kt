package com.liquidglass.music.lyrics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.PlayArrow
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
import com.liquidglass.music.ui.theme.LiquidAccentPrimary

@Composable
fun LyricsEditorSheet(
    editorLines: List<LyricLine>,
    currentPlaybackPositionMs: Long,
    onStampLine: (Int, Long) -> Unit,
    onSaveEdits: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Tap to Stamp, 1 = Raw LRC Editor
    var rawTextContent by remember {
        mutableStateOf(LrcParser.serializeToLrc(editorLines))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D0F18))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCancel) {
                    Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.White)
                }

                Text(
                    text = "Lyrics Editor",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                IconButton(onClick = onSaveEdits) {
                    Icon(Icons.Default.Check, contentDescription = "Save", tint = LiquidAccentPrimary)
                }
            }

            Spacer(Modifier.height(12.dp))

            // Tabs: Tap to Stamp vs Raw Text Editor
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = Color.White,
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
                    text = { Text("Tap to Stamp") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        rawTextContent = LrcParser.serializeToLrc(editorLines)
                        selectedTab = 1
                    },
                    text = { Text("Raw LRC Text") }
                )
            }

            Spacer(Modifier.height(16.dp))

            if (selectedTab == 0) {
                // Real-time "Tap to Stamp" mode
                Text(
                    text = "Tap the 'Stamp' button on each line as the audio reaches it:",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.7f)),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(editorLines) { index, line ->
                        LiquidGlassBox(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
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
                                            color = if (line.timestampMs > 0) LiquidAccentPrimary else Color.White.copy(alpha = 0.5f),
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = line.text,
                                        style = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
                                    )
                                }

                                Spacer(Modifier.width(8.dp))

                                // Stamp button
                                Button(
                                    onClick = { onStampLine(index, currentPlaybackPositionMs) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (line.timestampMs > 0) Color(0x336C7CFF) else LiquidAccentPrimary
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TouchApp,
                                        contentDescription = "Stamp",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text("Stamp")
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
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = LiquidAccentPrimary,
                        unfocusedBorderColor = Color(0x33FFFFFF)
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
                )
            }
        }
    }
}
