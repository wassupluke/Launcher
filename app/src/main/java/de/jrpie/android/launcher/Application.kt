package de.jrpie.android.launcher

import android.os.Build
import android.os.Build.VERSION_CODES
import androidx.preference.PreferenceManager
import de.jrpie.android.launcher.actions.TorchManager
import de.jrpie.android.launcher.apps.AppInfo
import de.jrpie.android.launcher.preferences.LauncherPreferences

class Application : android.app.Application() {
    var torchManager: TorchManager? = null
    var customAppNames: HashMap<AppInfo, String> = HashMap()
    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= VERSION_CODES.M) {
            torchManager = TorchManager(this)
        }

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        LauncherPreferences.init(preferences, this.resources)

        customAppNames = LauncherPreferences.apps().customNames()

        LauncherPreferences.getSharedPreferences()
            .registerOnSharedPreferenceChangeListener { _, pref ->
                if (pref == getString(R.string.settings_apps_custom_names_key)) {
                    customAppNames = LauncherPreferences.apps().customNames()
                }
            }
    }
}
