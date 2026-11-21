package org.giggleos.setup

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color

/** Shared, persistent GiggleOS appearance settings for prototype activities. */
object GiggleAppearance {
    data class Colors(
        val background: Int,
        val surface: Int,
        val text: Int,
        val muted: Int,
        val accent: Int,
        val heroStart: Int,
        val heroEnd: Int,
        val dark: Boolean
    )

    private const val PREFS = "giggle_appearance"
    private const val MODE = "mode"
    private const val PALETTE = "palette"

    fun mode(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(MODE, "system") ?: "system"

    fun palette(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(PALETTE, "ocean") ?: "ocean"

    fun save(context: Context, mode: String, palette: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(MODE, mode)
            .putString(PALETTE, palette)
            .apply()
    }

    fun colors(context: Context): Colors {
        val dark = when (mode(context)) {
            "dark" -> true
            "light" -> false
            else -> (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES
        }
        val palette = palette(context)
        val accent = when (palette) {
            "jade" -> Color.rgb(75, 151, 126)
            "coral" -> Color.rgb(190, 111, 91)
            else -> Color.rgb(63, 137, 157)
        }
        val heroStart = when (palette) {
            "jade" -> Color.rgb(72, 151, 126)
            "coral" -> Color.rgb(191, 116, 94)
            else -> Color.rgb(65, 139, 153)
        }
        val heroEnd = when (palette) {
            "jade" -> Color.rgb(34, 91, 79)
            "coral" -> Color.rgb(117, 68, 72)
            else -> Color.rgb(35, 83, 108)
        }
        return if (dark) Colors(
            background = Color.rgb(15, 27, 37),
            surface = Color.rgb(31, 47, 59),
            text = Color.rgb(241, 247, 249),
            muted = Color.rgb(166, 185, 195),
            accent = accent,
            heroStart = heroStart,
            heroEnd = heroEnd,
            dark = true
        ) else Colors(
            background = Color.rgb(238, 244, 246),
            surface = Color.WHITE,
            text = Color.rgb(27, 43, 56),
            muted = Color.rgb(101, 119, 132),
            accent = accent,
            heroStart = heroStart,
            heroEnd = heroEnd,
            dark = false
        )
    }
}
