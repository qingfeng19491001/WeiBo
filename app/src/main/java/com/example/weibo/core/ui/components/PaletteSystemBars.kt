package com.example.weibo.core.ui.components

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.size.Size

internal data class PaletteStatusBarResult(
    val backgroundColor: Color,
    val darkIcons: Boolean
)

internal fun shouldUseDarkStatusBarIcons(backgroundColor: Color): Boolean {
    
    return backgroundColor.luminanceCompat() > 0.5f
}

internal fun Color.luminanceCompat(): Float {
    
    return ColorUtils.calculateLuminance(this.toArgb()).toFloat()
}

internal suspend fun computeStatusBarFromPalette(
    context: Context,
    imageModel: Any,
    fallbackColor: Color
): PaletteStatusBarResult {
    val bitmap = loadBitmapWithCoil(context, imageModel) ?: return PaletteStatusBarResult(
        backgroundColor = fallbackColor,
        darkIcons = shouldUseDarkStatusBarIcons(fallbackColor)
    )

    val palette = Palette.from(bitmap).clearFilters().generate()

    val argb =
        palette.dominantSwatch?.rgb
            ?: palette.vibrantSwatch?.rgb
            ?: palette.mutedSwatch?.rgb
            ?: palette.lightVibrantSwatch?.rgb
            ?: palette.lightMutedSwatch?.rgb
            ?: palette.darkVibrantSwatch?.rgb
            ?: palette.darkMutedSwatch?.rgb
            ?: fallbackColor.toArgb()

    val color = Color(argb)
    return PaletteStatusBarResult(
        backgroundColor = color,
        darkIcons = shouldUseDarkStatusBarIcons(color)
    )
}

private suspend fun loadBitmapWithCoil(
    context: Context,
    model: Any
): Bitmap? {
    val loader = ImageLoader(context)

    val request = ImageRequest.Builder(context)
        .data(model)
        .allowHardware(false) 
        .size(Size.ORIGINAL)
        .build()

    val result = loader.execute(request)
    if (result !is SuccessResult) return null

    val drawable = result.drawable
    return drawable.toBitmapOrNull()
}

private fun android.graphics.drawable.Drawable.toBitmapOrNull(): Bitmap? {
    return when (this) {
        is android.graphics.drawable.BitmapDrawable -> this.bitmap
        else -> {
            val width = if (intrinsicWidth > 0) intrinsicWidth else return null
            val height = if (intrinsicHeight > 0) intrinsicHeight else return null
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bitmap)
            setBounds(0, 0, canvas.width, canvas.height)
            draw(canvas)
            bitmap
        }
    }
}

