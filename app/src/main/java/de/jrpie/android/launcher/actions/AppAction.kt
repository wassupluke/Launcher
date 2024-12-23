package de.jrpie.android.launcher.actions

import android.app.AlertDialog
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.Log
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.apps.AppInfo
import de.jrpie.android.launcher.apps.AppInfo.Companion.INVALID_USER
import de.jrpie.android.launcher.apps.DetailedAppInfo
import de.jrpie.android.launcher.ui.list.apps.openSettings
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("action:app")
class AppAction(val app: AppInfo) : Action {

    override fun invoke(context: Context, rect: Rect?): Boolean {
        val packageName = app.packageName
        if (app.user != INVALID_USER) {
            val launcherApps =
                context.getSystemService(Service.LAUNCHER_APPS_SERVICE) as LauncherApps
            app.getLauncherActivityInfo(context)?.let { app ->
                Log.i("Launcher", "Starting ${this.app}")
                launcherApps.startMainActivity(app.componentName, app.user, rect, null)
                return true
            }
        }

        context.packageManager.getLaunchIntentForPackage(packageName)?.let {
            it.addCategory(Intent.CATEGORY_LAUNCHER)
            context.startActivity(it)
            return true
        }

        /* check if app is installed */
        if (isAvailable(context)) {
            AlertDialog.Builder(
                context,
                R.style.AlertDialogCustom
            )
                .setTitle(context.getString(R.string.alert_cant_open_title))
                .setMessage(context.getString(R.string.alert_cant_open_message))
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    app.openSettings(context)
                }
                .setNegativeButton(android.R.string.cancel, null)
                .setIcon(android.R.drawable.ic_dialog_info)
                .show()
            return true
        }
        return false
    }

    override fun label(context: Context): String {
        return DetailedAppInfo.fromAppInfo(app, context)?.getCustomLabel(context).toString()
    }

    override fun getIcon(context: Context): Drawable? {
        return DetailedAppInfo.fromAppInfo(app, context)?.icon
    }

    override fun isAvailable(context: Context): Boolean {
        // check if app is installed
        return DetailedAppInfo.fromAppInfo(app, context) != null
    }
}