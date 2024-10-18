package de.jrpie.android.launcher

import androidx.preference.PreferenceManager
import de.jrpie.android.launcher.preferences.LauncherPreferences

class Application : android.app.Application() {
    override fun onCreate() {
        super.onCreate()

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        LauncherPreferences.init(preferences, this.resources)

    }
}