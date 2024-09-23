package de.jrpie.android.launcher.apps

import android.app.Service
import android.content.Context
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import de.jrpie.android.launcher.getUserFromId

/**
 * Represents an app installed on the users device.
 * Contains the minimal amount of data required to identify the app.
 */
class AppInfo(val packageName: CharSequence, val user: Int = INVALID_USER) {

    fun serialize(): String {
        val u = user
        return "$packageName;$u"
    }

    fun getLauncherActivityInfo(
        context: Context
    ): LauncherActivityInfo? {
        val launcherApps = context.getSystemService(Service.LAUNCHER_APPS_SERVICE) as LauncherApps
        return getUserFromId(user, context)?.let { userHandle ->
            launcherApps.getActivityList(packageName.toString(), userHandle).firstOrNull()
        }
    }


    companion object {
        const val INVALID_USER = -1

        fun deserialize(serialized: String): AppInfo {
            val values = serialized.split(";")
            val packageName = values[0]
            val user = Integer.valueOf(values[1])
            return AppInfo(packageName, user)
        }
    }
}