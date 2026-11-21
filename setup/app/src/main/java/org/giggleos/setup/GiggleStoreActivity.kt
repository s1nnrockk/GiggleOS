package org.giggleos.setup

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

/** Lightweight store front-end. Actual app delivery is delegated to the user's installed store. */
class GiggleStoreActivity : Activity() {
    private val colors get() = GiggleAppearance.colors(this)
    private val ink get() = colors.text
    private val muted get() = colors.muted
    private val paper get() = colors.background
    private lateinit var results: LinearLayout
    private lateinit var search: EditText

    private data class Listing(
        val name: String,
        val category: String,
        val description: String,
        val packageName: String,
        val tint: Int
    )

    private val catalog = listOf(
        Listing("Google Chrome", "Браузеры", "Быстрый браузер и синхронизация Google", "com.android.chrome", 0xFF4C8DCE.toInt()),
        Listing("YouTube", "Видео", "Видео, каналы и подписки", "com.google.android.youtube", 0xFFE05252.toInt()),
        Listing("Google Maps", "Навигация", "Карты, маршруты и места", "com.google.android.apps.maps", 0xFF4B9B79.toInt()),
        Listing("WhatsApp", "Общение", "Сообщения и звонки", "com.whatsapp", 0xFF39A979.toInt()),
        Listing("Telegram", "Общение", "Облачные чаты и каналы", "org.telegram.messenger", 0xFF419BCB.toInt()),
        Listing("Spotify", "Музыка", "Музыка, плейлисты и подкасты", "com.spotify.music", 0xFF398B62.toInt()),
        Listing("Roblox", "Игры", "Игры и пользовательские миры", "com.roblox.client", 0xFF4C5965.toInt()),
        Listing("Google Photos", "Фото", "Резервное копирование и галерея", "com.google.android.apps.photos", 0xFF648AC8.toInt())
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = paper
        window.navigationBarColor = paper
        @Suppress("DEPRECATION")
        run { window.decorView.systemUiVisibility = if (colors.dark) 0 else View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR }
        render()
    }

