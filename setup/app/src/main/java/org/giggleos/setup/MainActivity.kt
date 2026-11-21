package org.giggleos.setup

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private val ink = Color.rgb(30, 43, 56)
    private val muted = Color.rgb(103, 119, 132)
    private val paper = Color.rgb(246, 248, 249)
    private val blue = Color.rgb(46, 111, 157)
    private val paleBlue = Color.rgb(225, 239, 247)
    private val green = Color.rgb(66, 125, 104)
    private var step = 0
    private var localeChoice = "Русский"
    private var regionChoice = "Автоматически"
    private var sourceChoice = "new"
    private var appearanceChoice = "Авто"
    private var navigationChoice = "Жесты"
    private var notificationPrivacy = "Скрывать личные данные"
    private var clockStyle = "Минималистичные часы"
    private var lockChoice = "Настроить позже"
    private var transferFlags = 0
    private var rustLoaded = false

    private external fun nativeNextStep(current: Int): Int
    private external fun nativePreviousStep(current: Int): Int
    private external fun nativeValidateTransferFlags(flags: Int): Boolean
    private external fun nativeSha256(input: String): String

    companion object {
        init {
            try {
                System.loadLibrary("giggle_setup_core")
            } catch (_: UnsatisfiedLinkError) {
                // Kotlin fallback keeps the UI usable before the NDK library is built.
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rustLoaded = try {
            nativeSha256("giggleos").length == 64
        } catch (_: UnsatisfiedLinkError) { false }

        val prefs = getSharedPreferences("giggle_setup", MODE_PRIVATE)
        if (prefs.getBoolean("setup_complete", false)) {
            step = 11
        } else {
            step = savedInstanceState?.getInt("step")
                ?: prefs.getInt("current_step", 0).coerceIn(0, 11)
        }
        localeChoice = prefs.getString("language", localeChoice) ?: localeChoice
        regionChoice = prefs.getString("region", regionChoice) ?: regionChoice
        sourceChoice = prefs.getString("source", sourceChoice) ?: sourceChoice
        appearanceChoice = prefs.getString("appearance", appearanceChoice) ?: appearanceChoice
        navigationChoice = prefs.getString("navigation", navigationChoice) ?: navigationChoice
        notificationPrivacy = prefs.getString("notification_privacy", notificationPrivacy) ?: notificationPrivacy
        clockStyle = prefs.getString("clock_style", clockStyle) ?: clockStyle
        lockChoice = prefs.getString("lock", lockChoice) ?: lockChoice
        transferFlags = prefs.getInt("transfer_flags", 0)
        render()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("step", step)
        super.onSaveInstanceState(outState)
    }

    private fun persist() {
        getSharedPreferences("giggle_setup", MODE_PRIVATE).edit()
            .putInt("current_step", step)
            .putString("language", localeChoice)
            .putString("region", regionChoice)
            .putString("source", sourceChoice)
            .putString("appearance", appearanceChoice)
            .putString("navigation", navigationChoice)
            .putString("notification_privacy", notificationPrivacy)
            .putString("clock_style", clockStyle)
            .putString("lock", lockChoice)
            .putInt("transfer_flags", transferFlags)
            .apply()
    }

    private fun render() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(paper)
            setPadding(dp(24), dp(18), dp(24), dp(20))
        }

        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val brand = TextView(this).apply {
            text = "GIGGLEOS"
            textSize = 12f
            letterSpacing = .18f
            setTextColor(blue)
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        }
        top.addView(brand, LinearLayout.LayoutParams(0, dp(32), 1f))
        val stepLabel = TextView(this).apply {
            text = if (step == 11) "ГОТОВО" else "${step + 1} / 12"
            textSize = 12f
            setTextColor(muted)
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        }
        top.addView(stepLabel)
        root.addView(top)

        if (step < 11) {
            val progress = View(this).apply {
                background = rounded(blue, 20)
            }
            val track = LinearLayout(this).apply {
                setBackground(rounded(Color.rgb(226, 232, 236), 20))
                addView(progress, LinearLayout.LayoutParams(0, dp(4), (step + 1) / 12f))
            }
            root.addView(track, LinearLayout.LayoutParams(-1, dp(4)).apply {
                topMargin = dp(12)
                bottomMargin = dp(24)
            })
        } else {
            root.addView(space(20))
        }

        val scroll = ScrollView(this).apply { clipToPadding = false }
        val body = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(4), 0, dp(20))
        }
        buildPage(body)
        scroll.addView(body)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        if (step > 0 && step < 11) {
            actions.addView(actionButton("Назад", false) { goBack() },
                LinearLayout.LayoutParams(0, dp(54), 1f).apply { rightMargin = dp(8) })
        }
        val mainText = when (step) {
            0 -> "Начать"
            10 -> "Подтвердить"
            11 -> "Перейти на главный экран"
            else -> "Продолжить"
        }
        actions.addView(actionButton(mainText, true) { goNext() },
            LinearLayout.LayoutParams(0, dp(54), 2f))
        root.addView(actions)
        if (step in 3..9) {
            val skip = TextView(this).apply {
                text = "Пропустить этот шаг"
                textSize = 13f
                gravity = Gravity.CENTER
                setTextColor(muted)
                setPadding(0, dp(12), 0, dp(2))
                setOnClickListener { goNext() }
            }
            root.addView(skip, LinearLayout.LayoutParams(-1, dp(38)))
        }
        setContentView(root)
    }

    private fun buildPage(body: LinearLayout) {
        when (step) {
            0 -> {
                hero(body, "Добро пожаловать.", "Новая система. Чистый старт. Всё готово к настройке.")
                body.addView(infoCard("GiggleOS", "Настроим телефон за несколько простых шагов. Большинство пунктов можно пропустить и изменить позже."))
                body.addView(space(14))
                body.addView(note("Первый запуск", "Этот мастер появится после установки системы или сброса настроек. При обычной перезагрузке он не запускается."))
            }
            1 -> {
                hero(body, "Ваш язык.", "Выберите язык интерфейса GiggleOS.")
                body.addView(choiceList(body, listOf("Русский", "English", "Deutsch", "Español", "Français"), localeChoice) {
                    localeChoice = it; persist(); render()
                })
                body.addView(note("Примечание", "В этом прототипе выбор сохраняется внутри мастера. Глобальная смена языка будет подключена при интеграции в AOSP."))
            }
            2 -> {
                hero(body, "Регион и время.", "Используем региональные форматы даты, времени и чисел.")
                body.addView(choiceList(body, listOf("Автоматически", "Нидерланды", "Россия", "Германия", "США"), regionChoice) {
                    regionChoice = it; persist(); render()
                })
                body.addView(actionButton("Открыть настройки даты и времени", false) {
                    openSettings(Intent(Settings.ACTION_DATE_SETTINGS))
                })
            }
            3 -> {
                hero(body, "Подключитесь к Wi-Fi.", "Интернет пригодится для обновлений и загрузки приложений. Можно настроить позже.")
                body.addView(infoCard("Сеть Wi-Fi", "Откроем системный экран Android, чтобы показать реальные доступные сети и подключиться безопасно."))
                body.addView(space(12))
                body.addView(actionButton("Открыть настройки Wi-Fi", true) {
                    openSettings(Intent(Settings.ACTION_WIFI_SETTINGS))
                })
                body.addView(note("Без интернета", "Настройку GiggleOS можно продолжить офлайн."))
            }
            4 -> {
                hero(body, "Мобильная сеть.", "Проверьте SIM-карту и параметры оператора.")
                body.addView(infoCard("SIM и мобильные данные", "Доступность зависит от модема, SIM и драйверов конкретной модели."))
                body.addView(space(12))
                body.addView(actionButton("Открыть настройки сети", true) {
                    openSettings(Intent(Settings.ACTION_WIRELESS_SETTINGS))
                })
                body.addView(note("Если SIM не обнаружена", "Можно пропустить этот шаг и вернуться к нему после настройки."))
            }
            5 -> {
                hero(body, "Перенести данные?", "Выберите, откуда вы переходите. Старое устройство останется без изменений.")
                body.addView(choiceList(body, listOf("Настроить как новое устройство", "Перенести с Android", "Импортировать с iPhone"), when(sourceChoice) {
                    "android" -> "Перенести с Android"
                    "iphone" -> "Импортировать с iPhone"
                    else -> "Настроить как новое устройство"
                }) { selected ->
                    sourceChoice = when(selected) {
                        "Перенести с Android" -> "android"
                        "Импортировать с iPhone" -> "iphone"
                        else -> "new"
                    }
                    persist(); render()
                })
                if (sourceChoice != "new") {
                    body.addView(space(10))
                    body.addView(note("Выбор данных", "В будущем здесь появится защищённое соединение между устройствами. Сейчас реальный приём и копирование данных ещё не реализованы."))
                    body.addView(transferCategoryChecks(body))
                } else {
                    body.addView(note("Чистый старт", "На телефон не будут переноситься данные со старого устройства."))
                }
            }
            6 -> {
                hero(body, "Защитите телефон.", "Настройте способ разблокировки экрана.")
                choiceList(body, listOf("Настроить позже", "PIN-код или пароль", "Графический ключ"), lockChoice) {
                    lockChoice = it; persist(); render()
                }
                body.addView(space(10))
                body.addView(actionButton("Открыть системные настройки блокировки", false) {
                    openSettings(Intent(Settings.ACTION_SECURITY_SETTINGS))
                })
                body.addView(note("Защита", "Сам мастер не сохраняет PIN-код или пароль. Их ввод выполняется только в системном интерфейсе Android."))
            }
            7 -> {
                hero(body, "Биометрия.", "Разблокируйте устройство отпечатком пальца, если датчик поддерживается.")
                body.addView(infoCard("Отпечаток пальца", "Этот пункт будет доступен только после проверки биометрического датчика и системного HAL на конкретной модели."))
                body.addView(space(12))
                body.addView(actionButton("Открыть настройки биометрии", true) {
                    if (Build.VERSION.SDK_INT >= 30) {
                        openSettings(Intent("android.settings.BIOMETRIC_ENROLL"))
                    } else {
                        openSettings(Intent(Settings.ACTION_SECURITY_SETTINGS))
                    }
                })
                body.addView(note("Важно", "Если Android сообщает, что биометрия недоступна, пропустите шаг."))
            }
            8 -> {
                hero(body, "Экран блокировки.", "Выберите настроение и приватность уведомлений.")
                body.addView(infoCard("Предпросмотр", "10:08\n\nПонедельник, 21 ноября\n\nУведомления будут отображаться здесь"))
                body.addView(sectionLabel("Стиль часов"))
                body.addView(choiceList(body, listOf("Минималистичные часы", "Крупные часы"), clockStyle) {
                    clockStyle = it; persist(); render()
                })
                body.addView(sectionLabel("Уведомления на заблокированном экране"))
                body.addView(choiceList(body, listOf("Показывать содержимое", "Скрывать личные данные", "Не показывать"), notificationPrivacy) {
                    notificationPrivacy = it; persist(); render()
                })
                body.addView(note("Персонализация", "Выборы сохраняются в мастере. Применение к настоящему экрану блокировки подключается через System UI."))
            }
            9 -> {
                hero(body, "Настройте ощущения.", "Выберите тему и способ навигации.")
                body.addView(sectionLabel("Внешний вид"))
                body.addView(choiceList(body, listOf("Авто", "Светлая", "Тёмная"), appearanceChoice) {
                    appearanceChoice = it; persist(); render()
                })
                body.addView(space(12))
                body.addView(sectionLabel("Навигация"))
                body.addView(choiceList(body, listOf("Жесты", "Три кнопки"), navigationChoice) {
                    navigationChoice = it; persist(); render()
                })
                body.addView(space(12))
                body.addView(actionButton("Настройки специальных возможностей", false) {
                    openSettings(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                })
            }
            10 -> {
                hero(body, "Почти готово.", "Проверьте выбранные параметры перед запуском.")
                body.addView(summaryCard("Язык", localeChoice))
                body.addView(summaryCard("Регион", regionChoice))
                body.addView(summaryCard("Перенос", when(sourceChoice) {
                    "android" -> "С Android (подключение ещё не реализовано)"
                    "iphone" -> "С iPhone (импорт ещё не реализован)"
                    else -> "Настроить как новое устройство"
                }))
                body.addView(summaryCard("Блокировка", lockChoice))
                body.addView(summaryCard("Экран блокировки", "$clockStyle · $notificationPrivacy"))
                body.addView(summaryCard("Внешний вид", "$appearanceChoice · $navigationChoice"))
                body.addView(note("Можно изменить позже", "Большинство параметров будут доступны в приложении «Настройки» после завершения интеграции с AOSP."))
            }
            11 -> {
                hero(body, "Всё готово.", "Добро пожаловать в GiggleOS.")
                body.addView(infoCard("Ваш телефон готов к настройке рабочего стола.", "Это завершает демонстрационный мастер. Для системного первого запуска потребуется интеграция Setup Wizard в сборку AOSP."))
                body.addView(space(14))
                body.addView(note("Статус", if (rustLoaded) "Rust core подключён." else "Интерфейс работает в режиме Kotlin fallback. Rust core подключается после сборки NDK-библиотеки."))
            }
        }
    }

    private fun transferCategoryChecks(parent: LinearLayout): View {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded(Color.WHITE, 22)
            setPadding(dp(16), dp(10), dp(16), dp(10))
        }
        val items = listOf("Контакты" to 1, "Фотографии" to 2, "Видео" to 4, "Календарь" to 8, "Документы" to 16)
        items.forEach { (label, bit) ->
            val check = CheckBox(this).apply {
                text = label
                textSize = 15f
                setTextColor(ink)
                isChecked = (transferFlags and bit) != 0
                buttonTintList = android.content.res.ColorStateList.valueOf(blue)
                setOnCheckedChangeListener { _, checked ->
                    transferFlags = if (checked) transferFlags or bit else transferFlags and bit.inv()
                    val valid = try { nativeValidateTransferFlags(transferFlags) } catch (_: UnsatisfiedLinkError) { transferFlags in 0..31 }
                    if (!valid) transferFlags = 0
                    persist()
                }
            }
            box.addView(check)
        }
        return box
    }

    private fun choiceListView(title: String, choices: List<String>, selected: String): View {
        val container = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        container.addView(sectionLabel(title))
        container.addView(choiceList(container, choices, selected) { })
        return container
    }

    private fun choiceList(parent: LinearLayout, choices: List<String>, selected: String, onPick: (String) -> Unit): View {
        val wrap = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded(Color.WHITE, 22)
            setPadding(dp(12), dp(8), dp(12), dp(8))
        }
        choices.forEach { choice ->
            val row = TextView(this).apply {
                text = (if (choice == selected) "●  " else "○  ") + choice
                textSize = 15f
                setTextColor(if (choice == selected) blue else ink)
                typeface = Typeface.create(if (choice == selected) "sans-serif-medium" else "sans-serif", Typeface.NORMAL)
                setPadding(dp(10), dp(15), dp(10), dp(15))
                setOnClickListener { onPick(choice) }
            }
            wrap.addView(row)
            if (choice != choices.last()) wrap.addView(divider())
        }
        wrap.layoutParams = LinearLayout.LayoutParams(-1, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            bottomMargin = dp(14)
        }
        return wrap
    }

    private fun hero(parent: LinearLayout, title: String, subtitle: String) {
        val titleView = TextView(this).apply {
            text = title
            textSize = 31f
            letterSpacing = -.035f
            setTextColor(ink)
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            setPadding(0, dp(12), 0, dp(10))
        }
        parent.addView(titleView)
        val sub = TextView(this).apply {
            text = subtitle
            textSize = 16f
            setTextColor(muted)
            setLineSpacing(dp(3).toFloat(), 1f)
            setPadding(0, 0, 0, dp(22))
        }
        parent.addView(sub)
    }

    private fun infoCard(title: String, description: String): View {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded(Color.WHITE, 24)
            setPadding(dp(18), dp(18), dp(18), dp(18))
        }
        card.addView(TextView(this).apply {
            text = title
            textSize = 17f
            setTextColor(ink)
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        })
        card.addView(TextView(this).apply {
            text = description
            textSize = 14f
            setTextColor(muted)
            setLineSpacing(dp(3).toFloat(), 1f)
            setPadding(0, dp(8), 0, 0)
        })
        return card
    }

    private fun note(title: String, text: String): View {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded(paleBlue, 18)
            setPadding(dp(16), dp(14), dp(16), dp(14))
        }
        card.addView(TextView(this).apply {
            this.text = title
            textSize = 13f
            setTextColor(blue)
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        })
        card.addView(TextView(this).apply {
            this.text = text
            textSize = 13f
            setTextColor(ink)
            setPadding(0, dp(5), 0, 0)
            setLineSpacing(dp(2).toFloat(), 1f)
        })
        return card
    }

    private fun summaryCard(title: String, value: String): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded(Color.WHITE, 16)
            setPadding(dp(15), dp(12), dp(15), dp(12))
        }
        row.addView(TextView(this).apply {
            text = title.uppercase()
            textSize = 10f
            letterSpacing = .1f
            setTextColor(muted)
        })
        row.addView(TextView(this).apply {
            text = value
            textSize = 15f
            setTextColor(ink)
            setPadding(0, dp(4), 0, 0)
        })
        val lp = LinearLayout.LayoutParams(-1, -2)
        lp.bottomMargin = dp(8)
        row.layoutParams = lp
        return row
    }

    private fun sectionLabel(text: String) = TextView(this).apply {
        this.text = text
        textSize = 13f
        setTextColor(muted)
        typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        setPadding(dp(2), dp(4), 0, dp(9))
    }

    private fun actionButton(text: String, primary: Boolean, action: () -> Unit): Button {
        return Button(this).apply {
            this.text = text
            isAllCaps = false
            textSize = 14f
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            setTextColor(if (primary) Color.WHITE else blue)
            background = rounded(if (primary) blue else Color.WHITE, 18)
            elevation = if (primary) dp(2).toFloat() else 0f
            setOnClickListener { action() }
        }
    }

    private fun goNext() {
        if (step == 5 && sourceChoice != "new") {
            Toast.makeText(this, "Перенос данных появится после реализации защищённого сервиса передачи. Сейчас можно продолжить без переноса.", Toast.LENGTH_LONG).show()
            sourceChoice = "new"
        }
        if (step == 10) {
            step = 11
        } else if (step < 11) {
            step = try { nativeNextStep(step) } catch (_: UnsatisfiedLinkError) { (step + 1).coerceAtMost(11) }
        } else {
            getSharedPreferences("giggle_setup", MODE_PRIVATE).edit()
                .putBoolean("setup_complete", true).apply()
            Toast.makeText(this, "Настройка завершена.", Toast.LENGTH_SHORT).show()
        }
        persist()
        render()
    }

    private fun goBack() {
        step = try { nativePreviousStep(step) } catch (_: UnsatisfiedLinkError) { (step - 1).coerceAtLeast(0) }
        persist()
        render()
    }

    private fun openSettings(intent: Intent) {
        try {
            startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(this, "Этот экран настроек недоступен на текущем устройстве.", Toast.LENGTH_LONG).show()
        }
    }

    private fun rounded(color: Int, radius: Int): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
        }

    private fun divider() = View(this).apply {
        setBackgroundColor(Color.rgb(237, 241, 243))
        layoutParams = LinearLayout.LayoutParams(-1, dp(1)).apply {
            leftMargin = dp(8); rightMargin = dp(8)
        }
    }

    private fun space(height: Int) = View(this).apply {
        layoutParams = LinearLayout.LayoutParams(1, dp(height))
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
