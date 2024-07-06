package de.jrpie.android.launcher.tutorial.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.UIObject

/**
 * The [TutorialFragmentConcept] is a used as a tab in the TutorialActivity.
 *
 * It is used to display info about Launchers concept (open source, efficiency ...)
 */
class TutorialFragmentConcept : Fragment(), UIObject {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.tutorial_concept, container, false)
    }

    override fun onStart(){
        super<Fragment>.onStart()
        super<UIObject>.onStart()
    }

}
