package de.jrpie.android.launcher.apps

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherActivityInfo
import android.graphics.drawable.Drawable
import android.util.Log
import de.jrpie.android.launcher.Application
import de.jrpie.android.launcher.preferences.LauncherPreferences

/**
 * Stores information used to create [AppsRecyclerAdapter] rows.
 */
class DetailedAppInfo(
    val app: AppInfo,
    val label: CharSequence,
    val icon: Drawable,
    val isSystemApp: Boolean = false,
) {

    constructor(activityInfo: LauncherActivityInfo) : this(
        AppInfo(
            activityInfo.applicationInfo.packageName,
            activityInfo.name,
            activityInfo.user.hashCode()
        ),
        activityInfo.label,
        activityInfo.getBadgedIcon(0),
        activityInfo.applicationInfo.flags.and(ApplicationInfo.FLAG_SYSTEM) != 0
    )

    fun getCustomLabel(context: Context): CharSequence {
        val map = (context.applicationContext as? Application)?.customAppNames ?: return label

        return map[app] ?: label
    }

    fun setCustomLabel(label: CharSequence?) {

        Log.i("Launcher", "Setting custom label for ${this.app} to ${label}.")
        val map = LauncherPreferences.apps().customNames() ?: HashMap<AppInfo, String>()

        if (label.isNullOrEmpty()) {
            map.remove(app)
        } else {
            map[app] = label.toString()
        }

        LauncherPreferences.apps().customNames(map)
    }

    companion object {
        fun fromAppInfo(appInfo: AppInfo, context: Context): DetailedAppInfo? {
            return appInfo.getLauncherActivityInfo(context)?.let { DetailedAppInfo(it) }
        }
    }
}