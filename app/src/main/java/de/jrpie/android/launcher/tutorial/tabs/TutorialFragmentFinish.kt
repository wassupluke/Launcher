package de.jrpie.android.launcher.tutorial.tabs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.jrpie.android.launcher.*
import de.jrpie.android.launcher.BuildConfig.VERSION_NAME
import de.jrpie.android.launcher.databinding.TutorialFinishBinding

/**
 * The [TutorialFragmentFinish] is a used as a tab in the TutorialActivity.
 *
 * It is used to display further resources and let the user start Launcher
 */
class TutorialFragmentFinish : Fragment(), UIObject {

    private lateinit var binding: TutorialFinishBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = TutorialFinishBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super<Fragment>.onStart()
        super<UIObject>.onStart()
    }

    override fun applyTheme() {
        setButtonColor(binding.tutorialFinishButtonStart, vibrantColor)
        binding.tutorialFinishButtonStart.blink()
    }

    override fun setOnClicks() {
        super.setOnClicks()
        binding.tutorialFinishButtonStart.setOnClickListener{ finishTutorial() }
    }

    private fun finishTutorial() {
        context?.let { getPreferences(it) }?.let {
            if (!it.getBoolean(PREF_STARTED, false)) {
                it.edit()
                    .putBoolean(PREF_STARTED, true) // never auto run this again
                    .putLong(
                        PREF_STARTED_TIME,
                        System.currentTimeMillis() / 1000L
                    ) // record first startup timestamp
                    .putString(PREF_VERSION, VERSION_NAME) // save current launcher version
                    .apply()
            }
        }
        context?.let { setDefaultHomeScreen(it, checkDefault = true) }

        activity?.finish()
    }
}
