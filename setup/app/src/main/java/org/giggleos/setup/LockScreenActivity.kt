package org.giggleos.setup

import android.app.Activity
import android.app.KeyguardManager
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * App-level GiggleOS lock-screen preview.
 *
 * Credentials are never collected or stored here. Authentication is delegated
 * to Android's trusted Keyguard credential UI.
 */
class LockScreenActivity : Activity() {
    private val ink = Color.rgb(242, 247, 250)
    private val muted = Color.rgb(190, 207, 218)
    private val accent = Color.rgb(159, 211, 225)
    private val requestCredential = 4107

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        window.statusBarColor = Color.rgb(24, 49, 67)
        window.navigationBarColor = Color.rgb(18, 38, 53)
        render()
    }

    override fun onResume() {
        super.onResume()
        refreshSecurityStatus()
    }

    private fun render() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(28), dp(30), dp(28), dp(26))
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(
                    Color.rgb(48, 91, 112),
                    Color.rgb(31, 65, 86),
                    Color.rgb(18, 38, 53)
                )
            )
        }

        val brand = TextView(this).apply {
            text = "GIGGLEOS"
            textSize = 12f
            letterSpacing = .24f
            setTextColor(accent)
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        }
        root.addView(brand)

        val spacerTop = View(this)
        root.addView(spacerTop, LinearLayout.LayoutParams(1, 0, 1f))

        val time = TextView(this).apply {
            text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            textSize = 76f
            letterSpacing = -.045f
            gravity = Gravity.CENTER
            setTextColor(ink)
            typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
        }
        root.addView(time, LinearLayout.LayoutParams(-1, -2))

        val date = TextView(this).apply {
            text = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date())
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            textSize = 16f
            gravity = Gravity.CENTER
            setTextColor(muted)
        }
        root.addView(date, LinearLayout.LayoutParams(-1, -2).apply {
            topMargin = dp(8)
        })

        val status = TextView(this).apply {
            text = "Ваше пространство. Под вашей защитой."
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(muted)
        }
        root.addView(status, LinearLayout.LayoutParams(-1, -2).apply {
            topMargin = dp(36)
        })

        val spacerBottom = View(this)
        root.addView(spacerBottom, LinearLayout.LayoutParams(1, 0, 1f))

        val unlock = Button(this).apply {
            text = "Разблокировать"
            isAllCaps = false
            textSize = 16f
            setTextColor(Color.rgb(24, 49, 67))
            background = GradientDrawable().apply {
                setColor(accent)
                cornerRadius = dp(18).toFloat()
            }
            setPadding(dp(22), dp(14), dp(22), dp(14))
            setOnClickListener { authenticateOrSetCredential() }
        }
        root.addView(unlock, LinearLayout.LayoutParams(-1, dp(56)))

        val setup = TextView(this).apply {
            text = "Настройки защиты"
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(muted)
            setPadding(0, dp(18), 0, dp(10))
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
            }
        }
        root.addView(setup, LinearLayout.LayoutParams(-1, -2))

        val footer = TextView(this).apply {
            text = "ПРЕДПРОСМОТР GIGGLEOS"
            textSize = 10f
            letterSpacing = .16f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(148, 174, 188))
            setPadding(0, dp(8), 0, 0)
        }
        root.addView(footer)
        setContentView(root)
    }

    private fun refreshSecurityStatus() {
        val manager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        if (!manager.isDeviceSecure) {
            Toast.makeText(
                this,
                "На устройстве пока не настроен PIN-код, пароль или графический ключ.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun authenticateOrSetCredential() {
        val manager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        if (!manager.isDeviceSecure) {
            Toast.makeText(
                this,
                "Сначала настройте блокировку в системных настройках Android.",
                Toast.LENGTH_LONG
            ).show()
            startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
            return
        }

        val confirmation = manager.createConfirmDeviceCredentialIntent(
            "Разблокировка GiggleOS",
            "Подтвердите личность системным PIN-кодом, паролем или графическим ключом."
        )
        if (confirmation == null) {
            Toast.makeText(this, "Системная проверка сейчас недоступна.", Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
            return
        }
        @Suppress("DEPRECATION")
        startActivityForResult(confirmation, requestCredential)
    }

    @Deprecated("The system credential confirmation activity returns through this callback.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == requestCredential) {
            if (resultCode == RESULT_OK) {
                setResult(RESULT_OK)
                finish()
            } else {
                Toast.makeText(this, "Проверка не завершена.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
