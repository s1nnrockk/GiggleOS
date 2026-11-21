package org.giggleos.setup

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

/** Searchable app drawer backed by the actual launchable apps installed on the host Android device. */
class AppDrawerActivity : Activity() {
    private val ink = Color.rgb(27, 43, 56)
    private val muted = Color.rgb(101, 119, 132)
    private val paper = Color.rgb(238, 244, 246)
    private lateinit var list: LinearLayout
    private lateinit var search: EditText
    private data class AppEntry(val label: String, val packageName: String, val className: String, val icon: android.graphics.drawable.Drawable)

    private var apps: List<AppEntry> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = paper
        window.navigationBarColor = paper
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        loadApps()
        render()
    }

    private fun loadApps() {
        val launcher = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        apps = packageManager.queryIntentActivities(launcher, 0)
            .mapNotNull { info ->
                val activity = info.activityInfo ?: return@mapNotNull null
                if (activity.packageName == packageName) return@mapNotNull null
                AppEntry(
                    info.loadLabel(packageManager)?.toString()?.trim().orEmpty().ifBlank { activity.name },
                    activity.packageName,
                    activity.name,
                    info.loadIcon(packageManager)
                )
            }
            .distinctBy { it.packageName to it.className }
            .sortedBy { it.label.lowercase(java.util.Locale.getDefault()) }
    }

    private fun render() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(paper)
            setPadding(dp(20), dp(14), dp(20), dp(16))
        }
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        header.addView(TextView(this).apply {
            text = "Приложения"
            textSize = 29f
            letterSpacing = -.035f
            setTextColor(ink)
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        }, LinearLayout.LayoutParams(0, dp(48), 1f))
        header.addView(TextView(this).apply {
            text = "${apps.size} установлено"
            textSize = 11f
            setTextColor(muted)
        })
        root.addView(header)

        search = EditText(this).apply {
            hint = "Поиск приложений"
            textSize = 14f
            setSingleLine(true)
            setTextColor(ink)
            setHintTextColor(muted)
            background = rounded(Color.WHITE, 17)
            setPadding(dp(15), 0, dp(15), 0)
            imeOptions = android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH
        }
        root.addView(search, LinearLayout.LayoutParams(-1, dp(50)).apply {
            topMargin = dp(10)
            bottomMargin = dp(10)
        })

        val scroll = ScrollView(this).apply { clipToPadding = false }
        list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(4), 0, dp(16))
        }
        scroll.addView(list)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                renderApps(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })
        renderApps("")
        setContentView(root)
    }

    private fun renderApps(query: String) {
        list.removeAllViews()
        val filtered = apps.filter { it.label.contains(query, true) }
        if (filtered.isEmpty()) {
            list.addView(TextView(this).apply {
                text = if (apps.isEmpty()) "На устройстве не найдено приложений с ярлыками запуска."
                else "Ничего не найдено"
                textSize = 14f
                gravity = Gravity.CENTER
                setTextColor(muted)
                setPadding(dp(12), dp(36), dp(12), dp(36))
            })
            return
        }
        filtered.chunked(4).forEach { rowApps ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.TOP
            }
            rowApps.forEach { app ->
                val tile = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                    isClickable = true
                    isFocusable = true
                    setPadding(dp(3), dp(5), dp(3), dp(9))
                    val icon = ImageView(this@AppDrawerActivity).apply {
                        setImageDrawable(app.icon)
                        scaleType = ImageView.ScaleType.FIT_CENTER
                        contentDescription = app.label
                    }
                    addView(icon, LinearLayout.LayoutParams(dp(54), dp(54)))
                    addView(TextView(this@AppDrawerActivity).apply {
                        text = app.label
                        textSize = 10f
                        gravity = Gravity.CENTER
                        setTextColor(ink)
                        maxLines = 2
                        ellipsize = android.text.TextUtils.TruncateAt.END
                        setPadding(0, dp(5), 0, 0)
                    }, LinearLayout.LayoutParams(-1, dp(34)))
                    setOnClickListener { launchApp(app) }
                    contentDescription = "Открыть ${app.label}"
                }
                row.addView(tile, LinearLayout.LayoutParams(0, dp(98), 1f))
            }
            repeat(4 - rowApps.size) {
                row.addView(View(this), LinearLayout.LayoutParams(0, dp(98), 1f))
            }
            list.addView(row)
        }
    }

    private fun launchApp(app: AppEntry) {
        try {
            val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
                .setComponent(ComponentName(app.packageName, app.className))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(this, "Не удалось открыть приложение.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radius).toFloat()
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density + .5f).toInt()
}
