package de.jrpie.android.launcher

import android.app.Activity
import android.app.Service
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.UserHandle
import android.os.UserManager
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import de.jrpie.android.launcher.actions.Action
import de.jrpie.android.launcher.actions.AppInfo
import de.jrpie.android.launcher.actions.Gesture
import de.jrpie.android.launcher.ui.list.apps.AppsRecyclerAdapter
import de.jrpie.android.launcher.ui.tutorial.TutorialActivity


const val INVALID_USER = -1

/* Objects used by multiple activities */
val appsList: MutableList<AppInfo> = ArrayList()

/* Variables containing settings */
val displayMetrics = DisplayMetrics()

/* REQUEST CODES */

const val REQUEST_CHOOSE_APP = 1
const val REQUEST_CHOOSE_APP_FROM_FAVORITES = 2
const val REQUEST_UNINSTALL = 3

const val REQUEST_SET_DEFAULT_HOME = 42

/* Animate */

// Taken from https://stackoverflow.com/questions/47293269
fun View.blink(
    times: Int = Animation.INFINITE,
    duration: Long = 1000L,
    offset: Long = 20L,
    minAlpha: Float = 0.2f,
    maxAlpha: Float = 1.0f,
    repeatMode: Int = Animation.REVERSE
) {
    startAnimation(AlphaAnimation(minAlpha, maxAlpha).also {
        it.duration = duration
        it.startOffset = offset
        it.repeatMode = repeatMode
        it.repeatCount = times
    })
}

fun setDefaultHomeScreen(context: Context, checkDefault: Boolean = false) {

    if (checkDefault
        && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        && context is Activity
    ) {
        val roleManager = context.getSystemService(RoleManager::class.java)
        if (!roleManager.isRoleHeld(RoleManager.ROLE_HOME)) {
            context.startActivityForResult(
                roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME),
                REQUEST_SET_DEFAULT_HOME
            )
        }
        return
    }

    if (checkDefault) {
        val testIntent = Intent(Intent.ACTION_MAIN)
        testIntent.addCategory(Intent.CATEGORY_HOME)
        val defaultHome = testIntent.resolveActivity(context.packageManager)?.packageName
        if (defaultHome == context.packageName) {
            // Launcher is already the default home app
            return
        }
    }
    val intent = Intent(Settings.ACTION_HOME_SETTINGS)
    context.startActivity(intent)
}


fun getIntent(packageName: String, context: Context): Intent? {
    val intent: Intent? = context.packageManager.getLaunchIntentForPackage(packageName)
    intent?.addCategory(Intent.CATEGORY_LAUNCHER)
    return intent
}
/* --- */

fun getUserFromId(user: Int?, context: Context): UserHandle? {
    val userManager = context.getSystemService(Service.USER_SERVICE) as UserManager
    return userManager.userProfiles.firstOrNull { it.hashCode() == user }
}

fun getLauncherActivityInfo(
    packageName: String,
    user: Int?,
    context: Context
): LauncherActivityInfo? {
    val launcherApps = context.getSystemService(Service.LAUNCHER_APPS_SERVICE) as LauncherApps
    return getUserFromId(user, context)?.let { userHandle ->
        launcherApps.getActivityList(packageName, userHandle).firstOrNull()
    }
}

fun uninstallApp(appInfo: AppInfo, activity: Activity) {
    val packageName = appInfo.packageName.toString()
    val user = appInfo.user

    Log.i("Launcher", "uninstalling $packageName ($user)")
    val intent = Intent(Intent.ACTION_UNINSTALL_PACKAGE)
    intent.data = Uri.parse("package:$packageName")
    getUserFromId(user, activity)?.let { user ->
        intent.putExtra(Intent.EXTRA_USER, user)
    }

    intent.putExtra(Intent.EXTRA_RETURN_RESULT, true)
    activity.startActivityForResult(
        intent,
        REQUEST_UNINSTALL
    )
}

fun openNewTabWindow(urls: String, context: Context) {
    val uris = Uri.parse(urls)
    val intents = Intent(Intent.ACTION_VIEW, uris)
    val b = Bundle()
    b.putBoolean("new_window", true)
    intents.putExtras(b)
    context.startActivity(intents)
}

fun openAppSettings(
    appInfo: AppInfo,
    context: Context,
    sourceBounds: Rect? = null,
    opts: Bundle? = null
) {
    val launcherApps = context.getSystemService(Service.LAUNCHER_APPS_SERVICE) as LauncherApps
    getLauncherActivityInfo(appInfo.packageName.toString(), appInfo.user, context)?.let { app ->
        launcherApps.startAppDetailsActivity(app.componentName, app.user, sourceBounds, opts)
    }
}

fun openTutorial(context: Context) {
    context.startActivity(Intent(context, TutorialActivity::class.java))
}


/**
 * [loadApps] is used to speed up the [AppsRecyclerAdapter] loading time,
 * as it caches all the apps and allows for fast access to the data.
 */
fun loadApps(packageManager: PackageManager, context: Context) {
    val loadList = mutableListOf<AppInfo>()

    val launcherApps = context.getSystemService(Service.LAUNCHER_APPS_SERVICE) as LauncherApps
    val userManager = context.getSystemService(Service.USER_SERVICE) as UserManager

    // TODO: shortcuts - launcherApps.getShortcuts()
    val users = userManager.userProfiles
    for (user in users) {
        for (activityInfo in launcherApps.getActivityList(null, user)) {
            val app = AppInfo()
            app.label = activityInfo.label
            app.packageName = activityInfo.applicationInfo.packageName
            app.icon = activityInfo.getBadgedIcon(0)
            app.user = user.hashCode()
            app.isSystemApp =
                activityInfo.applicationInfo.flags.and(ApplicationInfo.FLAG_SYSTEM) != 0
            loadList.add(app)
        }
    }


    // fallback option
    if (loadList.isEmpty()) {
        Log.i("Launcher", "using fallback option to load packages")
        val i = Intent(Intent.ACTION_MAIN, null)
        i.addCategory(Intent.CATEGORY_LAUNCHER)
        val allApps = packageManager.queryIntentActivities(i, 0)
        for (ri in allApps) {
            val app = AppInfo()
            app.label = ri.loadLabel(packageManager)
            app.packageName = ri.activityInfo.packageName
            app.icon = ri.activityInfo.loadIcon(packageManager)
            loadList.add(app)
        }
    }
    loadList.sortBy { it.label.toString() }
    appsList.clear()
    appsList.addAll(loadList)
}


// Used in Tutorial and Settings `ActivityOnResult`
fun saveListActivityChoice(data: Intent?) {
    val forGesture = data?.getStringExtra("forGesture") ?: return
    Gesture.byId(forGesture)?.let { Action.setActionForGesture(it, Action.fromIntent(data)) }
}

// Taken from https://stackoverflow.com/a/50743764/12787264
fun openSoftKeyboard(context: Context, view: View) {
    view.requestFocus()
    // open the soft keyboard
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
}

// Taken from: https://stackoverflow.com/a/30340794/12787264
fun transformGrayscale(imageView: ImageView) {
    val matrix = ColorMatrix()
    matrix.setSaturation(0f)

    val filter = ColorMatrixColorFilter(matrix)
    imageView.colorFilter = filter
}
