package org.giggleos.setup

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

/** GiggleOS settings hub that routes protected controls to Android's real settings pages. */
class SettingsActivity : Activity() {
    private val colors get() = GiggleAppearance.colors(this)
    private val ink get() = colors.text
    private val muted get() = colors.muted
    private val paper get() = colors.background
    private val blue get() = colors.accent
    private lateinit var content: LinearLayout
    private lateinit var search: EditText

    private data class Entry(
        val group: String,
        val title: String,
        val subtitle: String,
        val action: String? = null,
        val special: String? = null
    )

    private val entries = listOf(
        Entry("Подключение", "Wi-Fi", "Сети и сохранённые подключения", Settings.ACTION_WIFI_SETTINGS),
        Entry("Подключение", "Bluetooth", "Наушники и устройства рядом", Settings.ACTION_BLUETOOTH_SETTINGS),
        Entry("Подключение", "Мобильная сеть", "SIM-карты и мобильные данные", Settings.ACTION_WIRELESS_SETTINGS),
        Entry("Подключение", "Режим полёта", "Параметры беспроводной связи", Settings.ACTION_AIRPLANE_MODE_SETTINGS),
        Entry("Персонализация", "Внешний вид GiggleOS", "Светлая и тёмная тема, цветовая палитра", special = "appearance"),
        Entry("Персонализация", "Экран и яркость", "Яркость, размер текста и тайм-аут", Settings.ACTION_DISPLAY_SETTINGS),
        Entry("Персонализация", "Обои GiggleOS", "Выбрать фон экрана блокировки", special = "wallpaper"),
        Entry("Персонализация", "Экран блокировки", "Часы и предпросмотр блокировки", special = "lock"),
        Entry("Звуки и уведомления", "Звук и вибрация", "Громкость и системные сигналы", Settings.ACTION_SOUND_SETTINGS),
        Entry("Звуки и уведомления", "Уведомления", "Разрешения и каналы приложений", Settings.ACTION_APP_NOTIFICATION_SETTINGS),
        Entry("Звуки и уведомления", "Центр уведомлений GiggleOS", "Доступ включается только с вашего разрешения", special = "notification_access"),
        Entry("Звуки и уведомления", "Не беспокоить", "Режим тишины и расписание", Settings.ACTION_ZEN_MODE_PRIORITY_SETTINGS),
        Entry("Безопасность", "Защита и блокировка", "PIN-код, пароль и биометрия", Settings.ACTION_SECURITY_SETTINGS),
        Entry("Безопасность", "Конфиденциальность", "Разрешения и доступ приложений", Settings.ACTION_PRIVACY_SETTINGS),
        Entry("Система", "Батарея", "Заряд и энергопотребление", Settings.ACTION_BATTERY_SAVER_SETTINGS),
        Entry("Система", "Специальные возможности", "Экран, управление и доступность", Settings.ACTION_ACCESSIBILITY_SETTINGS),
        Entry("Система", "О GiggleOS", "Версия прототипа и устройство", special = "about")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = paper
        window.navigationBarColor = paper
        applySystemBarIcons()

        render()
    }

