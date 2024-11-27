package de.jrpie.android.launcher.ui.list.apps

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.os.AsyncTask
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.REQUEST_CHOOSE_APP
import de.jrpie.android.launcher.actions.AppAction
import de.jrpie.android.launcher.apps.AppFilter
import de.jrpie.android.launcher.apps.AppInfo
import de.jrpie.android.launcher.apps.DetailedAppInfo
import de.jrpie.android.launcher.appsList
import de.jrpie.android.launcher.loadApps
import de.jrpie.android.launcher.openAppSettings
import de.jrpie.android.launcher.preferences.LauncherPreferences
import de.jrpie.android.launcher.transformGrayscale
import de.jrpie.android.launcher.ui.list.ListActivity
import de.jrpie.android.launcher.uninstallApp

/**
 * A [RecyclerView] (efficient scrollable list) containing all apps on the users device.
 * The apps details are represented by [AppInfo].
 *
 * @param activity - the activity this is in
 * @param intention - why the list is displayed ("view", "pick")
 * @param forGesture - the action which an app is chosen for (when the intention is "pick")
 */
@SuppressLint("NotifyDataSetChanged")
class AppsRecyclerAdapter(
    val activity: Activity,
    val root: View,
    private val intention: ListActivity.ListActivityIntention
    = ListActivity.ListActivityIntention.VIEW,
    private val forGesture: String? = "",
    private var appFilter: AppFilter = AppFilter(activity, "")
) :
    RecyclerView.Adapter<AppsRecyclerAdapter.ViewHolder>() {

    private val appsListDisplayed: MutableList<DetailedAppInfo> = mutableListOf()


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var textView: TextView = itemView.findViewById(R.id.list_apps_row_name)
        var img: ImageView = itemView.findViewById(R.id.list_apps_row_icon)

        override fun onClick(v: View) {
            var rect = Rect()
            img.getGlobalVisibleRect(rect)
            selectItem(adapterPosition, rect)
        }

        init {
            itemView.setOnClickListener(this)
        }
    }


    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        val appLabel = appsListDisplayed[i].getCustomLabel(activity).toString()
        val appIcon = appsListDisplayed[i].icon

        viewHolder.textView.text = appLabel
        viewHolder.img.setImageDrawable(appIcon)

        if (LauncherPreferences.theme().monochromeIcons()) transformGrayscale(
            viewHolder.img
        )

        // decide when to show the options popup menu about
        if (intention == ListActivity.ListActivityIntention.VIEW) {
            viewHolder.textView.setOnLongClickListener {
                showOptionsPopup(
                    viewHolder,
                    appsListDisplayed[i]
                )
            }
            viewHolder.img.setOnLongClickListener {
                showOptionsPopup(
                    viewHolder,
                    appsListDisplayed[i]
                )
            }
            // ensure onClicks are actually caught
            viewHolder.textView.setOnClickListener { viewHolder.onClick(viewHolder.textView) }
            viewHolder.img.setOnClickListener { viewHolder.onClick(viewHolder.img) }
        }
    }

    @Suppress("SameReturnValue")
    private fun showOptionsPopup(
        viewHolder: ViewHolder,
        appInfo: DetailedAppInfo
    ): Boolean {
        //create the popup menu

        val popup = PopupMenu(activity, viewHolder.img)
        popup.inflate(R.menu.menu_app)

        if (appInfo.isSystemApp) {
            popup.menu.findItem(R.id.app_menu_delete).setVisible(false)
        }

        if (LauncherPreferences.apps().hidden()?.contains(appInfo.app) == true) {
            popup.menu.findItem(R.id.app_menu_hidden).setTitle(R.string.list_app_hidden_remove)
        }

        if (LauncherPreferences.apps().favorites()?.contains(appInfo.app) == true) {
            popup.menu.findItem(R.id.app_menu_favorite).setTitle(R.string.list_app_favorite_remove)
        }


        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.app_menu_delete -> {
                    uninstallApp(appInfo.app, activity)
                    true
                }

                R.id.app_menu_info -> {
                    openAppSettings(appInfo.app, activity)
                    true
                }

                R.id.app_menu_favorite -> {
                    var favorites: MutableSet<AppInfo> =
                        LauncherPreferences.apps().favorites() ?: mutableSetOf()

                    Log.i("LAUNCHER", favorites.size.toString())
                    for (app in favorites) {
                        Log.i("LAUNCHER", app.serialize())

                    }

                    if (favorites.contains(appInfo.app)) {
                        favorites.remove(appInfo.app)
                        Log.i(
                            "LAUNCHER",
                            "Removing " + appInfo.app.serialize() + " from favorites."
                        )
                    } else {
                        Log.i("LAUNCHER", "Adding " + appInfo.app.serialize() + " to favorites.")
                        favorites.add(appInfo.app)
                    }
                    Log.i("LAUNCHER", favorites.size.toString())
                    for (app in favorites) {
                        Log.i("LAUNCHER", app.serialize())

                    }
                    LauncherPreferences.apps().favorites(favorites)

                    true
                }

                R.id.app_menu_hidden -> {
                    val hidden: MutableSet<AppInfo> =
                        LauncherPreferences.apps().hidden() ?: mutableSetOf()
                    if (hidden.contains(appInfo.app)) {
                        hidden.remove(appInfo.app)
                    } else {
                        hidden.add(appInfo.app)

                        Snackbar.make(root, R.string.snackbar_app_hidden, Snackbar.LENGTH_LONG)
                            .setAction(R.string.undo) {
                                LauncherPreferences.apps().hidden(
                                    LauncherPreferences.apps().hidden().minus(appInfo.app)
                                )
                            }.show()
                    }
                    LauncherPreferences.apps().hidden(hidden)

                    true
                }

                R.id.app_menu_rename -> {
                    val builder = AlertDialog.Builder(activity, R.style.AlertDialogCustom)

                    val title = activity.getString(R.string.dialog_rename_title, appInfo.label)
                    builder.setTitle(title)
                    builder.setView(R.layout.dialog_rename_app)

                    builder.setNegativeButton(R.string.dialog_rename_cancel) { d, _ -> d.cancel() }
                    builder.setPositiveButton(R.string.dialog_rename_ok) { d, _ ->
                        appInfo.setCustomLabel(
                            (d as? AlertDialog)
                                ?.findViewById<EditText>(R.id.dialog_rename_app_edit_text)
                                ?.text.toString()
                        )
                    }

                    val dialog = builder.create()
                    dialog.show()
                    val input = dialog.findViewById<EditText>(R.id.dialog_rename_app_edit_text)
                    input?.setText(appInfo.getCustomLabel(activity))
                    input?.hint = appInfo.label
                    true
                }

                else -> false
            }
        }

        popup.show()
        return true
    }

    override fun getItemCount(): Int {
        return appsListDisplayed.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view: View = inflater.inflate(R.layout.list_apps_row, parent, false)
        return ViewHolder(view)
    }

    init {
        // Load the apps
        if (appsList.size == 0)
            loadApps(activity.packageManager, activity)
        else {
            AsyncTask.execute { loadApps(activity.packageManager, activity) }
            notifyDataSetChanged()
        }
        updateAppsList()

    }

    fun selectItem(pos: Int, rect: Rect = Rect()) {
        if (pos >= appsListDisplayed.size) {
            return
        }
        val appInfo = appsListDisplayed[pos]
        when (intention) {
            ListActivity.ListActivityIntention.VIEW -> {
                AppAction(appInfo.app).invoke(activity, rect)
            }

            ListActivity.ListActivityIntention.PICK -> {
                val returnIntent = Intent()
                AppAction(appInfo.app).writeToIntent(returnIntent)
                returnIntent.putExtra("forGesture", forGesture)
                activity.setResult(REQUEST_CHOOSE_APP, returnIntent)
                activity.finish()
            }
        }
    }

    fun updateAppsList(triggerAutoLaunch: Boolean = false) {
        appsListDisplayed.clear()
        appsListDisplayed.addAll(appFilter(appsList))

        if (triggerAutoLaunch &&
            appsListDisplayed.size == 1
            && intention == ListActivity.ListActivityIntention.VIEW
            && LauncherPreferences.functionality().searchAutoLaunch()
        ) {
            val info = appsListDisplayed[0]
            AppAction(info.app).invoke(activity)

            val inputMethodManager =
                activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(View(activity).windowToken, 0)
        }

        notifyDataSetChanged()
    }

    /**
     * The function [setSearchString] is used to search elements within this [RecyclerView].
     */
    fun setSearchString(search: String) {
        appFilter.search = search
        updateAppsList(true)

    }

    fun setFavoritesVisibility(v: AppFilter.Companion.AppSetVisibility) {
        appFilter.favoritesVisibility = v
        updateAppsList()
    }

    fun setHiddenAppsVisibility(v: AppFilter.Companion.AppSetVisibility) {
        appFilter.hiddenVisibility = v
        updateAppsList()
    }
}
