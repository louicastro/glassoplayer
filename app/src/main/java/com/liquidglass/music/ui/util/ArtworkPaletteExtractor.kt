package com.liquidglass.music.ui.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

data class ExtractedColors(
    val dominant: Color,
    val surfaceTint: Color,
    val accent: Color
)

object ArtworkPaletteExtractor {

    private val defaultColorsDark = ExtractedColors(
        dominant = Color(0xFF131524),
        surfaceTint = Color(0x332B304C),
        accent = Color(0xFF7282FF)
    )

    suspend fun extractColorsFromUri(context: Context, uriString: String?): ExtractedColors = withContext(Dispatchers.IO) {
        if (uriString == null) return@withContext defaultColorsDark

        try {
            val uri = Uri.parse(uriString)
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap != null) {
                val palette = Palette.from(bitmap)
                    .maximumColorCount(16)
                    .generate()

                val dominantInt = palette.getDarkVibrantColor(
                    palette.getDominantColor(0xFF131524.toInt())
                )
                val accentInt = palette.getVibrantColor(
                    palette.getLightVibrantColor(0xFF7282FF.toInt())
                )

                val dominant = Color(dominantInt)
                val accent = Color(accentInt)
                val surfaceTint = dominant.copy(alpha = 0.45f)

                bitmap.recycle()
                return@withContext ExtractedColors(
                    dominant = dominant,
                    surfaceTint = surfaceTint,
                    accent = accent
                )
            }
        } catch (e: Exception) {
            // Fallback gracefully on unreadable images
        }

        defaultColorsDark
    }
}
