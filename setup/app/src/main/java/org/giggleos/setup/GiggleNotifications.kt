package org.giggleos.setup

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Stores only app identity and arrival time; notification text and actions are never persisted. */
object GiggleNotifications {
    private const val PREFS = "giggle_notifications"
    private const val KEY = "items"
    private const val MAX_ITEMS = 30

    data class Entry(val key: String, val label: String, val packageName: String, val postedAt: Long)

    fun hasAccess(context: Context): Boolean {
        val enabled = android.provider.Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        ) ?: return false
        return enabled.split(':').any { component ->
            component.contains(context.packageName, ignoreCase = true)
        }
    }

    fun entries(context: Context): List<Entry> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, "[]") ?: "[]"
        return try {
            val array = JSONArray(raw)
            (0 until array.length()).mapNotNull { index ->
                val item = array.optJSONObject(index) ?: return@mapNotNull null
                Entry(
                    item.optString("key"),
                    item.optString("label", "Приложение"),
                    item.optString("package"),
                    item.optLong("time")
                )
            }.sortedByDescending { it.postedAt }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun add(context: Context, key: String, packageName: String, postedAt: Long) {
        if (packageName == context.packageName) return
        val label = try {
            val app = context.packageManager.getApplicationInfo(packageName, 0)
            context.packageManager.getApplicationLabel(app).toString()
        } catch (_: Exception) {
            packageName.substringAfterLast('.')
        }
        val updated = entries(context).filterNot { it.key == key }.toMutableList()
        updated.add(Entry(key, label, packageName, postedAt))
        save(context, updated.sortedByDescending { it.postedAt }.take(MAX_ITEMS))
    }

    fun remove(context: Context, key: String) {
        save(context, entries(context).filterNot { it.key == key })
    }

    fun clear(context: Context) {
        save(context, emptyList())
    }

    private fun save(context: Context, items: List<Entry>) {
        val array = JSONArray()
        items.forEach { item ->
            array.put(JSONObject().apply {
                put("key", item.key)
                put("label", item.label)
                put("package", item.packageName)
                put("time", item.postedAt)
            })
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY, array.toString())
            .apply()
    }
}
