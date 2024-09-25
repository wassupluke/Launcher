package de.jrpie.android.launcher.actions

import android.app.AlertDialog
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.LauncherApps
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.Log
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.apps.AppInfo
import de.jrpie.android.launcher.apps.AppInfo.Companion.INVALID_USER
import de.jrpie.android.launcher.apps.DetailedAppInfo
import de.jrpie.android.launcher.getIntent
import de.jrpie.android.launcher.openAppSettings

class AppAction(private var appInfo: AppInfo) : Action {

    override fun invoke(context: Context, rect: Rect?): Boolean {
        val packageName = appInfo.packageName.toString()
        if (appInfo.user != INVALID_USER) {
            val launcherApps =
                context.getSystemService(Service.LAUNCHER_APPS_SERVICE) as LauncherApps
            appInfo.getLauncherActivityInfo(context)?.let { app ->
                Log.i("Launcher", "Starting $appInfo")
                launcherApps.startMainActivity(app.componentName, app.user, rect, null)
                return true
            }
        }

        val intent = getIntent(packageName, context)

        if (intent != null) {
            context.startActivity(intent)
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
                    openAppSettings(appInfo, context)
                }
                .setNegativeButton(android.R.string.cancel, null)
                .setIcon(android.R.drawable.ic_dialog_info)
                .show()
            return true
        }
        return false
    }

    override fun label(context: Context): String {
        return DetailedAppInfo.fromAppInfo(appInfo, context)?.label.toString()
    }

    override fun getIcon(context: Context): Drawable? {
        return DetailedAppInfo.fromAppInfo(appInfo, context)?.icon
    }

    override fun isAvailable(context: Context): Boolean {
        // check if app is installed
        return DetailedAppInfo.fromAppInfo(appInfo, context) != null;
    }

    override fun bindToGesture(editor: SharedPreferences.Editor, id: String) {
        val u = appInfo.user

        // TODO: replace this by AppInfo#serialize (breaking change to SharedPreferences!)
        var app = appInfo.packageName.toString()
        if (appInfo.activityName != null) {
            app += ";${appInfo.activityName}"
        }
        editor
            .putString("$id.app", app)
            .putInt("$id.user", u)
    }

    override fun writeToIntent(intent: Intent) {
        intent.putExtra("action_id", "${appInfo.packageName};${appInfo.activityName}")
        intent.putExtra("user", appInfo.user)
    }
}