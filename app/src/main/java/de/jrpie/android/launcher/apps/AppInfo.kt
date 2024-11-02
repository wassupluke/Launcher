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
class AppInfo(val packageName: CharSequence, val activityName: CharSequence?, val user: Int = INVALID_USER) {

    // TODO: make activityName non nullable (breaking change to SharedPreferences!)

    fun serialize(): String {
        val u = user
        var ret = "$packageName;$u"
        activityName?.let { ret += ";$activityName" }

        return ret
    }

    override fun equals(other: Any?): Boolean {
        if(other is AppInfo) {
            return other.user == user && other.packageName == packageName
                    && other.activityName == activityName
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return packageName.hashCode()
    }

    fun getLauncherActivityInfo(
        context: Context
    ): LauncherActivityInfo? {
        val launcherApps = context.getSystemService(Service.LAUNCHER_APPS_SERVICE) as LauncherApps
        val userHandle = getUserFromId(user, context)
        val activityList = launcherApps.getActivityList(packageName.toString(), userHandle)
        return activityList.firstOrNull { app -> app.name == activityName }
            ?: activityList.firstOrNull()
    }


    override fun toString(): String {
        return "AppInfo {package=$packageName, activity=$activityName, user=$user}"
    }

    companion object {
        const val INVALID_USER = -1

        fun deserialize(serialized: String): AppInfo {
            val values = serialized.split(";")
            val packageName = values[0]
            val user = Integer.valueOf(values[1])
            val activityName = values.getOrNull(2)
            return AppInfo(packageName, activityName, user)
        }
    }
}