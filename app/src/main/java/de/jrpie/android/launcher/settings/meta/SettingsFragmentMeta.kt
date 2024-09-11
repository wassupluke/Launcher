package de.jrpie.android.launcher.settings.meta

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.UIObject
import de.jrpie.android.launcher.openNewTabWindow
import de.jrpie.android.launcher.tutorial.TutorialActivity
import de.jrpie.android.launcher.databinding.SettingsMetaBinding
import de.jrpie.android.launcher.preferences.resetPreferences

/**
 * The [SettingsFragmentMeta] is a used as a tab in the SettingsActivity.
 *
 * It is used to change settings and access resources about Launcher,
 * that are not directly related to the behaviour of the app itself.
 *
 * (greek `meta` = above, next level)
 */
class SettingsFragmentMeta : Fragment(), UIObject {

    private lateinit var binding: SettingsMetaBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SettingsMetaBinding.inflate(inflater, container, false)
        return binding.root
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
            Uri.parse(String.format("%s?id=%s", url, this.requireContext().packageName))
        )
        var flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        flags = flags or Intent.FLAG_ACTIVITY_NEW_DOCUMENT
        intent.addFlags(flags)
        return intent
    }

    override fun setOnClicks() {

        binding.settingsMetaButtonViewTutorial.setOnClickListener {
            startActivity(Intent(this.context, TutorialActivity::class.java))
        }

        // prompting for settings-reset confirmation
        binding.settingsMetaButtonResetSettings.setOnClickListener {
            AlertDialog.Builder(this.requireContext(), R.style.AlertDialogCustom)
                .setTitle(getString(R.string.settings_meta_reset))
                .setMessage(getString(R.string.settings_meta_reset_confirm))
                .setPositiveButton(android.R.string.ok
                ) { _, _ ->
                    resetPreferences(this.requireContext())
                    requireActivity().finish()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()
        }


        // report a bug
        binding.settingsMetaButtonReportBug.setOnClickListener {
            openNewTabWindow(
                getString(R.string.settings_meta_report_bug_link),
                requireContext()
            )
        }



        // contact developer
        binding.settingsMetaButtonContact.setOnClickListener {
            openNewTabWindow(
                getString(R.string.settings_meta_contact_url),
                requireContext()
            )
        }

        // contact fork developer
        binding.settingsMetaButtonForkContact.setOnClickListener {
            openNewTabWindow(
                getString(R.string.settings_meta_fork_contact_url),
                requireContext()
            )
        }

        // privacy policy
        binding.settingsMetaButtonPrivacy.setOnClickListener {
            openNewTabWindow(
                getString(R.string.settings_meta_privacy_url),
                requireContext()
            )
        }

    }
}
