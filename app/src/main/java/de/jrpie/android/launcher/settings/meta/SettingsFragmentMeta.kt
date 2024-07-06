package de.jrpie.android.launcher.settings.meta

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.UIObject
import de.jrpie.android.launcher.openNewTabWindow
import de.jrpie.android.launcher.resetSettings
import de.jrpie.android.launcher.setButtonColor
import de.jrpie.android.launcher.settings.intendedSettingsPause
import de.jrpie.android.launcher.tutorial.TutorialActivity
import de.jrpie.android.launcher.vibrantColor
import kotlinx.android.synthetic.main.settings_meta.*

/**
 * The [SettingsFragmentMeta] is a used as a tab in the SettingsActivity.
 *
 * It is used to change settings and access resources about Launcher,
 * that are not directly related to the behaviour of the app itself.
 *
 * (greek `meta` = above, next level)
 */
class SettingsFragmentMeta : Fragment(), UIObject {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.settings_meta, container, false)
    }

    override fun onStart() {
        super<Fragment>.onStart()
        super<UIObject>.onStart()
    }

    // Rate App
    //  Just copied code from https://stackoverflow.com/q/10816757/12787264
    //   that is how we write good software ^^

    private fun rateIntentForUrl(url: String): Intent {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(String.format("%s?id=%s", url, this.context!!.packageName))
        )
        var flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        flags = flags or Intent.FLAG_ACTIVITY_NEW_DOCUMENT
        intent.addFlags(flags)
        return intent
    }

    override fun applyTheme() {
        setButtonColor(settings_meta_button_select_launcher, vibrantColor)
        setButtonColor(settings_meta_button_view_tutorial, vibrantColor)
        setButtonColor(settings_meta_button_reset_settings, vibrantColor)
        setButtonColor(settings_meta_button_report_bug, vibrantColor)
        setButtonColor(settings_meta_button_contact, vibrantColor)
        setButtonColor(settings_meta_button_fork_contact, vibrantColor)
    }

    override fun setOnClicks() {

        settings_meta_button_select_launcher.setOnClickListener {
            intendedSettingsPause = true
            val callHomeSettingIntent = Intent(Settings.ACTION_HOME_SETTINGS)
            startActivity(callHomeSettingIntent)
        }

        settings_meta_button_view_tutorial.setOnClickListener {
            intendedSettingsPause = true
            startActivity(Intent(this.context, TutorialActivity::class.java))
        }

        // prompting for settings-reset confirmation
        settings_meta_button_reset_settings.setOnClickListener {
            AlertDialog.Builder(this.context!!, R.style.AlertDialogCustom)
                .setTitle(getString(R.string.settings_meta_reset))
                .setMessage(getString(R.string.settings_meta_reset_confirm))
                .setPositiveButton(android.R.string.ok
                ) { _, _ ->
                    resetSettings(this.context!!)
                    activity!!.finish()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()
        }


        // report a bug
        settings_meta_button_report_bug.setOnClickListener {
            intendedSettingsPause = true
            openNewTabWindow(
                getString(R.string.settings_meta_report_bug_link),
                context!!
            )
        }

        // contact developer
        settings_meta_button_contact.setOnClickListener {
            intendedSettingsPause = true
            openNewTabWindow(
                getString(R.string.settings_meta_contact_url),
                context!!
            )
        }

        // contact fork developer
        settings_meta_button_fork_contact.setOnClickListener {
            intendedSettingsPause = true
            openNewTabWindow(
                getString(R.string.settings_meta_fork_contact_url),
                context!!
            )
        }

    }
}
