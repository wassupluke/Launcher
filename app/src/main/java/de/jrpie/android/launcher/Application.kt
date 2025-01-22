package de.jrpie.android.launcher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.os.AsyncTask
import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.UserHandle
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import de.jrpie.android.launcher.actions.TorchManager
import de.jrpie.android.launcher.apps.AppInfo
import de.jrpie.android.launcher.apps.DetailedAppInfo
import de.jrpie.android.launcher.preferences.LauncherPreferences
import de.jrpie.android.launcher.preferences.migratePreferencesToNewVersion
import de.jrpie.android.launcher.preferences.resetPreferences

class Application : android.app.Application() {
    val apps = MutableLiveData<List<DetailedAppInfo>>()

    private val profileAvailabilityBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // TODO: only update specific apps
            // use Intent.EXTRA_USER
            loadApps()
        }
    }

    // TODO: only update specific apps
    private val launcherAppsCallback = object : LauncherApps.Callback() {
        override fun onPackageRemoved(p0: String?, p1: UserHandle?) {
            loadApps()
        }

        override fun onPackageAdded(p0: String?, p1: UserHandle?) {
            loadApps()
        }

        override fun onPackageChanged(p0: String?, p1: UserHandle?) {
            loadApps()
        }

        override fun onPackagesAvailable(p0: Array<out String>?, p1: UserHandle?, p2: Boolean) {
            // TODO
        }

        override fun onPackagesSuspended(packageNames: Array<out String>?, user: UserHandle?) {
            // TODO
        }

        override fun onPackagesUnsuspended(packageNames: Array<out String>?, user: UserHandle?) {
            // TODO
        }

        override fun onPackagesUnavailable(p0: Array<out String>?, p1: UserHandle?, p2: Boolean) {
            // TODO
        }

        override fun onPackageLoadingProgressChanged(
            packageName: String,
            user: UserHandle,
            progress: Float
        ) {
            // TODO
        }

        override fun onShortcutsChanged(
            packageName: String,
            shortcuts: MutableList<ShortcutInfo>,
            user: UserHandle
        ) {
            // TODO
        }
    }

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


        // Try to restore old preferences
        migratePreferencesToNewVersion(this)

        // First time opening the app: set defaults and start tutorial
        if (!LauncherPreferences.internal().started()) {
            resetPreferences(this)

            LauncherPreferences.internal().started(true)
            openTutorial(this)
        }


        LauncherPreferences.getSharedPreferences()
            .registerOnSharedPreferenceChangeListener(listener)


        val launcherApps = getSystemService(LAUNCHER_APPS_SERVICE) as LauncherApps
        launcherApps.registerCallback(launcherAppsCallback)

        if (Build.VERSION.SDK_INT >= VERSION_CODES.N) {
            val filter = IntentFilter().also {
                if (Build.VERSION.SDK_INT >= VERSION_CODES.VANILLA_ICE_CREAM) {
                    it.addAction(Intent.ACTION_PROFILE_AVAILABLE)
                    it.addAction(Intent.ACTION_PROFILE_UNAVAILABLE)
                } else {
                    it.addAction(Intent.ACTION_MANAGED_PROFILE_AVAILABLE)
                    it.addAction(Intent.ACTION_MANAGED_PROFILE_UNAVAILABLE)
                }
            }
            ContextCompat.registerReceiver(this, profileAvailabilityBroadcastReceiver, filter,
                ContextCompat.RECEIVER_EXPORTED
            )
        }

        loadApps()
    }

    fun getCustomAppNames(): HashMap<AppInfo, String> {
        return (customAppNames ?: LauncherPreferences.apps().customNames() ?: HashMap())
            .also { customAppNames = it }
    }

    private fun loadApps() {
        AsyncTask.execute { apps.postValue(getApps(packageManager, applicationContext)) }
    }
}
