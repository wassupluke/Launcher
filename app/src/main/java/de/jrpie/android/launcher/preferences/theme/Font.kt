package de.jrpie.android.launcher.preferences.theme

import android.content.res.Resources
import de.jrpie.android.launcher.R

enum class Font(val id: Int) {
    HACK(R.style.fontHack),
    SYSTEM_DEFAULT(R.style.fontSystemDefault),
    ;

    fun applyToTheme(theme: Resources.Theme) {
        theme.applyStyle(id, true)
    }
}