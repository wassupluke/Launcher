package de.jrpie.android.launcher

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.KeyEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import android.widget.Switch
import android.widget.Toast
import de.jrpie.android.launcher.list.ListActivity
import de.jrpie.android.launcher.list.apps.AppInfo
import de.jrpie.android.launcher.list.apps.AppsRecyclerAdapter
import de.jrpie.android.launcher.list.other.LauncherAction
import de.jrpie.android.launcher.settings.SettingsActivity
import de.jrpie.android.launcher.settings.intendedSettingsPause
import de.jrpie.android.launcher.tutorial.TutorialActivity


/* Preference Key Constants */

const val PREF_DOMINANT = "custom_dominant"
const val PREF_VIBRANT = "custom_vibrant"
const val PREF_THEME = "theme"

const val PREF_SCREEN_TIMEOUT_DISABLED = "disableTimeout"
const val PREF_SCREEN_FULLSCREEN = "useFullScreen"
const val PREF_DATE_FORMAT = "dateFormat"

const val PREF_DOUBLE_ACTIONS_ENABLED = "enableDoubleActions"
const val PREF_EDGE_ACTIONS_ENABLED = "enableEdgeActions"
const val PREF_SEARCH_AUTO_LAUNCH = "searchAutoLaunch"
const val PREF_SEARCH_AUTO_KEYBOARD = "searchAutoKeyboard"

const val PREF_SLIDE_SENSITIVITY = "slideSensitivity"

const val PREF_STARTED = "startedBefore"
const val PREF_STARTED_TIME = "firstStartup"

const val PREF_VERSION = "version"

/* Objects used by multiple activities */
val appsList: MutableList<AppInfo> = ArrayList()

/* Variables containing settings */
val displayMetrics = DisplayMetrics()

var dominantColor = 0
var vibrantColor = 0

/* REQUEST CODES */

const val REQUEST_CHOOSE_APP = 1
const val REQUEST_UNINSTALL = 2

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

fun getPreferences(context: Context): SharedPreferences{
    return context.getSharedPreferences(
        context.getString(R.string.preference_file_key),
        Context.MODE_PRIVATE
    )
}

/* Activity related */

fun isInstalled(uri: String, context: Context): Boolean {
    if (uri.startsWith("launcher:")) return true // All internal actions

    try {
        context.packageManager.getPackageInfo(uri, PackageManager.GET_ACTIVITIES)
        return true
    } catch (_: PackageManager.NameNotFoundException) { }
    return false
}

private fun getIntent(packageName: String, context: Context): Intent? {
    val intent: Intent? = context.packageManager.getLaunchIntentForPackage(packageName)
    intent?.addCategory(Intent.CATEGORY_LAUNCHER)
    return intent
}

fun launch(
    data: String, activity: Activity,
    animationIn: Int = android.R.anim.fade_in, animationOut: Int = android.R.anim.fade_out
) {

    if (LauncherAction.isOtherAction(data)) { // [type]:[info]
        LauncherAction.byId(data)?.let {it.launch(activity) }
    }
    else launchApp(data, activity) // app

    activity.overridePendingTransition(animationIn, animationOut)
}

/* Media player actions */

fun audioNextTrack(activity: Activity) {

    val mAudioManager = activity.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    val eventTime: Long = SystemClock.uptimeMillis()

    val downEvent = KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT, 0)
    mAudioManager.dispatchMediaKeyEvent(downEvent)

    val upEvent = KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_NEXT, 0)
    mAudioManager.dispatchMediaKeyEvent(upEvent)
}

fun audioPreviousTrack(activity: Activity) {
    val mAudioManager = activity.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    val eventTime: Long = SystemClock.uptimeMillis()

    val downEvent =
        KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS, 0)
    mAudioManager.dispatchMediaKeyEvent(downEvent)

    val upEvent = KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PREVIOUS, 0)
    mAudioManager.dispatchMediaKeyEvent(upEvent)
}

fun audioVolumeUp(activity: Activity) {
    val audioManager =
        activity.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    audioManager.adjustStreamVolume(
        AudioManager.STREAM_MUSIC,
        AudioManager.ADJUST_RAISE,
        AudioManager.FLAG_SHOW_UI
    )
}

fun audioVolumeDown(activity: Activity) {
    val audioManager =
        activity.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    audioManager.adjustStreamVolume(
        AudioManager.STREAM_MUSIC,
        AudioManager.ADJUST_LOWER,
        AudioManager.FLAG_SHOW_UI
    )
}

/* --- */

