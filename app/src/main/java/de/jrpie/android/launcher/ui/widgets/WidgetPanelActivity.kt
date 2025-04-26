package de.jrpie.android.launcher.ui.widgets

import android.app.Activity
import android.content.res.Resources
import android.os.Bundle
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.databinding.ActivityWidgetPanelBinding
import de.jrpie.android.launcher.preferences.LauncherPreferences
import de.jrpie.android.launcher.ui.UIObject
import de.jrpie.android.launcher.ui.widgets.manage.EXTRA_PANEL_ID
import de.jrpie.android.launcher.widgets.WidgetPanel

class WidgetPanelActivity : Activity(), UIObject {
    lateinit var binding: ActivityWidgetPanelBinding
    var widgetPanelId: Int = WidgetPanel.Companion.HOME.id
    override fun onCreate(savedInstanceState: Bundle?) {
        super<Activity>.onCreate(savedInstanceState)
        super<UIObject>.onCreate()
        widgetPanelId = intent.getIntExtra(EXTRA_PANEL_ID, WidgetPanel.Companion.HOME.id)
        val binding = ActivityWidgetPanelBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.widgetPanelWidgetContainer.widgetPanelId = widgetPanelId
        binding.widgetPanelWidgetContainer.updateWidgets(
            this,
            LauncherPreferences.widgets().widgets()
        )
    }

    override fun getTheme(): Resources.Theme {
        val mTheme = modifyTheme(super.getTheme())
        mTheme.applyStyle(R.style.backgroundWallpaper, true)
        LauncherPreferences.clock().font().applyToTheme(mTheme)
        LauncherPreferences.theme().colorTheme().applyToTheme(
            mTheme,
            LauncherPreferences.theme().textShadow()
        )

        return mTheme
    }


    override fun onStart() {
        super<Activity>.onStart()
        super<UIObject>.onStart()
    }

    override fun isHomeScreen(): Boolean {
        return true
    }
}