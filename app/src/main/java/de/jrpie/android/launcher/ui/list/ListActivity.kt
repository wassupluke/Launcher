package de.jrpie.android.launcher.ui.list

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import de.jrpie.android.launcher.Application
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.REQUEST_UNINSTALL
import de.jrpie.android.launcher.actions.LauncherAction
import de.jrpie.android.launcher.apps.AppFilter
import de.jrpie.android.launcher.apps.isPrivateSpaceLocked
import de.jrpie.android.launcher.apps.isPrivateSpaceSetUp
import de.jrpie.android.launcher.apps.togglePrivateSpaceLock
import de.jrpie.android.launcher.databinding.ListBinding
import de.jrpie.android.launcher.preferences.LauncherPreferences
import de.jrpie.android.launcher.ui.UIObject
import de.jrpie.android.launcher.ui.list.apps.ListFragmentApps
import de.jrpie.android.launcher.ui.list.other.ListFragmentOther


// TODO: Better solution for this intercommunication functionality (used in list-fragments)
var intention = ListActivity.ListActivityIntention.VIEW
var favoritesVisibility: AppFilter.Companion.AppSetVisibility = AppFilter.Companion.AppSetVisibility.VISIBLE
var privateSpaceVisibility: AppFilter.Companion.AppSetVisibility =
    AppFilter.Companion.AppSetVisibility.VISIBLE
var hiddenVisibility: AppFilter.Companion.AppSetVisibility = AppFilter.Companion.AppSetVisibility.HIDDEN
var forGesture: String? = null

/**
 * The [ListActivity] is the most general purpose activity in Launcher:
 * - used to view all apps and edit their settings
 * - used to choose an app / intent to be launched
 *
 * The activity itself can also be chosen to be launched as an action.
 */
class ListActivity : AppCompatActivity(), UIObject {
    private lateinit var binding: ListBinding


    private fun updateLockIcon(locked: Boolean) {
        binding.listLock.setImageDrawable(
            AppCompatResources.getDrawable(
                this,
                if (locked) {
                    R.drawable.baseline_lock_24
                } else {
                    R.drawable.baseline_lock_open_24
                }
            )
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            binding.listLock.tooltipText = getString(
                if (locked) {
                    R.string.tooltip_unlock_private_space
                } else {
                    R.string.tooltip_lock_private_space
                }
            )
        }
    }



