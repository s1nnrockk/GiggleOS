package org.giggleos.setup

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.widget.Toast

/** Per-icon appearance overrides shared by the home screen and app drawer. */
object GiggleIconAppearance {
    const val REQUEST_IMAGE = 9917
    private const val PREFS = "giggle_icon_appearance"
    private const val PENDING_KEY = "pending_icon_key"

    private val palette = listOf(
        "Морская волна" to 0xFF2E8290.toInt(),
        "Небесный" to 0xFF538BBF.toInt(),
        "Нефрит" to 0xFF438A70.toInt(),
        "Коралл" to 0xFFC87568.toInt(),
        "Янтарный" to 0xFFD19A45.toInt(),
        "Лавандовый" to 0xFF8D78B5.toInt(),
        "Графит" to 0xFF52616B.toInt(),
        "Светлый" to 0xFFE5EDF0.toInt()
    )

    fun background(activity: Activity, key: String, fallback: Int): Drawable {
        val prefs = activity.getSharedPreferences(PREFS, Activity.MODE_PRIVATE)
        val uriText = prefs.getString("image:$key", null)
        if (!uriText.isNullOrBlank()) {
            try {
                val uri = Uri.parse(uriText)
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                activity.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
                val sample = maxOf(1, minOf(bounds.outWidth, bounds.outHeight) / 160)
                val options = BitmapFactory.Options().apply { inSampleSize = sample.coerceAtLeast(1) }
                val bitmap = activity.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
                if (bitmap != null) return BitmapDrawable(activity.resources, bitmap)
            } catch (_: Exception) { }
        }
        val color = prefs.getInt("color:$key", fallback)
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = 19f * activity.resources.displayMetrics.density
        }
    }

    fun showEditor(activity: Activity, key: String, changed: () -> Unit) {
        val prefs = activity.getSharedPreferences(PREFS, Activity.MODE_PRIVATE)
        val options = arrayOf("Выбрать изображение", "Выбрать цвет", "Сбросить оформление")
        AlertDialog.Builder(activity)
            .setTitle("Оформление иконки")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        prefs.edit().putString(PENDING_KEY, key).apply()
                        val picker = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE)
                            type = "image/*"
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                        }
                        try { activity.startActivityForResult(picker, REQUEST_IMAGE) }
                        catch (_: Exception) { Toast.makeText(activity, "Не удалось открыть галерею.", Toast.LENGTH_SHORT).show() }
                    }
                    1 -> AlertDialog.Builder(activity)
                        .setTitle("Цвет фона")
                        .setItems(palette.map { it.first }.toTypedArray()) { _, colorIndex ->
                            prefs.edit().remove("image:$key").putInt("color:$key", palette[colorIndex].second).apply()
                            changed()
                        }
                        .setNegativeButton("Отмена", null)
                        .show()
                    2 -> {
                        prefs.edit().remove("image:$key").remove("color:$key").apply()
                        changed()
                    }
                }
            }
            .setNegativeButton("Закрыть", null)
            .show()
    }

    fun handleImageResult(activity: Activity, resultCode: Int, data: Intent?): Boolean {
        if (resultCode != Activity.RESULT_OK || data?.data == null) return true
        val prefs = activity.getSharedPreferences(PREFS, Activity.MODE_PRIVATE)
        val key = prefs.getString(PENDING_KEY, null) ?: return true
        val uri = data.data ?: return true
        try {
            activity.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            prefs.edit().putString("image:$key", uri.toString()).remove("color:$key").remove(PENDING_KEY).apply()
            Toast.makeText(activity, "Фон иконки сохранён.", Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {
            Toast.makeText(activity, "Не удалось сохранить изображение.", Toast.LENGTH_SHORT).show()
        }
        return true
    }
}
