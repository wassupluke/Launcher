package de.jrpie.android.launcher.preferences.theme

import android.content.res.Resources
import de.jrpie.android.launcher.R

@Suppress("unused")
enum class ColorTheme(private val id: Int, private val shadowId: Int) {
    DEFAULT(R.style.colorThemeDefault, R.style.textShadow),
    DARK(R.style.colorThemeDark, R.style.textShadow),
    LIGHT(R.style.colorThemeLight, R.style.textShadowLight),
    ;

    fun applyToTheme(theme: Resources.Theme, shadow: Boolean) {
        theme.applyStyle(id, true)

        if (shadow) {
            theme.applyStyle(shadowId, true)
        }
    }
}