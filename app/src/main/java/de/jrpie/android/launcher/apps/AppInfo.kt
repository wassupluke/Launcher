package de.jrpie.android.launcher.apps

import android.app.Service
import android.content.Context
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import de.jrpie.android.launcher.getUserFromId
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Represents an app installed on the users device.
 * Contains the minimal amount of data required to identify the app.
 */
@Serializable
class AppInfo(val packageName: String, val activityName: String?, val user: Int = INVALID_USER) {

    fun serialize(): String {
        return Json.encodeToString(this)
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
        val activityList = launcherApps.getActivityList(packageName, userHandle)
        return activityList.firstOrNull { app -> app.name == activityName }
            ?: activityList.firstOrNull()
    }


    override fun toString(): String {
        return "AppInfo {package=$packageName, activity=$activityName, user=$user}"
    }

    companion object {
        const val INVALID_USER = -1

        fun deserialize(serialized: String): AppInfo {
            return Json.decodeFromString(serialized)
        }
    }
}