fun launchApp(packageName: String, context: Context) {
    val intent = getIntent(packageName, context)

    if (intent != null) {
        context.startActivity(intent)
    } else {
        if (isInstalled(packageName, context)){

            AlertDialog.Builder(
                context,
                R.style.AlertDialogCustom
            )
                .setTitle(context.getString(R.string.alert_cant_open_title))
                .setMessage(context.getString(R.string.alert_cant_open_message))
                .setPositiveButton(android.R.string.ok
                ) { _, _ ->
                    openAppSettings(
                        packageName,
                        context
                    )
                }
                .setNegativeButton(android.R.string.cancel, null)
                .setIcon(android.R.drawable.ic_dialog_info)
                .show()
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.toast_cant_open_message),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

fun openNewTabWindow(urls: String, context: Context) {
    val uris = Uri.parse(urls)
    val intents = Intent(Intent.ACTION_VIEW, uris)
    val b = Bundle()
    b.putBoolean("new_window", true)
    intents.putExtras(b)
    context.startActivity(intents)
}

/* Settings related functions */

fun getSavedTheme(context: Context) : String {
    return getPreferences(context).getString(PREF_THEME, "finn").toString()
}

fun saveTheme(context: Context, themeName: String) : String {
    getPreferences(context).edit()
        .putString(PREF_THEME, themeName)
        .apply()

    return themeName
}

fun resetToDefaultTheme(activity: Activity) {
    dominantColor = activity.resources.getColor(R.color.finnmglasTheme_background_color)
    vibrantColor = activity.resources.getColor(R.color.finnmglasTheme_accent_color)

    getPreferences(activity).edit()
        .putInt(PREF_DOMINANT, dominantColor)
        .putInt(PREF_VIBRANT, vibrantColor)
        .apply()

    saveTheme(activity,"finn")
    loadSettings(activity)

    intendedSettingsPause = true
    activity.recreate()
}

fun resetToDarkTheme(activity: Activity) {
    dominantColor = activity.resources.getColor(R.color.darkTheme_background_color)
    vibrantColor = activity.resources.getColor(R.color.darkTheme_accent_color)

    getPreferences(activity).edit()
        .putInt(PREF_DOMINANT, dominantColor)
        .putInt(PREF_VIBRANT, vibrantColor)
        .apply()

    saveTheme(activity,"dark")

    intendedSettingsPause = true
    activity.recreate()
}


fun openAppSettings(pkg: String, context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    intent.data = Uri.parse("package:$pkg")
    context.startActivity(intent)
}

fun openSettings(activity: Activity) {
    activity.startActivity(Intent(activity, SettingsActivity::class.java))
}

fun openTutorial(activity: Activity){
    activity.startActivity(Intent(activity, TutorialActivity::class.java))
}

fun openAppsList(activity: Activity){
    val intent = Intent(activity, ListActivity::class.java)
    intent.putExtra("intention", ListActivity.ListActivityIntention.VIEW.toString())
    intendedSettingsPause = true
    activity.startActivity(intent)
}

/**
 * [loadApps] is used to speed up the [AppsRecyclerAdapter] loading time,
 * as it caches all the apps and allows for fast access to the data.
 */
fun loadApps(packageManager: PackageManager) {
    val loadList = mutableListOf<AppInfo>()

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
    loadList.sortBy { it.label.toString() }

    appsList.clear()
    appsList.addAll(loadList)
}

fun loadSettings(context: Context) {
    val preferences = getPreferences(context)
    dominantColor = preferences.getInt(PREF_DOMINANT, 0)
    vibrantColor = preferences.getInt(PREF_VIBRANT, 0)
}

fun resetSettings(context: Context) {

    val editor = getPreferences(context).edit()

    // set default theme
    dominantColor = context.resources.getColor(R.color.finnmglasTheme_background_color)
    vibrantColor = context.resources.getColor(R.color.finnmglasTheme_accent_color)

    editor
        .putInt(PREF_DOMINANT, dominantColor)
        .putInt(PREF_VIBRANT, vibrantColor)
        .putString(PREF_THEME, "finn")
        .putBoolean(PREF_SCREEN_TIMEOUT_DISABLED, false)
        .putBoolean(PREF_SEARCH_AUTO_LAUNCH, false)
        .putInt(PREF_DATE_FORMAT, 0)
        .putBoolean(PREF_SCREEN_FULLSCREEN, true)
        .putBoolean(PREF_DOUBLE_ACTIONS_ENABLED, false)
        .putInt(PREF_SLIDE_SENSITIVITY, 50)

    Gesture.values().forEach { editor.putString(it.id, it.pickDefaultApp(context)) }

    editor.apply()
}

fun setWindowFlags(window: Window) {
    window.setFlags(0, 0) // clear flags

    val preferences = getPreferences(window.context)
    // Display notification bar
    if (preferences.getBoolean(PREF_SCREEN_FULLSCREEN, true))
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    else window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

    // Screen Timeout
    if (preferences.getBoolean(PREF_SCREEN_TIMEOUT_DISABLED, false))
        window.setFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
    else window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
}

// Used in Tutorial and Settings `ActivityOnResult`
fun saveListActivityChoice(context: Context, data: Intent?) {
    val value = data?.getStringExtra("value")
    val forGesture = data?.getStringExtra("forGesture") ?: return

    Gesture.byId(forGesture)?.setApp(context, value.toString())

    loadSettings(context)
}

// Taken from https://stackoverflow.com/a/50743764/12787264
fun openSoftKeyboard(context: Context, view: View) {
    view.requestFocus()
    // open the soft keyboard
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
}

/* Bitmaps */

fun setButtonColor(btn: Button, color: Int) {
    if (Build.VERSION.SDK_INT >= 29)
        btn.background.colorFilter = BlendModeColorFilter(color, BlendMode.MULTIPLY)
    else {
        // tested with API 17 (Android 4.4.2 on S4 mini) -> fails
        // tested with API 28 (Android 9 on S8) -> necessary
        btn.background.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP)
    }
    // not setting it in any other case (yet), unable to find a good solution
}

fun setSwitchColor(sw: Switch, trackColor: Int) {
    if (Build.VERSION.SDK_INT >= 29) {
        sw.trackDrawable.colorFilter = BlendModeColorFilter(trackColor, BlendMode.MULTIPLY)
    }
    else {
        sw.trackDrawable.colorFilter = PorterDuffColorFilter(trackColor, PorterDuff.Mode.SRC_ATOP)
    }
}

// Taken from: https://stackoverflow.com/a/30340794/12787264
fun transformGrayscale(imageView: ImageView){
    val matrix = ColorMatrix()
    matrix.setSaturation(0f)

    val filter = ColorMatrixColorFilter(matrix)
    imageView.colorFilter = filter
}
