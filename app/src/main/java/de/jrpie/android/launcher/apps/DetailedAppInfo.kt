package de.jrpie.android.launcher.apps

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherActivityInfo
import android.graphics.drawable.Drawable

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
        AppInfo(activityInfo.applicationInfo.packageName, activityInfo.user.hashCode()),
        activityInfo.label,
        activityInfo.getBadgedIcon(0),
        activityInfo.applicationInfo.flags.and(ApplicationInfo.FLAG_SYSTEM) != 0
    )

    companion object {
        fun fromAppInfo(appInfo: AppInfo, context: Context): DetailedAppInfo? {
            return appInfo.getLauncherActivityInfo(context)?.let { DetailedAppInfo(it) }
        }
    }
}