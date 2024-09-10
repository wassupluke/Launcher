package de.jrpie.android.launcher.tutorial.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.jrpie.android.launcher.BuildConfig.VERSION_CODE
import de.jrpie.android.launcher.UIObject
import de.jrpie.android.launcher.databinding.TutorialFinishBinding
import de.jrpie.android.launcher.preferences.LauncherPreferences
import de.jrpie.android.launcher.setDefaultHomeScreen

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
    ): View {
        binding = TutorialFinishBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super<Fragment>.onStart()
        super<UIObject>.onStart()
    }

    override fun setOnClicks() {
        super.setOnClicks()
        binding.tutorialFinishButtonStart.setOnClickListener { finishTutorial() }
    }

    private fun finishTutorial() {
        if (!LauncherPreferences.internal().started()) {
            LauncherPreferences.internal().started(true)
            LauncherPreferences.internal().startedTime(System.currentTimeMillis() / 1000L)
            LauncherPreferences.internal().versionCode(VERSION_CODE)
        }
        context?.let { setDefaultHomeScreen(it, checkDefault = true) }
        activity?.finish()
    }
}
