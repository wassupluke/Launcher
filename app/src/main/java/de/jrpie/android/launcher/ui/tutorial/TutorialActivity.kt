package de.jrpie.android.launcher.ui.tutorial

import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.window.OnBackInvokedDispatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.REQUEST_CHOOSE_APP
import de.jrpie.android.launcher.preferences.LauncherPreferences
import de.jrpie.android.launcher.saveListActivityChoice
import de.jrpie.android.launcher.ui.UIObject
import de.jrpie.android.launcher.ui.tutorial.tabs.TutorialFragmentConcept
import de.jrpie.android.launcher.ui.tutorial.tabs.TutorialFragmentFinish
import de.jrpie.android.launcher.ui.tutorial.tabs.TutorialFragmentSetup
import de.jrpie.android.launcher.ui.tutorial.tabs.TutorialFragmentStart
import de.jrpie.android.launcher.ui.tutorial.tabs.TutorialFragmentUsage

/**
 * The [TutorialActivity] is displayed automatically on new installations.
 * It can also be opened from Settings.
 *
 * It tells the user about the concept behind launcher
 * and helps with the setup process (on new installations)
 */
class TutorialActivity : AppCompatActivity(), UIObject {

    override fun onCreate(savedInstanceState: Bundle?) {
        super<AppCompatActivity>.onCreate(savedInstanceState)
        super<UIObject>.onCreate()

        // Handle back key / gesture on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_OVERLAY
            ) {
                // prevent going back when the tutorial is shown for the first time
                if (!LauncherPreferences.internal().started()) {
                    return@registerOnBackInvokedCallback
                }
                finish()
            }
        }

        // Initialise layout
        setContentView(R.layout.tutorial)

        // set up tabs and swiping in settings
        val sectionsPagerAdapter = TutorialSectionsPagerAdapter(supportFragmentManager)
        val viewPager: ViewPager = findViewById(R.id.tutorial_viewpager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tutorial_tabs)
        tabs.setupWithViewPager(viewPager)
    }

    override fun getTheme(): Resources.Theme {
        return modifyTheme(super.getTheme())
    }

    override fun onStart() {
        super<AppCompatActivity>.onStart()
        super<UIObject>.onStart()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CHOOSE_APP -> saveListActivityChoice(data)
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    // prevent going back when the tutorial is shown for the first time
    override fun onBackPressed() {
        if (LauncherPreferences.internal().started())
            super.onBackPressed()
    }

}

/**
 * The [TutorialSectionsPagerAdapter] defines which fragments are shown when,
 * in the [TutorialActivity].
 *
 * Tabs: (Start | Concept | Usage | Setup | Finish)
 */
class TutorialSectionsPagerAdapter(fm: FragmentManager) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> TutorialFragmentStart()
            1 -> TutorialFragmentConcept()
            2 -> TutorialFragmentUsage()
            3 -> TutorialFragmentSetup()
            4 -> TutorialFragmentFinish()
            else -> Fragment()
        }
    }

    /* We don't use titles here, as we have the dots */
    override fun getPageTitle(position: Int): CharSequence {
        return ""
    }

    override fun getCount(): Int {
        return 5
    }
}
