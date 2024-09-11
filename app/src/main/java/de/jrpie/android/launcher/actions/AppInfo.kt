package de.jrpie.android.launcher.actions

import android.app.Service
import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import de.jrpie.android.launcher.INVALID_USER
import de.jrpie.android.launcher.getUserFromId

/**
 * Stores information used to create [AppsRecyclerAdapter] rows.
 *
 * Represents an app installed on the users device.
 */
class AppInfo(var packageName: CharSequence? = null, var user: Int? = null) {
    var label: CharSequence? = null
    var icon: Drawable? = null
    var isSystemApp: Boolean = false

    fun getAppIcon(context: Context): Drawable {
        if (user != null && user != INVALID_USER) {
            val launcherApps =
                context.getSystemService(Service.LAUNCHER_APPS_SERVICE) as LauncherApps
            getUserFromId(user, context)?.let { userHandle ->
                launcherApps.getActivityList(packageName.toString(), userHandle).firstOrNull()
                    ?.let { app ->
                        return app.getBadgedIcon(0)
                    }
            }
        }
        return context.packageManager.getApplicationIcon(packageName.toString())
    }

    fun isInstalled(context: Context): Boolean {
        /* TODO: this should also check the user */
        try {
            context.packageManager.getPackageInfo(
                packageName.toString(),
                PackageManager.GET_ACTIVITIES
            )
            return true
        } catch (_: PackageManager.NameNotFoundException) {
        }
        return false
    }


}