    private fun render() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(paper)
            setPadding(dp(20), dp(14), dp(20), dp(16))
        }
        val header = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        header.addView(TextView(this).apply {
            text = "Магазин"
            textSize = 29f
            letterSpacing = -.035f
            setTextColor(ink)
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        }, LinearLayout.LayoutParams(0, dp(46), 1f))
        header.addView(TextView(this).apply {
            text = "GIGGLE STORE"
            textSize = 10f
            letterSpacing = .12f
            setTextColor(colors.accent)
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        })
        root.addView(header)

        val hero = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(colors.heroStart, colors.heroEnd)).apply { cornerRadius = dp(24).toFloat() }
            setPadding(dp(18), dp(17), dp(18), dp(17))
        }
        hero.addView(TextView(this).apply {
            text = "Приложения для вашего устройства"
            textSize = 18f
            setTextColor(Color.WHITE)
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        })
        hero.addView(TextView(this).apply {
            text = "Открывайте страницы приложений в Google Play. Если Play Маркет не установлен, попробуем открыть веб-версию."
            textSize = 12f
            setTextColor(0xFFE8F1F4.toInt())
            setPadding(0, dp(6), 0, dp(12))
        })
        val play = actionText("Открыть Google Play", Color.WHITE, Color.rgb(37, 62, 75))
        play.setOnClickListener { openPlaySearch("") }
        hero.addView(play, LinearLayout.LayoutParams(-1, dp(42)))
        root.addView(hero, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(12) })

        search = EditText(this).apply {
            hint = "Найти приложение или игру"
            textSize = 14f
            setSingleLine(true)
            setTextColor(ink)
            setHintTextColor(muted)
            background = rounded(colors.surface, 16)
            setPadding(dp(15), 0, dp(15), 0)
        }
        root.addView(search, LinearLayout.LayoutParams(-1, dp(50)).apply { topMargin = dp(13); bottomMargin = dp(8) })

        val scroll = ScrollView(this).apply { clipToPadding = false }
        results = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, dp(5), 0, dp(16)) }
        scroll.addView(results)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { renderListings(s?.toString().orEmpty()) }
            override fun afterTextChanged(s: Editable?) = Unit
        })
        renderListings("")
        setContentView(root)
    }

    private fun renderListings(query: String) {
        results.removeAllViews()
        val filtered = catalog.filter { it.name.contains(query, true) || it.category.contains(query, true) || it.description.contains(query, true) }
        val title = TextView(this).apply {
            text = if (query.isBlank()) "Популярные приложения" else "Результаты"
            textSize = 17f
            setTextColor(ink)
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            setPadding(dp(2), dp(8), 0, dp(10))
        }
        results.addView(title)
        if (filtered.isEmpty()) {
            results.addView(TextView(this).apply {
                text = "В локальной подборке ничего нет. Искать «$query» в Google Play?"
                textSize = 13f
                setTextColor(muted)
                setPadding(dp(8), dp(18), dp(8), dp(12))
            })
            results.addView(actionText("Искать в Google Play", Color.WHITE, colors.accent).apply { setOnClickListener { openPlaySearch(query) } })
            return
        }
        filtered.forEach { app ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                background = rounded(colors.surface, 19)
                setPadding(dp(12), dp(11), dp(12), dp(11))
            }
            val mark = TextView(this).apply {
                text = app.name.take(1).uppercase()
                textSize = 21f
                gravity = Gravity.CENTER
                setTextColor(Color.WHITE)
                typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                background = rounded(app.tint, 15)
            }
            card.addView(mark, LinearLayout.LayoutParams(dp(48), dp(48)))
            val info = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(11), 0, dp(7), 0)
                addView(TextView(this@GiggleStoreActivity).apply { text = app.name; textSize = 14f; setTextColor(ink); typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL) })
                addView(TextView(this@GiggleStoreActivity).apply { text = app.category + " · " + app.description; textSize = 10f; setTextColor(muted); maxLines = 2; setPadding(0, dp(3), 0, 0) })
            }
            card.addView(info, LinearLayout.LayoutParams(0, -2, 1f))
            val open = TextView(this).apply {
                text = "Открыть"
                textSize = 11f
                gravity = Gravity.CENTER
                setTextColor(Color.WHITE)
                background = rounded(colors.accent, 14)
                setPadding(dp(11), dp(9), dp(11), dp(9))
                setOnClickListener { openListing(app) }
            }
            card.addView(open)
            card.setOnClickListener { openListing(app) }
            val lp = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(8) }
            results.addView(card, lp)
        }
        results.addView(TextView(this).apply {
            text = "Каталог — подборка ссылок, а не собственный сервер распространения APK. Установка и обновления выполняются магазином, который выберет пользователь."
            textSize = 10f
            setTextColor(muted)
            setPadding(dp(3), dp(10), dp(3), dp(8))
        })
    }

    private fun openListing(app: Listing) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${Uri.encode(app.packageName)}")))
        } catch (_: Exception) {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${Uri.encode(app.packageName)}")))
            } catch (_: Exception) {
                Toast.makeText(this, "Google Play недоступен. Проверьте подключение к интернету.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun openPlaySearch(query: String) {
        val safe = Uri.encode(query.ifBlank { "apps" })
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=$safe&c=apps")))
        } catch (_: Exception) {
            try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/search?q=$safe&c=apps"))) }
            catch (_: Exception) { Toast.makeText(this, "Не удалось открыть магазин.", Toast.LENGTH_SHORT).show() }
        }
    }

    private fun actionText(label: String, textColor: Int, backgroundColor: Int) = TextView(this).apply {
        text = label
        textSize = 13f
        gravity = Gravity.CENTER
        setTextColor(textColor)
        background = rounded(backgroundColor, 14)
        isClickable = true
        isFocusable = true
    }

    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply { setColor(color); cornerRadius = dp(radius).toFloat() }
    private fun dp(value: Int): Int = (value * resources.displayMetrics.density + .5f).toInt()
}
