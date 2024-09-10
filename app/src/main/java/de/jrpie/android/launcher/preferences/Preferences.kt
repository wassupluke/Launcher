package de.jrpie.android.launcher.preferences

import android.app.Activity
import android.content.Context
import android.content.Intent
import de.jrpie.android.launcher.BuildConfig.VERSION_CODE
import de.jrpie.android.launcher.Gesture
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.tutorial.TutorialActivity


fun migrateToNewVersion(activity: Activity) {
    when (LauncherPreferences.internal().versionCode()) {
        // Check versions, make sure transitions between versions go well

        VERSION_CODE -> { /* the version installed and used previously are the same */ }
        21,22,23 -> {
            // TODO
        } else -> { /* The version used before was pre- v1.3.0,
                        as version tracking started then */

            /*
             * before, the dominant and vibrant color of the `finn` and `dark` theme
             * were not stored anywhere. Now they have to be stored:
             * -> we just reset them using newly implemented functions
             */
            /*when (LauncherPreferences.theme().colorTheme()) {
                "finn" -> resetToDefaultTheme(activity)
                "dark" -> resetToDarkTheme(activity)
            } */
            LauncherPreferences.internal().versionCode(VERSION_CODE)
            // show the new tutorial
            activity.startActivity(Intent(activity, TutorialActivity::class.java))
        }
    }
}

fun resetSettings(context: Context) {

    val editor = LauncherPreferences.getSharedPreferences().edit()
    Gesture.values().forEach { editor.putString(it.id, it.pickDefaultApp(context)) }
    editor.apply()

    // set default theme
    val dominantColor = context.resources.getColor(R.color.finnmglasTheme_background_color)
    val vibrantColor = context.resources.getColor(R.color.finnmglasTheme_accent_color)


    LauncherPreferences.theme().dominant(dominantColor)
    LauncherPreferences.theme().vibrant(vibrantColor)
}



