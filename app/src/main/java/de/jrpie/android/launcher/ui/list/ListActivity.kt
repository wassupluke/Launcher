package de.jrpie.android.launcher.ui.list

import android.app.Activity
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
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import de.jrpie.android.launcher.Application
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.REQUEST_UNINSTALL
import de.jrpie.android.launcher.actions.LauncherAction
import de.jrpie.android.launcher.apps.AppFilter
import de.jrpie.android.launcher.apps.hidePrivateSpaceWhenLocked
import de.jrpie.android.launcher.apps.isPrivateSpaceLocked
import de.jrpie.android.launcher.apps.isPrivateSpaceSetUp
import de.jrpie.android.launcher.apps.togglePrivateSpaceLock
import de.jrpie.android.launcher.databinding.ListBinding
import de.jrpie.android.launcher.preferences.LauncherPreferences
import de.jrpie.android.launcher.ui.UIObject
import de.jrpie.android.launcher.ui.list.apps.ListFragmentApps
import de.jrpie.android.launcher.ui.list.other.ListFragmentOther


/**
 * The [ListActivity] is the most general purpose activity in Launcher:
 * - used to view all apps and edit their settings
 * - used to choose an app / intent to be launched
 *
 * The activity itself can also be chosen to be launched as an action.
 */
class ListActivity : AppCompatActivity(), UIObject {
    private lateinit var binding: ListBinding
    var intention = ListActivityIntention.VIEW
    var favoritesVisibility: AppFilter.Companion.AppSetVisibility =
        AppFilter.Companion.AppSetVisibility.VISIBLE
    var privateSpaceVisibility: AppFilter.Companion.AppSetVisibility =
        AppFilter.Companion.AppSetVisibility.VISIBLE
    var hiddenVisibility: AppFilter.Companion.AppSetVisibility =
        AppFilter.Companion.AppSetVisibility.HIDDEN
    var forGesture: String? = null


    private fun updateLockIcon(locked: Boolean) {
        if (
        // only show lock for VIEW intention
            (intention != ListActivityIntention.VIEW)
            // hide lock when private space does not exist
            || !isPrivateSpaceSetUp(this)
            // hide lock when private space apps are hidden from the main list and we are not in the private space list
            || (LauncherPreferences.apps().hidePrivateSpaceApps()
                    && privateSpaceVisibility != AppFilter.Companion.AppSetVisibility.EXCLUSIVE)
            // hide lock when private space is locked and the hidden when locked setting is set
            || (locked && hidePrivateSpaceWhenLocked(this))
        ) {
            binding.listLock.visibility = View.GONE
            return
        }

        binding.listLock.visibility = View.VISIBLE

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
            titleResource =
                if (hiddenVisibility == AppFilter.Companion.AppSetVisibility.EXCLUSIVE) {
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

        val sectionsPagerAdapter = ListSectionsPagerAdapter(this)
        binding.listViewpager.apply {
            adapter = sectionsPagerAdapter
            currentItem = 0
        }
        TabLayoutMediator(binding.listTabs, binding.listViewpager) { tab, position ->
            tab.text = sectionsPagerAdapter.getPageTitle(position)
        }.attach()
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
class ListSectionsPagerAdapter(private val activity: ListActivity) :
    FragmentStateAdapter(activity) {

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ListFragmentApps()
            1 -> ListFragmentOther()
            else -> Fragment()
        }
    }

    fun getPageTitle(position: Int): CharSequence {
        return activity.resources.getString(TAB_TITLES[position])
    }

    override fun getItemCount(): Int {
        return when (activity.intention) {
            ListActivity.ListActivityIntention.VIEW -> 1
            else -> 2
        }
    }
}
