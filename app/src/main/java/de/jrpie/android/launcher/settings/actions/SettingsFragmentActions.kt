package de.jrpie.android.launcher.settings.actions

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.UIObject
import de.jrpie.android.launcher.databinding.SettingsActionsBinding
import de.jrpie.android.launcher.list.ListActivity


/**
 *  The [SettingsFragmentActions] is a used as a tab in the SettingsActivity.
 *
 *  It is used to change Apps / Intents to be launched when a specific action
 *  is triggered.
 *  It also allows the user to view all apps ([ListActivity]) or install new ones.
 */

class
SettingsFragmentActions : Fragment(), UIObject {

    private var binding: SettingsActionsBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SettingsActionsBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onStart() {
        super<Fragment>.onStart()
        super<UIObject>.onStart()

    }

    override fun setOnClicks() {

        // App management buttons
        binding!!.settingsActionsButtonViewApps.setOnClickListener {
            val intent = Intent(this.context, ListActivity::class.java)
            intent.putExtra("intention", ListActivity.ListActivityIntention.VIEW.toString())
            startActivity(intent)
        }
        binding!!.settingsActionsButtonInstallApps.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_MAIN)
                intent.addCategory(Intent.CATEGORY_APP_MARKET)
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(
                    context,
                    getString(R.string.settings_apps_toast_store_not_found),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

}
