package de.jrpie.android.launcher.ui

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.view.Window
import android.view.WindowManager
import de.jrpie.android.launcher.preferences.LauncherPreferences

/**
 * An interface implemented by every [Activity], Fragment etc. in Launcher.
 * It handles themes and window flags - a useful abstraction as it is the same everywhere.
 */
fun setWindowFlags(window: Window, homeScreen: Boolean) {
    window.setFlags(0, 0) // clear flags
    // Display notification bar
    if (LauncherPreferences.display().fullScreen())
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    else window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

    // Screen Timeout
    if (LauncherPreferences.display().screenTimeoutDisabled())
        window.setFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
    else window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

    if (!homeScreen) {
        LauncherPreferences.theme().background().applyToWindow(window)

    }

}

interface UIObject {
    fun onCreate() {
        if (this is Activity) {
            setWindowFlags(window, isHomeScreen())

            if (!LauncherPreferences.display().rotateScreen()) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR
            }

        }
    }
    fun onStart() {
        setOnClicks()
        adjustLayout()
    }

    fun modifyTheme(theme: Resources.Theme): Resources.Theme {
        LauncherPreferences.theme().colorTheme().applyToTheme(
            theme,
            LauncherPreferences.theme().textShadow()
        )
        LauncherPreferences.theme().background().applyToTheme(theme)
        LauncherPreferences.theme().font().applyToTheme(theme)

        return theme
    }

    // fun applyTheme() { }
    fun setOnClicks() {}
    fun adjustLayout() {}

    fun isHomeScreen(): Boolean {
        return false
    }
}