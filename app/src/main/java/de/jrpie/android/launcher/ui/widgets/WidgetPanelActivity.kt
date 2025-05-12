package de.jrpie.android.launcher.ui.widgets

import android.app.Activity
import android.content.res.Resources
import android.os.Bundle
import androidx.core.view.ViewCompat
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.databinding.ActivityWidgetPanelBinding
import de.jrpie.android.launcher.preferences.LauncherPreferences
import de.jrpie.android.launcher.ui.UIObject
import de.jrpie.android.launcher.ui.widgets.manage.EXTRA_PANEL_ID
import de.jrpie.android.launcher.widgets.WidgetPanel

class WidgetPanelActivity : Activity(), UIObject {
    lateinit var binding: ActivityWidgetPanelBinding
    private var widgetPanelId: Int = WidgetPanel.HOME.id
    override fun onCreate(savedInstanceState: Bundle?) {
        super<Activity>.onCreate(savedInstanceState)
        super<UIObject>.onCreate()
        widgetPanelId = intent.getIntExtra(EXTRA_PANEL_ID, WidgetPanel.HOME.id)
        val binding = ActivityWidgetPanelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // The widget container should extend below the status and navigation bars,
        // so let's set an empty WindowInsetsListener to prevent it from being moved.
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
            windowInsets
        }

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

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus && LauncherPreferences.display().hideNavigationBar()) {
            hideNavigationBar()
        }
    }

    override fun onStart() {
        super<Activity>.onStart()
        super<UIObject>.onStart()
    }

    override fun isHomeScreen(): Boolean {
        return true
    }
}