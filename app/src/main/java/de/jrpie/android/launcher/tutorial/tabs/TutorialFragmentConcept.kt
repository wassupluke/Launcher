package de.jrpie.android.launcher.tutorial.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.jrpie.android.launcher.BuildConfig
import de.jrpie.android.launcher.UIObject
import de.jrpie.android.launcher.databinding.TutorialConceptBinding
/**
 * The [TutorialFragmentConcept] is a used as a tab in the TutorialActivity.
 *
 * It is used to display info about Launchers concept (open source, efficiency ...)
 */
class TutorialFragmentConcept : Fragment(), UIObject {
    private lateinit var binding: TutorialConceptBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = TutorialConceptBinding.inflate(inflater, container, false)
        binding.tutorialConceptBadgeVersion.text = BuildConfig.VERSION_NAME
        return binding.root
    }

    override fun onStart(){
        super<Fragment>.onStart()
        super<UIObject>.onStart()
    }

}
