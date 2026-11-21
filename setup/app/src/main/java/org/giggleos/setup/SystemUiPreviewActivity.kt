package org.giggleos.setup

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.BatteryManager
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextClock
import android.widget.TextView
import android.widget.Toast

/** App-level System UI preview. AOSP SystemUI remains the authority for real system surfaces. */
class SystemUiPreviewActivity : Activity() {
    private val backgroundColor = Color.rgb(13, 28, 42)
    private val surface = Color.rgb(28, 48, 64)
    private val raised = Color.rgb(37, 62, 78)
    private val ink = Color.rgb(244, 248, 250)
    private val muted = Color.rgb(174, 196, 207)
    private val accent = Color.rgb(166, 222, 225)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        window.statusBarColor = backgroundColor
        window.navigationBarColor = backgroundColor
        render()
    }

    private fun render() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(backgroundColor)
            setPadding(dp(22), dp(18), dp(22), dp(24))
        }
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        header.addView(label("GIGGLEOS", 11f, accent, true), LinearLayout.LayoutParams(0, -2, 1f))
        header.addView(label(batteryText(), 12f, muted, false))
        root.addView(header)

        val scroll = ScrollView(this).apply { clipToPadding = false }
        val body = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(18), 0, dp(12))
        }
        body.addView(TextClock(this).apply {
            format12Hour = "h:mm"
            format24Hour = "HH:mm"
            textSize = 58f
            letterSpacing = -.05f
            includeFontPadding = false
            setTextColor(ink)
            typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
            contentDescription = "Текущее время"
        })
        body.addView(TextClock(this).apply {
            format12Hour = "EEEE, d MMMM"
            format24Hour = "EEEE, d MMMM"
            textSize = 14f
            setTextColor(muted)
            setPadding(0, dp(3), 0, dp(20))
        })

        section(body, "Быстрые настройки", "Карточки открывают настоящие системные параметры.")
        body.addView(tileRow(
            tile("Wi-Fi", "Сети и подключение", "⌁") { openSettings(Settings.ACTION_WIFI_SETTINGS) },
            tile("Bluetooth", "Устройства рядом", "ᛒ") { openSettings(Settings.ACTION_BLUETOOTH_SETTINGS) }
        ))
        body.addView(space(9))
        body.addView(tileRow(
            tile("Экран", "Яркость и оформление", "◐") { openSettings(Settings.ACTION_DISPLAY_SETTINGS) },
            tile("Звук", "Громкость и режимы", "♫") { openSettings(Settings.ACTION_SOUND_SETTINGS) }
        ))
        body.addView(space(9))
        body.addView(tileRow(
            tile("Батарея", "Энергопотребление", "▰") { openSettings(Settings.ACTION_BATTERY_SAVER_SETTINGS) },
            tile("Защита", "PIN и биометрия", "◇") { openSettings(Settings.ACTION_SECURITY_SETTINGS) }
        ))
        body.addView(space(22))

        section(body, "Уведомления", "Реальная лента подключается внутри AOSP SystemUI.")
        body.addView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = rounded(surface, 22)
            setPadding(dp(18), dp(24), dp(18), dp(24))
            addView(label("Всё спокойно", 17f, ink, true))
            addView(label("Новых уведомлений нет", 13f, muted, false).apply {
                setPadding(0, dp(7), 0, 0)
            })
        })
        body.addView(space(22))
        section(body, "Управление устройством", "Действия передаются системным приложениям.")
        body.addView(actionRow("Экран блокировки", "Открыть предпросмотр") {
            startActivity(Intent(this, LockScreenActivity::class.java))
        })
        body.addView(space(8))
        body.addView(actionRow("Настройки уведомлений", "Каналы и разрешения") {
            openSettings(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        })
        body.addView(space(8))
        body.addView(actionRow("Все настройки", "Открыть настройки Android") {
            openSettings(Settings.ACTION_SETTINGS)
        })
        body.addView(space(18))
        body.addView(label("ПРОТОТИП SYSTEM UI · НЕ ЗАМЕНЯЕТ СИСТЕМНУЮ ШТОРКУ", 9f, Color.rgb(128, 154, 170), true).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, 0)
        })
        scroll.addView(body)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)

        val animationsEnabled = try {
            Settings.Global.getFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f
        } catch (_: Exception) { true }
        if (animationsEnabled) {
            root.alpha = 0f
            root.translationY = dp(8).toFloat()
            root.animate().alpha(1f).translationY(0f).setDuration(220L).start()
        }
    }

    private fun tileRow(first: View, second: View) = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        addView(first, LinearLayout.LayoutParams(0, dp(106), 1f).apply { rightMargin = dp(5) })
        addView(second, LinearLayout.LayoutParams(0, dp(106), 1f).apply { leftMargin = dp(5) })
    }

    private fun tile(title: String, subtitle: String, glyph: String, action: () -> Unit) =
        LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(15), dp(13), dp(13), dp(12))
            background = rounded(raised, 22)
            isClickable = true
            isFocusable = true
            addView(label(glyph, 19f, accent, true))
            addView(label(title, 14f, ink, true).apply { setPadding(0, dp(5), 0, 0) })
            addView(label(subtitle, 10f, muted, false).apply { setPadding(0, dp(2), 0, 0) })
            setOnClickListener { action() }
        }

    private fun actionRow(title: String, subtitle: String, action: () -> Unit) =
        LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded(surface, 18)
            setPadding(dp(16), dp(13), dp(16), dp(13))
            isClickable = true
            isFocusable = true
            addView(label(title, 14f, ink, true))
            addView(label(subtitle, 11f, muted, false).apply { setPadding(0, dp(4), 0, 0) })
            setOnClickListener { action() }
        }

    private fun section(parent: LinearLayout, title: String, subtitle: String) {
        parent.addView(label(title, 18f, ink, true))
        parent.addView(label(subtitle, 11f, muted, false).apply {
            setPadding(0, dp(5), 0, dp(12))
        })
    }

    private fun label(value: String, size: Float, color: Int, medium: Boolean) =
        TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            typeface = Typeface.create(if (medium) "sans-serif-medium" else "sans-serif", Typeface.NORMAL)
        }

    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radius).toFloat()
    }

    private fun space(height: Int) = View(this).apply {
        layoutParams = LinearLayout.LayoutParams(1, dp(height))
    }

    private fun batteryText(): String {
        val intent = registerReceiver(null, android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level >= 0 && scale > 0) "Батарея ${level * 100 / scale}%" else "GIGGLEOS"
    }

    private fun openSettings(action: String) {
        try {
            startActivity(Intent(action))
        } catch (_: Exception) {
            Toast.makeText(this, "Этот системный экран недоступен на устройстве.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density + .5f).toInt()
}
