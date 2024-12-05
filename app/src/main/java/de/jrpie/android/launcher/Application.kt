package de.jrpie.android.launcher

import android.content.SharedPreferences
import android.os.Build
import android.os.Build.VERSION_CODES
import androidx.preference.PreferenceManager
import de.jrpie.android.launcher.actions.TorchManager
import de.jrpie.android.launcher.apps.AppInfo
import de.jrpie.android.launcher.preferences.LauncherPreferences

class Application : android.app.Application() {
    var torchManager: TorchManager? = null
    private var customAppNames: HashMap<AppInfo, String>? = null
    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, pref ->
        if (pref == getString(R.string.settings_apps_custom_names_key)) {
            customAppNames = LauncherPreferences.apps().customNames()
        }
    }

    override fun onCreate() {
        super.onCreate()
        // TODO  Error: Invalid resource ID 0x00000000.
        // DynamicColors.applyToActivitiesIfAvailable(this)


        if (Build.VERSION.SDK_INT >= VERSION_CODES.M) {
            torchManager = TorchManager(this)
        }

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        LauncherPreferences.init(preferences, this.resources)

        LauncherPreferences.getSharedPreferences()
            .registerOnSharedPreferenceChangeListener(listener)
    }

    fun getCustomAppNames(): HashMap<AppInfo, String> {
        return (customAppNames ?: LauncherPreferences.apps().customNames() ?: HashMap())
            .also { customAppNames = it }
    }
}