    enum class ListActivityIntention(val titleResource: Int) {
        VIEW(R.string.list_title_view), /* view list of apps */
        PICK(R.string.list_title_pick)  /* choose app or action to associate to a gesture */
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super<AppCompatActivity>.onCreate(savedInstanceState)
        super<UIObject>.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && intention == ListActivityIntention.VIEW) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_OVERLAY
            ) {
                finish()
            }
        }

        // get info about which action this activity is open for
        intent.extras?.let { bundle ->
            intention = bundle.getString("intention")
                ?.let { ListActivityIntention.valueOf(it) }
                ?: ListActivityIntention.VIEW

            favoritesVisibility = bundle.getSerializable("favoritesVisibility")
                    as? AppFilter.Companion.AppSetVisibility ?: favoritesVisibility
            privateSpaceVisibility = bundle.getSerializable("privateSpaceVisibility")
                    as? AppFilter.Companion.AppSetVisibility ?: privateSpaceVisibility
            hiddenVisibility = bundle.getSerializable("hiddenVisibility")
                    as? AppFilter.Companion.AppSetVisibility ?: hiddenVisibility

            if (intention != ListActivityIntention.VIEW)
                forGesture = bundle.getString("forGesture")
        }


        // Initialise layout
        binding = ListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.listSettings.setOnClickListener {
            LauncherAction.SETTINGS.launch(this@ListActivity)
        }

        binding.listLock.visibility =
            if (intention != ListActivityIntention.VIEW) {
                View.GONE
            } else if (!isPrivateSpaceSetUp(this)) {
                View.GONE
            } else if (LauncherPreferences.apps().hidePrivateSpaceApps()) {
                if (privateSpaceVisibility == AppFilter.Companion.AppSetVisibility.EXCLUSIVE) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            } else {
                View.VISIBLE
            }

        if (privateSpaceVisibility == AppFilter.Companion.AppSetVisibility.EXCLUSIVE) {
            isPrivateSpaceSetUp(this, showToast = true, launchSettings = true)
            if (isPrivateSpaceLocked(this)) {
                togglePrivateSpaceLock(this)
            }
        }
        updateLockIcon(isPrivateSpaceLocked(this))

        val privateSpaceLocked = (this.applicationContext as Application).privateSpaceLocked
        privateSpaceLocked.observe(this) { updateLockIcon(it) }

        // android:windowSoftInputMode="adjustResize" doesn't work in full screen.
        // workaround from https://stackoverflow.com/a/57623505
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            this.window.decorView.viewTreeObserver.addOnGlobalLayoutListener {
                val r = Rect()
                window.decorView.getWindowVisibleDisplayFrame(r)
                val height: Int =
                    binding.listContainer.context.resources.displayMetrics.heightPixels
                val diff = height - r.bottom
                if (diff != 0 &&
                    LauncherPreferences.display().hideStatusBar()
                ) {
                    if (binding.listContainer.paddingBottom != diff) {
                        binding.listContainer.setPadding(0, 0, 0, diff)
                    }
                } else {
                    if (binding.listContainer.paddingBottom != 0) {
                        binding.listContainer.setPadding(0, 0, 0, 0)
                    }
                }
            }
        }

    }

    override fun onStart() {
        super<AppCompatActivity>.onStart()
        super<UIObject>.onStart()
    }

    override fun onPause() {
        super.onPause()

        // ensure that the activity closes then an app is launched
        // and when the user navigates to recent apps
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_UNINSTALL) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, getString(R.string.list_removed), Toast.LENGTH_LONG).show()
                finish()
            } else if (resultCode == Activity.RESULT_FIRST_USER) {
                Toast.makeText(this, getString(R.string.list_not_removed), Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }


    fun updateTitle() {
        var titleResource = intention.titleResource
        if (intention == ListActivityIntention.VIEW) {
            titleResource = if (hiddenVisibility == AppFilter.Companion.AppSetVisibility.EXCLUSIVE) {
                R.string.list_title_hidden
            } else if (privateSpaceVisibility == AppFilter.Companion.AppSetVisibility.EXCLUSIVE) {
                R.string.list_title_private_space
            } else if (favoritesVisibility == AppFilter.Companion.AppSetVisibility.EXCLUSIVE) {
                R.string.list_title_favorite
            } else {
                R.string.list_title_view
            }
        }

        binding.listHeading.text = getString(titleResource)
    }


    override fun getTheme(): Resources.Theme {
        return modifyTheme(super.getTheme())
    }

    override fun setOnClicks() {
        binding.listClose.setOnClickListener { finish() }
        binding.listLock.setOnClickListener {
            togglePrivateSpaceLock(this)
            if (privateSpaceVisibility == AppFilter.Companion.AppSetVisibility.EXCLUSIVE) {
                finish()
            }
        }
    }

    override fun adjustLayout() {

        // Hide tabs for the "view" action
        if (intention == ListActivityIntention.VIEW) {
            binding.listTabs.visibility = View.GONE
        }

        updateTitle()

        val sectionsPagerAdapter = ListSectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = findViewById(R.id.list_viewpager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.list_tabs)
        tabs.setupWithViewPager(viewPager)
    }
}

private val TAB_TITLES = arrayOf(
    R.string.list_tab_app,
    R.string.list_tab_other
)

/**
 * The [ListSectionsPagerAdapter] returns the fragment,
 * which corresponds to the selected tab in [ListActivity].
 */
class ListSectionsPagerAdapter(private val context: Context, fm: FragmentManager) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> ListFragmentApps()
            1 -> ListFragmentOther()
            else -> Fragment()
        }
    }

    override fun getPageTitle(position: Int): CharSequence {
        return context.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        return when (intention) {
            ListActivity.ListActivityIntention.VIEW -> 1
            else -> 2
        }
    }
}
