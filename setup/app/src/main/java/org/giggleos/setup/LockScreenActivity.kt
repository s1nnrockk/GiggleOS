package org.giggleos.setup

import android.app.Activity
import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextClock
import android.widget.TextView
import android.widget.Toast
import java.util.Locale

/**
 * GiggleOS lock-screen visual prototype.
 *
 * This Activity is not the device Keyguard. It never reads, stores or verifies
 * credentials. Android remains the only authority for device authentication.
 */
class LockScreenActivity : Activity() {
    private val ink = Color.rgb(247, 250, 252)
    private val muted = Color.rgb(195, 213, 224)
    private val accent get() = GiggleAppearance.colors(this).accent
    private lateinit var securityStatus: TextView
    private lateinit var batteryStatus: TextView
    private var batteryReceiverRegistered = false

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != Intent.ACTION_BATTERY_CHANGED) return
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val charging = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1) in
                setOf(BatteryManager.BATTERY_STATUS_CHARGING, BatteryManager.BATTERY_STATUS_FULL)
            if (level >= 0 && scale > 0) {
                val percent = (level * 100 / scale).coerceIn(0, 100)
                batteryStatus.text = if (charging) "$percent% · Зарядка" else "$percent%"
            } else {
                batteryStatus.text = "Батарея"
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        applyImmersiveWindow()
        render()
    }

    override fun onResume() {
        super.onResume()
        applyImmersiveWindow()
        refreshSecurityStatus()
        registerBatteryReceiver()
    }

    override fun onPause() {
        unregisterBatteryReceiver()
        super.onPause()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) applyImmersiveWindow()
    }

    @Suppress("DEPRECATION")
    private fun applyImmersiveWindow() {
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= 29) {
            window.isNavigationBarContrastEnforced = false
            window.isStatusBarContrastEnforced = false
        }
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    }

    private fun render() {
        val root = FrameLayout(this).apply {
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(
                    GiggleAppearance.colors(this).heroStart,
                    GiggleAppearance.colors(this).heroEnd,
                    Color.rgb(14, 31, 48)
                )
            )
        }

        // Restrained ambient light gives depth without expensive blur effects.
        val glow = View(this).apply {
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(Color.rgb(82, 151, 164), Color.TRANSPARENT)
            ).apply { cornerRadius = dp(260).toFloat() }
            alpha = .34f
        }
        root.addView(glow, FrameLayout.LayoutParams(dp(300), dp(300), Gravity.TOP or Gravity.END).apply {
            topMargin = dp(-120)
            rightMargin = dp(-110)
        })

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(28), dp(28), dp(28), dp(24))
        }
        root.addView(content, FrameLayout.LayoutParams(-1, -1))

        val topRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val brand = TextView(this).apply {
            text = "GIGGLEOS"
            textSize = 11f
            letterSpacing = .24f
            setTextColor(accent)
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        }
        topRow.addView(brand, LinearLayout.LayoutParams(0, -2, 1f))
        batteryStatus = TextView(this).apply {
            text = "Батарея"
            textSize = 12f
            setTextColor(muted)
            gravity = Gravity.END
            contentDescription = "Уровень заряда батареи"
        }
        topRow.addView(batteryStatus)
        content.addView(topRow)

        content.addView(space(0, 1f))

        val clock = TextClock(this).apply {
            format12Hour = "h:mm"
            format24Hour = "HH:mm"
            textSize = 88f
            letterSpacing = -.065f
            gravity = Gravity.CENTER
            setTextColor(ink)
            typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
            contentDescription = "Текущее время"
            includeFontPadding = false
        }
        content.addView(clock, LinearLayout.LayoutParams(-1, -2))

        val date = TextClock(this).apply {
            format12Hour = "EEEE, d MMMM"
            format24Hour = "EEEE, d MMMM"
            textSize = 16f
            gravity = Gravity.CENTER
            setTextColor(muted)
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            setPadding(0, dp(9), 0, 0)
            contentDescription = "Текущая дата"
        }
        content.addView(date, LinearLayout.LayoutParams(-1, -2))

        val privacy = getSharedPreferences("giggle_setup", MODE_PRIVATE)
            .getString("notification_privacy", "Скрывать личные данные")
        val privacyLabel = TextView(this).apply {
            text = when (privacy) {
                "Не показывать" -> "Уведомления скрыты"
                "Показывать содержимое" -> "Предпросмотр уведомлений включён"
                else -> "Содержимое уведомлений скрыто"
            }
            textSize = 12f
            gravity = Gravity.CENTER
            setTextColor(muted)
            alpha = .9f
            setPadding(0, dp(30), 0, 0)
        }
        content.addView(privacyLabel)

        content.addView(space(0, 1f))

        securityStatus = TextView(this).apply {
            textSize = 13f
            gravity = Gravity.CENTER
            setTextColor(accent)
            setPadding(dp(12), dp(8), dp(12), dp(18))
        }
        content.addView(securityStatus, LinearLayout.LayoutParams(-1, -2))

        val unlock = pillButton("Разблокировать", true) { authenticateOrOpenSecuritySettings() }
        content.addView(unlock, LinearLayout.LayoutParams(-1, dp(58)))

        val settings = TextView(this).apply {
            text = "Параметры защиты"
            textSize = 13f
            gravity = Gravity.CENTER
            setTextColor(muted)
            setPadding(dp(12), dp(17), dp(12), dp(12))
            isFocusable = true
            isClickable = true
            setOnClickListener { openSecuritySettings() }
        }
        content.addView(settings, LinearLayout.LayoutParams(-1, -2))

        val footer = TextView(this).apply {
            text = "ПРЕДПРОСМОТР ЭКРАНА БЛОКИРОВКИ"
            textSize = 9f
            letterSpacing = .16f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(142, 169, 184))
            setPadding(0, dp(8), 0, 0)
        }
        content.addView(footer)
        setContentView(root)
    }

    private fun refreshSecurityStatus() {
        val manager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        securityStatus.text = if (manager.isDeviceSecure) {
            "Защита устройства включена"
        } else {
            "Для защиты настройте PIN-код, пароль или графический ключ"
        }
    }

    private fun authenticateOrOpenSecuritySettings() {
        val manager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        if (!manager.isDeviceSecure) {
            Toast.makeText(this, "Сначала настройте системную блокировку.", Toast.LENGTH_LONG).show()
            openSecuritySettings()
            return
        }

        val confirmation = manager.createConfirmDeviceCredentialIntent(
            "Разблокировка GiggleOS",
            "Подтвердите личность системным способом защиты."
        )
        if (confirmation == null) {
            Toast.makeText(this, "Системная проверка недоступна на этом устройстве.", Toast.LENGTH_LONG).show()
            openSecuritySettings()
            return
        }
        @Suppress("DEPRECATION")
        startActivityForResult(confirmation, REQUEST_CREDENTIAL)
    }

    private fun openSecuritySettings() {
        try {
            startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
        } catch (_: Exception) {
            Toast.makeText(this, "Настройки защиты недоступны.", Toast.LENGTH_LONG).show()
        }
    }

    @Deprecated("Android credential confirmation returns its result through this callback.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_CREDENTIAL) return
        if (resultCode == RESULT_OK) {
            setResult(RESULT_OK)
            finish()
        } else {
            Toast.makeText(this, "Проверка не завершена.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun registerBatteryReceiver() {
        if (batteryReceiverRegistered) return
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(batteryReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(batteryReceiver, filter)
        }
        batteryReceiverRegistered = true
    }

    private fun unregisterBatteryReceiver() {
        if (!batteryReceiverRegistered) return
        try {
            unregisterReceiver(batteryReceiver)
        } catch (_: IllegalArgumentException) {
            // Receiver may already have been removed by the framework.
        }
        batteryReceiverRegistered = false
    }

    private fun pillButton(label: String, primary: Boolean, action: () -> Unit) =
        TextView(this).apply {
            text = label
            textSize = 15f
            gravity = Gravity.CENTER
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            setTextColor(if (primary) Color.rgb(20, 49, 66) else ink)
            background = GradientDrawable().apply {
                setColor(if (primary) accent else Color.argb(34, 255, 255, 255))
                cornerRadius = dp(22).toFloat()
                if (!primary) setStroke(dp(1), Color.argb(65, 255, 255, 255))
            }
            isFocusable = true
            isClickable = true
            setOnClickListener { action() }
        }

    private fun space(height: Int, weight: Float = 0f) =
        View(this).apply {
            layoutParams = LinearLayout.LayoutParams(1, if (weight > 0f) 0 else dp(height), weight)
        }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density + .5f).toInt()

    companion object {
        private const val REQUEST_CREDENTIAL = 4107
    }
}
