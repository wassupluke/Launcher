package de.jrpie.android.launcher.tutorial.tabs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.jrpie.android.launcher.*
import de.jrpie.android.launcher.databinding.TutorialStartBinding

/**
 * The [TutorialFragmentStart] is a used as a tab in the TutorialActivity.
 *
 * It displays info about the app and gets the user into the tutorial
 */
class TutorialFragmentStart : Fragment(), UIObject {

    private lateinit var binding: TutorialStartBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = TutorialStartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart(){
        super<Fragment>.onStart()
        super<UIObject>.onStart()
    }

    override fun applyTheme() {
        val vibrantColor = LauncherPreferences.theme().vibrant()

        binding.tutorialStartIconRight.setTextColor(vibrantColor)
        binding.tutorialStartIconRight.blink()

    }
}