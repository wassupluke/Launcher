package de.jrpie.android.launcher.preferences.theme

import android.content.res.Resources
import de.jrpie.android.launcher.R

@Suppress("unused")
enum class ColorTheme(val id: Int) {
    DEFAULT(R.style.colorThemeDefault),
    DARK(R.style.colorThemeDark),
    ;

    fun applyToTheme(theme: Resources.Theme) {
        theme.applyStyle(id, true)
    }
}