package dev.catandbunny.cloudbuddy.data

import android.content.Context
import java.util.UUID

class InstallationIdStore(context: Context) {
    private val preferences = context.getSharedPreferences("cloud_buddy_installation", Context.MODE_PRIVATE)

    fun get(): String {
        preferences.getString(KEY_ID, null)?.let { return it }
        return UUID.randomUUID().toString().also { generated ->
            preferences.edit().putString(KEY_ID, generated).apply()
        }
    }

    private companion object {
        const val KEY_ID = "installation_id"
    }
}
