package de.jrpie.android.launcher.settings.launcher

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Switch
import androidx.fragment.app.Fragment
import de.jrpie.android.launcher.PREF_DATE_FORMAT
import de.jrpie.android.launcher.PREF_DOUBLE_ACTIONS_ENABLED
import de.jrpie.android.launcher.PREF_EDGE_ACTIONS_ENABLED
import de.jrpie.android.launcher.PREF_SCREEN_FULLSCREEN
import de.jrpie.android.launcher.PREF_SCREEN_TIMEOUT_DISABLED
import de.jrpie.android.launcher.PREF_SEARCH_AUTO_KEYBOARD
import de.jrpie.android.launcher.PREF_SEARCH_AUTO_LAUNCH
import de.jrpie.android.launcher.PREF_SLIDE_SENSITIVITY
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.UIObject
import de.jrpie.android.launcher.getPreferences
import de.jrpie.android.launcher.getSavedTheme
import de.jrpie.android.launcher.resetToDarkTheme
import de.jrpie.android.launcher.resetToDefaultTheme
import de.jrpie.android.launcher.setButtonColor
import de.jrpie.android.launcher.setSwitchColor
import de.jrpie.android.launcher.setWindowFlags
import de.jrpie.android.launcher.settings.intendedSettingsPause
import de.jrpie.android.launcher.vibrantColor
import de.jrpie.android.launcher.databinding.SettingsLauncherBinding
import de.jrpie.android.launcher.setDefaultHomeScreen


/**
 * The [SettingsFragmentLauncher] is a used as a tab in the SettingsActivity.
 *
 * It is used to change themes, select wallpapers ... theme related stuff
 */
class SettingsFragmentLauncher : Fragment(), UIObject {

    private lateinit var binding: SettingsLauncherBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SettingsLauncherBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart(){
        super<Fragment>.onStart()
        super<UIObject>.onStart()
    }


    override fun applyTheme() {

        setButtonColor(binding.settingsLauncherButtonChooseHomescreen, vibrantColor)
        setSwitchColor(binding.settingsLauncherSwitchScreenTimeout, vibrantColor)
        setSwitchColor(binding.settingsLauncherSwitchScreenFull, vibrantColor)
        setSwitchColor(binding.settingsLauncherSwitchAutoLaunch, vibrantColor)
        setSwitchColor(binding.settingsLauncherSwitchAutoKeyboard, vibrantColor)
        setSwitchColor(binding.settingsLauncherSwitchEnableDouble, vibrantColor)
        setSwitchColor(binding.settingsLauncherSwitchEnableEdge, vibrantColor)


        setButtonColor(binding.settingsLauncherButtonChooseWallpaper, vibrantColor)
        binding.settingsSeekbarSensitivity.progressDrawable.setColorFilter(vibrantColor, PorterDuff.Mode.SRC_IN)
    }

    override fun setOnClicks() {

        val preferences = getPreferences(requireActivity())

        fun bindSwitchToPref(switch: Switch, pref: String, default: Boolean, onChange: (Boolean) -> Unit){
            switch.isChecked = preferences.getBoolean(pref, default)
            switch.setOnCheckedChangeListener { _, isChecked -> // Toggle double actions
                preferences.edit()
                    .putBoolean(pref, isChecked)
                    .apply()
                onChange(isChecked);
            }
        }

        binding.settingsLauncherButtonChooseHomescreen.setOnClickListener {
            setDefaultHomeScreen(requireContext(), checkDefault = false)
        }

        binding.settingsLauncherButtonChooseWallpaper.setOnClickListener {
            // https://github.com/LineageOS/android_packages_apps_Trebuchet/blob/6caab89b21b2b91f0a439e1fd8c4510dcb255819/src/com/android/launcher3/views/OptionsPopupView.java#L271
            val intent = Intent(Intent.ACTION_SET_WALLPAPER)
                //.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .putExtra("com.android.wallpaper.LAUNCH_SOURCE", "app_launched_launcher")
                .putExtra("com.android.launcher3.WALLPAPER_FLAVOR", "focus_wallpaper")
            startActivity(intent)
        }



        bindSwitchToPref(binding.settingsLauncherSwitchScreenTimeout, PREF_SCREEN_TIMEOUT_DISABLED, false) {
            activity?.let{setWindowFlags(it.window)}
        }
        bindSwitchToPref(binding.settingsLauncherSwitchScreenFull, PREF_SCREEN_FULLSCREEN, true) {
            activity?.let{setWindowFlags(it.window)}
        }
        bindSwitchToPref(binding.settingsLauncherSwitchAutoLaunch, PREF_SEARCH_AUTO_LAUNCH, false) {}
        bindSwitchToPref(binding.settingsLauncherSwitchAutoKeyboard, PREF_SEARCH_AUTO_KEYBOARD, true) {}
        bindSwitchToPref(binding.settingsLauncherSwitchEnableDouble, PREF_DOUBLE_ACTIONS_ENABLED, false) {}
        bindSwitchToPref(binding.settingsLauncherSwitchEnableEdge, PREF_EDGE_ACTIONS_ENABLED, false) {}

        binding.settingsSeekbarSensitivity.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {}
                override fun onStartTrackingTouch(p0: SeekBar?) {}
                override fun onStopTrackingTouch(p0: SeekBar?) {
                    preferences.edit()
                        .putInt(PREF_SLIDE_SENSITIVITY, p0!!.progress * 100 / 4) // scale to %
                        .apply()
                }
            }
        )
    }

    override fun adjustLayout() {

        val preferences = getPreferences(requireActivity())
        // Load values into the date-format spinner
        val staticAdapter = ArrayAdapter.createFromResource(
                requireActivity(), R.array.settings_launcher_time_format_spinner_items,
                android.R.layout.simple_spinner_item )

        staticAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.settingsLauncherFormatSpinner.adapter = staticAdapter

        binding.settingsLauncherFormatSpinner.setSelection(preferences.getInt(PREF_DATE_FORMAT, 0))

        binding.settingsLauncherFormatSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                preferences.edit()
                    .putInt(PREF_DATE_FORMAT, position)
                    .apply()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) { }
        }

        // Load values into the theme spinner
        val staticThemeAdapter = ArrayAdapter.createFromResource(
            requireActivity(), R.array.settings_launcher_theme_spinner_items,
            android.R.layout.simple_spinner_item )

        staticThemeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.settingsLauncherThemeSpinner.adapter = staticThemeAdapter

        val themeInt = when (getSavedTheme(requireActivity())) {
            "finn" -> 0
            "dark" -> 1
            else -> 0
        }

        binding.settingsLauncherThemeSpinner.setSelection(themeInt)

        binding.settingsLauncherThemeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                when (position) {
                    0 -> if (getSavedTheme(activity!!) != "finn") resetToDefaultTheme(activity!!)
                    1 -> if (getSavedTheme(activity!!) != "dark") resetToDarkTheme(activity!!)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) { }
        }

        binding.settingsSeekbarSensitivity.progress = preferences.getInt(PREF_SLIDE_SENSITIVITY, 2) * 4 / 100
    }
}
