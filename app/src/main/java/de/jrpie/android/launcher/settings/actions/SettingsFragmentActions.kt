package de.jrpie.android.launcher.settings.actions

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import de.jrpie.android.launcher.*
import de.jrpie.android.launcher.databinding.SettingsActionsBinding
import de.jrpie.android.launcher.list.ListActivity
import de.jrpie.android.launcher.settings.intendedSettingsPause


/**
 *  The [SettingsFragmentActions] is a used as a tab in the SettingsActivity.
 *
 *  It is used to change Apps / Intents to be launched when a specific action
 *  is triggered.
 *  It also allows the user to view all apps ([ListActivity]) or install new ones.
 */

class SettingsFragmentActions : Fragment(), UIObject {

    private var binding: SettingsActionsBinding? = null

    private val sharedPreferencesListener =
        OnSharedPreferenceChangeListener { _, _ ->
            binding?.let { it.settingsActionsRviewFragment.getFragment<SettingsFragmentActionsRecycler>().actionViewAdapter?.updateActions() }
        }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        getPreferences(requireContext()).registerOnSharedPreferenceChangeListener(sharedPreferencesListener)
        binding = SettingsActionsBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onStart() {
        super<Fragment>.onStart()
        super<UIObject>.onStart()

    }

    override fun applyTheme() {
        setButtonColor(binding!!.settingsActionsButtonViewApps, vibrantColor)
        setButtonColor(binding!!.settingsActionsButtonInstallApps, vibrantColor)
    }

    override fun setOnClicks() {

        // App management buttons
        binding!!.settingsActionsButtonViewApps.setOnClickListener{
            val intent = Intent(this.context, ListActivity::class.java)
            intent.putExtra("intention", ListActivity.ListActivityIntention.VIEW.toString())
            intendedSettingsPause = true
            startActivity(intent)
        }
        binding!!.settingsActionsButtonInstallApps.setOnClickListener{
            try {
                val rateIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/"))

                intendedSettingsPause = true
                startActivity(rateIntent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this.context, getString(R.string.settings_apps_toast_store_not_found), Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        getPreferences(requireContext()).unregisterOnSharedPreferenceChangeListener(sharedPreferencesListener)
    }
}
