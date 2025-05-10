package de.jrpie.android.launcher.preferences.legacy

import android.content.Context
import de.jrpie.android.launcher.Application
import de.jrpie.android.launcher.preferences.LauncherPreferences
import de.jrpie.android.launcher.preferences.PREFERENCE_VERSION
import de.jrpie.android.launcher.widgets.ClockWidget
import de.jrpie.android.launcher.widgets.WidgetPanel
import de.jrpie.android.launcher.widgets.WidgetPosition

fun migratePreferencesFromVersion4(context: Context) {
    assert(PREFERENCE_VERSION == 100)
    assert(LauncherPreferences.internal().versionCode() < 100)

    LauncherPreferences.widgets().widgets(
        setOf(
            ClockWidget(
                (context.applicationContext as Application).appWidgetHost.allocateAppWidgetId(),
                WidgetPosition(1, 3, 10, 4),
                WidgetPanel.HOME.id
            )
        )
    )


    LauncherPreferences.internal().versionCode(100)
}