    private fun render() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(paper)
            setPadding(dp(22), dp(14), dp(22), dp(18))
        }
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        header.addView(TextView(this).apply {
            text = "Настройки"
            textSize = 29f
            letterSpacing = -.035f
            setTextColor(ink)
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        }, LinearLayout.LayoutParams(0, dp(48), 1f))
        header.addView(TextView(this).apply {
            text = "GIGGLEOS"
            textSize = 10f
            letterSpacing = .15f
            setTextColor(blue)
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        })
        root.addView(header)

        val profile = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded(colors.surface, 22)
            setPadding(dp(17), dp(15), dp(17), dp(15))
        }
        profile.addView(TextView(this).apply {
            text = "Устройство"
            textSize = 11f
            setTextColor(muted)
        })
        profile.addView(TextView(this).apply {
            text = "GiggleOS"
            textSize = 19f
            setTextColor(ink)
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            setPadding(0, dp(4), 0, 0)
        })
        profile.addView(TextView(this).apply {
            text = "Настройте телефон под себя"
            textSize = 12f
            setTextColor(muted)
            setPadding(0, dp(3), 0, 0)
        })
        root.addView(profile, LinearLayout.LayoutParams(-1, -2).apply {
            topMargin = dp(12)
            bottomMargin = dp(14)
        })

        search = EditText(this).apply {
            hint = "Поиск настроек"
            textSize = 14f
            setSingleLine(true)
            setTextColor(ink)
            setHintTextColor(muted)
            background = rounded(colors.surface, 16)
            setPadding(dp(15), 0, dp(15), 0)
        }
        root.addView(search, LinearLayout.LayoutParams(-1, dp(50)).apply {
            bottomMargin = dp(10)
        })

        val scroll = ScrollView(this).apply { clipToPadding = false }
        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(4), 0, dp(16))
        }
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                renderEntries(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })
        renderEntries("")
        setContentView(root)
    }

    private fun renderEntries(query: String) {
        content.removeAllViews()
        val filtered = entries.filter {
            it.title.contains(query, true) || it.subtitle.contains(query, true) || it.group.contains(query, true)
        }
        if (filtered.isEmpty()) {
            content.addView(TextView(this).apply {
                text = "Ничего не найдено"
                textSize = 14f
                gravity = Gravity.CENTER
                setTextColor(muted)
                setPadding(0, dp(32), 0, dp(32))
            })
            return
        }
        filtered.groupBy { it.group }.forEach { (group, rows) ->
            content.addView(TextView(this).apply {
                text = group.uppercase()
                textSize = 10f
                letterSpacing = .1f
                setTextColor(muted)
                typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                setPadding(dp(3), dp(14), 0, dp(8))
            })
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                background = rounded(colors.surface, 20)
                setPadding(dp(14), dp(3), dp(14), dp(3))
            }
            rows.forEachIndexed { index, entry ->
                val row = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    isClickable = true
                    isFocusable = true
                    setPadding(dp(3), dp(12), dp(3), dp(12))
                    addView(LinearLayout(this@SettingsActivity).apply {
                        orientation = LinearLayout.VERTICAL
                        addView(TextView(this@SettingsActivity).apply {
                            text = entry.title
                            textSize = 14f
                            setTextColor(ink)
                            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                        })
                        addView(TextView(this@SettingsActivity).apply {
                            text = entry.subtitle
                            textSize = 11f
                            setTextColor(muted)
                            setPadding(0, dp(3), 0, 0)
                        })
                    }, LinearLayout.LayoutParams(0, -2, 1f))
                    addView(TextView(this@SettingsActivity).apply {
                        text = "›"
                        textSize = 24f
                        setTextColor(Color.rgb(164, 177, 185))
                    })
                    setOnClickListener { openEntry(entry) }
                }
                card.addView(row)
                if (index != rows.lastIndex) {
                    card.addView(View(this@SettingsActivity).apply {
                        setBackgroundColor(if (colors.dark) Color.rgb(53, 69, 80) else Color.rgb(235, 240, 242))
                        layoutParams = LinearLayout.LayoutParams(-1, dp(1))
                    })
                }
            }
            content.addView(card)
        }
    }

    private fun openEntry(entry: Entry) {
        when (entry.special) {
            "lock" -> startActivity(Intent(this, LockScreenActivity::class.java))
            "about" -> showAbout()
            "appearance" -> showAppearance()
            "wallpaper" -> showWallpaper()
            "notification_access" -> {
                try {
                    startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                } catch (_: Exception) {
                    Toast.makeText(this, "Настройка доступа недоступна на этом устройстве.", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                try {
                    startActivity(Intent(entry.action))
                } catch (_: Exception) {
                    Toast.makeText(this, "Этот раздел недоступен на устройстве.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun showAppearance() {
        val modes = listOf("system", "light", "dark")
        val palettes = listOf("ocean", "jade", "coral")
        val modeLabels = mapOf("system" to "Как в системе", "light" to "Светлая", "dark" to "Тёмная")
        val paletteLabels = mapOf("ocean" to "Океан", "jade" to "Нефрит", "coral" to "Коралл")
        val choices = modes.flatMap { mode -> palettes.map { palette ->
            "${modeLabels.getValue(mode)} · ${paletteLabels.getValue(palette)}" to (mode to palette)
        } }
        val current = GiggleAppearance.mode(this) to GiggleAppearance.palette(this)
        val selected = choices.indexOfFirst { it.second == current }.coerceAtLeast(0)
        AlertDialog.Builder(this)
            .setTitle("Внешний вид GiggleOS")
            .setSingleChoiceItems(choices.map { it.first }.toTypedArray(), selected) { dialog, which ->
                val choice = choices[which].second
                GiggleAppearance.save(this, choice.first, choice.second)
                dialog.dismiss()
                recreate()
            }
            .setNegativeButton("Закрыть", null)
            .show()
    }

    @Suppress("DEPRECATION")
    private fun applySystemBarIcons() {
        window.decorView.systemUiVisibility = if (colors.dark) 0 else
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
    }


    private fun showWallpaper() {
        val options = listOf(
            "coast" to "Тихий берег",
            "aurora" to "Северное сияние",
            "dawn" to "Рассвет",
            "meadow" to "Зелёная долина"
        )
        val selected = options.indexOfFirst { it.first == GiggleAppearance.wallpaper(this) }.coerceAtLeast(0)
        AlertDialog.Builder(this)
            .setTitle("Обои GiggleOS")
            .setSingleChoiceItems(options.map { it.second }.toTypedArray(), selected) { dialog, which ->
                GiggleAppearance.saveWallpaper(this, options[which].first)
                dialog.dismiss()
                Toast.makeText(this, "Обои применены к экрану блокировки.", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Закрыть", null)
            .show()
    }

    private fun showAbout() {
        val version = try {
            packageManager.getPackageInfo(packageName, 0).versionName ?: "0.1.0"
        } catch (_: Exception) { "0.1.0" }
        val details = "GiggleOS Prototype $version\n\nУстройство: ${Build.MANUFACTURER} ${Build.MODEL}\nAndroid: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})\nКодовое имя устройства: ${Build.DEVICE}\n\nЭто пользовательский прототип интерфейса, а не установленная прошивка GiggleOS."
        AlertDialog.Builder(this)
            .setTitle("О GiggleOS")
            .setMessage(details)
            .setPositiveButton("Готово", null)
            .show()
    }

    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radius).toFloat()
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density + .5f).toInt()
}
