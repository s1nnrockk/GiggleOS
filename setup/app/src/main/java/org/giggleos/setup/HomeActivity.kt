package org.giggleos.setup

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Build
import android.app.role.RoleManager
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextClock
import android.widget.TextView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Interactive home-screen prototype. Android remains responsible for launching installed apps. */
class HomeActivity : Activity() {
    companion object {
        const val EXTRA_REQUEST_DEFAULT_HOME = "org.giggleos.setup.REQUEST_DEFAULT_HOME"
        private const val HOME_ROLE_REQUEST = 701
    }

    private val colors get() = GiggleAppearance.colors(this)
    private val ink get() = colors.text
    private val muted get() = colors.muted
    private val paper get() = colors.background
    private val deep get() = colors.heroEnd

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = paper
        window.navigationBarColor = paper
        applySystemBarIcons()
        render()
        if (intent.getBooleanExtra(EXTRA_REQUEST_DEFAULT_HOME, false)) requestDefaultHome()
    }

    @Suppress("DEPRECATION")
    private fun requestDefaultHome() {
        val prefs = getSharedPreferences("giggle_setup", MODE_PRIVATE)
        if (prefs.getBoolean("home_role_prompted", false)) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val roleManager = getSystemService(RoleManager::class.java)
                if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                    if (roleManager.isRoleHeld(RoleManager.ROLE_HOME)) {
                        prefs.edit().putBoolean("home_role_prompted", true).apply()
                    } else {
                        prefs.edit().putBoolean("home_role_prompted", true).apply()
                        startActivityForResult(roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME), HOME_ROLE_REQUEST)
                    }
                }
            } else {
                prefs.edit().putBoolean("home_role_prompted", true).apply()
                startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
            }
        } catch (_: Exception) {
            Toast.makeText(this, "Выбрать GiggleOS основным экраном можно в настройках приложений по умолчанию.", Toast.LENGTH_LONG).show()
        }
    }


    @Suppress("DEPRECATION")
    private fun applySystemBarIcons() {
        window.decorView.systemUiVisibility = if (colors.dark) 0 else
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
    }

    override fun onRestart() {
        super.onRestart()
        window.statusBarColor = paper
        window.navigationBarColor = paper
        applySystemBarIcons()
        render()
    }

    private fun render() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(paper)
            setPadding(dp(20), dp(12), dp(20), dp(12))
        }

        val status = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        status.addView(TextClock(this).apply {
            format12Hour = "h:mm"
            format24Hour = "HH:mm"
            textSize = 14f
            setTextColor(ink)
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        }, LinearLayout.LayoutParams(0, dp(30), 1f))
        val island = TextView(this).apply {
            text = "●  GIGGLE"
            textSize = 10f
            letterSpacing = .08f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            background = rounded(Color.rgb(20, 28, 35), 22)
            setPadding(dp(17), dp(8), dp(17), dp(8))
            isClickable = true
            isFocusable = true
            contentDescription = "Открыть быстрые настройки"
            setOnClickListener { showControlCenter() }
        }
        status.addView(island)
        status.addView(TextView(this).apply {
            text = batteryText()
            textSize = 11f
            setTextColor(muted)
            gravity = Gravity.END or Gravity.CENTER_VERTICAL
        }, LinearLayout.LayoutParams(0, dp(30), 1f))
        root.addView(status)

        val scroll = ScrollView(this).apply { clipToPadding = false }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(22), 0, dp(12))
        }

        val date = TextClock(this).apply {
            format12Hour = "EEEE, d MMMM"
            format24Hour = "EEEE, d MMMM"
            textSize = 13f
            setTextColor(muted)
            letterSpacing = .02f
        }
        content.addView(date)
        content.addView(TextView(this).apply {
            text = "Хорошего дня"
            textSize = 31f
            letterSpacing = -.04f
            setTextColor(ink)
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            setPadding(0, dp(4), 0, dp(16))
        })

        val glance = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(colors.heroStart, colors.heroEnd)
            ).apply { cornerRadius = dp(28).toFloat() }
            setPadding(dp(20), dp(18), dp(20), dp(18))
            addView(TextView(this@HomeActivity).apply {
                text = "GIGGLEOS"
                textSize = 10f
                letterSpacing = .18f
                setTextColor(Color.rgb(222, 241, 241))
                typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            })
            addView(TextView(this@HomeActivity).apply {
                text = SimpleDateFormat("d", Locale.getDefault()).format(Date())
                textSize = 52f
                setTextColor(Color.WHITE)
                typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
                setPadding(0, dp(3), 0, 0)
            })
            addView(TextView(this@HomeActivity).apply {
                text = "Ваше пространство. Ваш ритм."
                textSize = 13f
                setTextColor(Color.rgb(229, 241, 243))
            })
        }
        content.addView(glance, LinearLayout.LayoutParams(-1, dp(154)))

        val appsHeader = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(2), dp(24), 0, dp(13))
        }
        appsHeader.addView(TextView(this).apply {
            text = "Приложения"
            textSize = 18f
            setTextColor(ink)
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        }, LinearLayout.LayoutParams(0, -2, 1f))
        appsHeader.addView(TextView(this).apply {
            text = "Все приложения  ›"
            textSize = 12f
            setTextColor(colors.accent)
            isClickable = true
            isFocusable = true
            setOnClickListener { startActivity(Intent(this@HomeActivity, AppDrawerActivity::class.java)) }
        })
        appsHeader.setOnClickListener { startActivity(Intent(this, AppDrawerActivity::class.java)) }
        content.addView(appsHeader)
        content.addView(TextView(this).apply {
            text = "Открыть магазин приложений  ↗"
            textSize = 12f
            setTextColor(colors.accent)
            setPadding(dp(2), 0, 0, dp(12))
            isClickable = true
            isFocusable = true
            setOnClickListener { startActivity(Intent(this@HomeActivity, GiggleStoreActivity::class.java)) }
        })

        val apps = listOf(
            App("Телефон", "phone", Color.rgb(48, 158, 116), Intent(Intent.ACTION_DIAL)),
            App("Сообщения", "message", Color.rgb(65, 139, 190), Intent(Intent.ACTION_MAIN).addCategory("android.intent.category.APP_MESSAGING")),
            App("Камера", "camera", Color.rgb(70, 83, 96), Intent("android.media.action.IMAGE_CAPTURE")),
            App("Фото", "photos", Color.rgb(198, 113, 93), Intent(Intent.ACTION_VIEW).setType("image/*")),
            App("Браузер", "browser", Color.rgb(55, 126, 159), Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://www.google.com"))),
            App("Настройки", "settings", Color.rgb(104, 119, 132), Intent(this, SettingsActivity::class.java)),
            App("Календарь", "calendar", Color.rgb(190, 88, 82), Intent(Intent.ACTION_MAIN).addCategory("android.intent.category.APP_CALENDAR")),
            App("Часы", "clock", Color.rgb(65, 111, 145), Intent("android.intent.action.SHOW_ALARMS")),
            App("Файлы", "files", Color.rgb(187, 145, 68), Intent(Intent.ACTION_GET_CONTENT).setType("*/*")),
            App("Контакты", "contacts", Color.rgb(76, 143, 128), Intent(Intent.ACTION_MAIN).addCategory("android.intent.category.APP_CONTACTS")),
            App("Музыка", "music", Color.rgb(158, 99, 142), Intent(Intent.ACTION_VIEW).setType("audio/*")),
            App("System UI", "system", deep, null)
        )
        val rows = apps.chunked(4)
        rows.forEach { rowApps ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.TOP
            }
            rowApps.forEach { app ->
                row.addView(appTile(app), LinearLayout.LayoutParams(0, dp(104), 1f))
            }
            repeat(4 - rowApps.size) {
                row.addView(View(this), LinearLayout.LayoutParams(0, dp(104), 1f))
            }
            content.addView(row)
        }

        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val dock = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            background = rounded(if (colors.dark) Color.rgb(31, 47, 59) else Color.argb(235, 255, 255, 255), 28)
            setPadding(dp(8), dp(9), dp(8), dp(9))
            elevation = dp(5).toFloat()
        }
        listOf(
            App("Телефон", "phone", Color.rgb(48, 158, 116), Intent(Intent.ACTION_DIAL)),
            App("Браузер", "browser", Color.rgb(55, 126, 159), Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://www.google.com"))),
            App("Камера", "camera", Color.rgb(70, 83, 96), Intent("android.media.action.IMAGE_CAPTURE")),
            App("Настройки", "settings", Color.rgb(104, 119, 132), Intent(this, SettingsActivity::class.java))
        ).forEach { app ->
            dock.addView(appTile(app, compact = true), LinearLayout.LayoutParams(0, dp(62), 1f))
        }
        root.addView(dock, LinearLayout.LayoutParams(-1, dp(76)).apply {
            topMargin = dp(8)
            bottomMargin = dp(4)
        })

        setContentView(root)
        val animations = try {
            Settings.Global.getFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f
        } catch (_: Exception) { true }
        if (animations) {
            root.alpha = 0f
            root.translationY = dp(7).toFloat()
            root.animate().alpha(1f).translationY(0f).setDuration(220L).start()
        }
    }

    private data class App(val title: String, val iconKey: String, val color: Int, val intent: Intent?)

    private fun appTile(app: App, compact: Boolean = false): View {
        val tile = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            isClickable = true
            isFocusable = true
            setPadding(dp(3), dp(2), dp(3), dp(2))
            val icon = IconGlyphView(this@HomeActivity, app.iconKey).apply {
                background = GiggleIconAppearance.background(this@HomeActivity, app.iconKey, app.color)
                elevation = dp(2).toFloat()
            }
            addView(icon, LinearLayout.LayoutParams(dp(if (compact) 48 else 56), dp(if (compact) 48 else 56)))
            if (!compact) {
                addView(TextView(this@HomeActivity).apply {
                    text = app.title
                    textSize = 10f
                    gravity = Gravity.CENTER
                    setTextColor(ink)
                    maxLines = 1
                    setPadding(0, dp(6), 0, 0)
                })
            }
            setOnClickListener {
                if (app.intent == null) {
                    startActivity(Intent(this@HomeActivity, SystemUiPreviewActivity::class.java))
                } else {
                    launch(app.intent)
                }
            }
            setOnLongClickListener {
                GiggleIconAppearance.showEditor(this@HomeActivity, app.iconKey) { render() }
                true
            }
            contentDescription = app.title
        }
        GiggleMotion.attachPress(tile, this)
        return tile
    }

    private fun showControlCenter() {
        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(20), dp(22), dp(20))
            background = rounded(Color.rgb(239, 245, 247), 28)
        }
        panel.addView(TextView(this).apply {
            text = "Центр управления"
            textSize = 22f
            setTextColor(ink)
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            setPadding(0, 0, 0, dp(14))
        })
        val actions = listOf(
            "Wi-Fi" to Settings.ACTION_WIFI_SETTINGS,
            "Bluetooth" to Settings.ACTION_BLUETOOTH_SETTINGS,
            "Экран" to Settings.ACTION_DISPLAY_SETTINGS,
            "Звук" to Settings.ACTION_SOUND_SETTINGS,
            "Батарея" to Settings.ACTION_BATTERY_SAVER_SETTINGS,
            "Защита" to Settings.ACTION_SECURITY_SETTINGS
        )
        actions.chunked(2).forEach { pair ->
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            pair.forEach { (label, action) ->
                val button = TextView(this).apply {
                    text = label
                    textSize = 14f
                    gravity = Gravity.CENTER
                    setTextColor(ink)
                    background = rounded(Color.WHITE, 18)
                    setPadding(dp(8), dp(17), dp(8), dp(17))
                    isClickable = true
                    setOnClickListener { launch(Intent(action)) }
                }
                row.addView(button, LinearLayout.LayoutParams(0, dp(58), 1f).apply {
                    if (label == pair.first().first) rightMargin = dp(6) else leftMargin = dp(6)
                })
            }
            panel.addView(row)
            panel.addView(space(8))
        }
        val notifications = GiggleNotifications.entries(this)
        if (notifications.isEmpty()) {
            panel.addView(TextView(this).apply {
                text = if (GiggleNotifications.hasAccess(this@HomeActivity))
                    "Всё спокойно\\nНовых уведомлений нет"
                else
                    "Центр уведомлений выключен\\nРазрешите доступ, чтобы видеть уведомления приложений"
                textSize = 14f
                gravity = Gravity.CENTER
                setTextColor(muted)
                background = rounded(colors.surface, 18)
                setPadding(dp(12), dp(22), dp(12), dp(22))
            })
        } else {
            panel.addView(TextView(this).apply {
                text = "Последние уведомления"
                textSize = 14f
                setTextColor(ink)
                typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                setPadding(0, 0, 0, dp(9))
            })
            notifications.forEach { item ->
                panel.addView(TextView(this).apply {
                    text = "${item.label}  ·  ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(item.postedAt))}\\nНовое уведомление · содержимое скрыто"
                    textSize = 12f
                    setTextColor(ink)
                    background = rounded(colors.surface, 16)
                    setPadding(dp(14), dp(12), dp(14), dp(12))
                    isClickable = true
                    isFocusable = true
                    setOnClickListener {
                        val launchIntent = packageManager.getLaunchIntentForPackage(item.packageName)
                        if (launchIntent != null) launch(launchIntent)
                    }
                })
                panel.addView(space(6))
            }
        }
        AlertDialog.Builder(this)
            .setView(panel)
            .setNegativeButton("Закрыть", null)
            .setPositiveButton(if (GiggleNotifications.hasAccess(this)) "Очистить список" else "Включить доступ") { _, _ ->
                if (GiggleNotifications.hasAccess(this)) {
                    GiggleNotifications.clear(this)
                    Toast.makeText(this, "Список уведомлений очищен.", Toast.LENGTH_SHORT).show()
                } else {
                    launch(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                }
            }
            .setNeutralButton("Все настройки") { _, _ -> launch(Intent(this, SettingsActivity::class.java)) }
            .show()
    }

    @Deprecated("Deprecated in Android API")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GiggleIconAppearance.REQUEST_IMAGE) {
            GiggleIconAppearance.handleImageResult(this, resultCode, data)
            render()
        }
    }

    private fun launch(intent: Intent) {
        try {
            startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(this, "Подходящее приложение не найдено.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun batteryText(): String {
        val intent = registerReceiver(null, android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level >= 0 && scale > 0) "▰ ${level * 100 / scale}%" else "GIGGLEOS"
    }


    private class IconGlyphView(context: android.content.Context, private val key: String) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 2.6f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        private val path = Path()
        private val rect = RectF()

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val scale = minOf(width, height) / 56f
            canvas.save()
            canvas.translate(width / 2f, height / 2f)
            canvas.scale(scale, scale)
            when (key) {
                "phone" -> {
                    path.reset()
                    path.moveTo(-13f, -17f)
                    path.cubicTo(-17f, -15f, -15f, -7f, -8f, 2f)
                    path.cubicTo(-2f, 10f, 8f, 16f, 14f, 13f)
                    path.lineTo(19f, 7f)
                    path.lineTo(10f, 0f)
                    path.lineTo(5f, 5f)
                    path.cubicTo(0f, 3f, -4f, -1f, -6f, -7f)
                    path.lineTo(-1f, -12f)
                    path.lineTo(-8f, -20f)
                    path.close()
                    canvas.drawPath(path, paint)
                }
                "message" -> {
                    rect.set(-19f, -15f, 19f, 12f)
                    canvas.drawRoundRect(rect, 7f, 7f, paint)
                    path.reset(); path.moveTo(-9f, 12f); path.lineTo(-14f, 19f); path.lineTo(-13f, 11f)
                    canvas.drawPath(path, paint)
                    canvas.drawLine(-10f, -4f, 10f, -4f, paint)
                    canvas.drawLine(-10f, 3f, 5f, 3f, paint)
                }
                "camera" -> {
                    rect.set(-19f, -12f, 19f, 16f)
                    canvas.drawRoundRect(rect, 5f, 5f, paint)
                    path.reset(); path.moveTo(-10f, -12f); path.lineTo(-6f, -18f); path.lineTo(7f, -18f); path.lineTo(11f, -12f)
                    canvas.drawPath(path, paint)
                    canvas.drawCircle(0f, 2f, 8f, paint)
                    canvas.drawCircle(13f, -6f, 1.5f, paint)
                }
                "photos" -> {
                    canvas.drawCircle(0f, -11f, 7f, paint)
                    canvas.drawCircle(11f, 0f, 7f, paint)
                    canvas.drawCircle(0f, 11f, 7f, paint)
                    canvas.drawCircle(-11f, 0f, 7f, paint)
                    canvas.drawCircle(0f, 0f, 3f, paint)
                }
                "browser" -> {
                    canvas.drawCircle(0f, 0f, 19f, paint)
                    rect.set(-9f, -19f, 9f, 19f); canvas.drawOval(rect, paint)
                    path.reset(); path.moveTo(-17f, -7f); path.quadTo(0f, -13f, 17f, -7f)
                    path.moveTo(-17f, 7f); path.quadTo(0f, 13f, 17f, 7f)
                    canvas.drawPath(path, paint)
                }
                "settings" -> {
                    canvas.drawCircle(0f, 0f, 7f, paint)
                    for (i in 0 until 8) {
                        val angle = Math.toRadians(i * 45.0)
                        val x1 = kotlin.math.cos(angle).toFloat() * 12f
                        val y1 = kotlin.math.sin(angle).toFloat() * 12f
                        val x2 = kotlin.math.cos(angle).toFloat() * 19f
                        val y2 = kotlin.math.sin(angle).toFloat() * 19f
                        canvas.drawLine(x1, y1, x2, y2, paint)
                    }
                    canvas.drawCircle(0f, 0f, 3f, paint)
                }
                "calendar" -> {
                    rect.set(-17f, -16f, 17f, 18f); canvas.drawRoundRect(rect, 4f, 4f, paint)
                    canvas.drawLine(-17f, -6f, 17f, -6f, paint)
                    canvas.drawLine(-9f, -21f, -9f, -11f, paint)
                    canvas.drawLine(9f, -21f, 9f, -11f, paint)
                    canvas.drawLine(-8f, 2f, -8f, 2f, paint)
                    canvas.drawCircle(-7f, 3f, 1f, paint); canvas.drawCircle(2f, 3f, 1f, paint)
                    canvas.drawCircle(10f, 3f, 1f, paint); canvas.drawCircle(-7f, 11f, 1f, paint)
                    canvas.drawCircle(2f, 11f, 1f, paint); canvas.drawCircle(10f, 11f, 1f, paint)
                }
                "clock" -> {
                    canvas.drawCircle(0f, 0f, 19f, paint)
                    canvas.drawLine(0f, 0f, 0f, -12f, paint)
                    canvas.drawLine(0f, 0f, 10f, 6f, paint)
                    canvas.drawCircle(0f, 0f, 1.5f, paint)
                }
                "files" -> {
                    path.reset(); path.moveTo(-19f, -13f); path.lineTo(-3f, -13f); path.lineTo(2f, -7f)
                    path.lineTo(19f, -7f); path.lineTo(16f, 17f); path.lineTo(-17f, 17f); path.close()
                    canvas.drawPath(path, paint)
                    canvas.drawLine(-18f, -5f, 18f, -5f, paint)
                }
                "contacts" -> {
                    canvas.drawCircle(0f, -9f, 7f, paint)
                    path.reset(); path.moveTo(-17f, 17f); path.cubicTo(-16f, 2f, 16f, 2f, 17f, 17f)
                    canvas.drawPath(path, paint)
                }
                "music" -> {
                    path.reset(); path.moveTo(4f, -18f); path.lineTo(4f, 10f)
                    path.moveTo(4f, -17f); path.lineTo(17f, -20f); path.lineTo(17f, 7f)
                    canvas.drawPath(path, paint)
                    canvas.drawCircle(-3f, 12f, 7f, paint)
                    canvas.drawCircle(10f, 9f, 7f, paint)
                }
                else -> {
                    canvas.drawLine(-17f, -12f, 17f, -12f, paint)
                    canvas.drawLine(-17f, 0f, 17f, 0f, paint)
                    canvas.drawLine(-17f, 12f, 17f, 12f, paint)
                    canvas.drawCircle(-7f, -12f, 3f, paint)
                    canvas.drawCircle(9f, 0f, 3f, paint)
                    canvas.drawCircle(-1f, 12f, 3f, paint)
                }
            }
            canvas.restore()
        }
    }

    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radius).toFloat()
    }

    private fun space(height: Int) = View(this).apply {
        layoutParams = LinearLayout.LayoutParams(1, dp(height))
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density + .5f).toInt()
}
