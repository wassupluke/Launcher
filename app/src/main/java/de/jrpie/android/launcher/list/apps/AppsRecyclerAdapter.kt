package de.jrpie.android.launcher.list.apps

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.jrpie.android.launcher.PREF_SEARCH_AUTO_LAUNCH
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.REQUEST_CHOOSE_APP
import de.jrpie.android.launcher.REQUEST_UNINSTALL
import de.jrpie.android.launcher.appsList
import de.jrpie.android.launcher.getPreferences
import de.jrpie.android.launcher.getSavedTheme
import de.jrpie.android.launcher.launch
import de.jrpie.android.launcher.launchApp
import de.jrpie.android.launcher.list.ListActivity
import de.jrpie.android.launcher.list.intendedChoosePause
import de.jrpie.android.launcher.loadApps
import de.jrpie.android.launcher.openAppSettings
import de.jrpie.android.launcher.transformGrayscale
import de.jrpie.android.launcher.uninstallApp
import java.util.*

/**
 * A [RecyclerView] (efficient scrollable list) containing all apps on the users device.
 * The apps details are represented by [AppInfo].
 *
 * @param activity - the activity this is in
 * @param intention - why the list is displayed ("view", "pick")
 * @param forGesture - the action which an app is chosen for (when the intention is "pick")
 */
class AppsRecyclerAdapter(val activity: Activity,
                          val intention: ListActivity.ListActivityIntention
                            = ListActivity.ListActivityIntention.VIEW,
                          val forGesture: String? = ""):
    RecyclerView.Adapter<AppsRecyclerAdapter.ViewHolder>() {

    private val appsListDisplayed: MutableList<AppInfo>

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var textView: TextView = itemView.findViewById(R.id.list_apps_row_name)
        var img: ImageView = itemView.findViewById(R.id.list_apps_row_icon)
        var menuDots: ImageView = itemView.findViewById(R.id.list_apps_row_menu)

        override fun onClick(v: View) {
            val pos = adapterPosition
            val context: Context = v.context
            val appPackageName = appsListDisplayed[pos].packageName.toString()
            val appUser = appsListDisplayed[pos].user
            when (intention){
                ListActivity.ListActivityIntention.VIEW -> {
                    val rect = Rect()
                    img.getGlobalVisibleRect(rect)
                    launchApp(appPackageName, appUser, activity, rect)
                }
                ListActivity.ListActivityIntention.PICK -> {
                    val returnIntent = Intent()
                    returnIntent.putExtra("value", appPackageName)
                    appUser?.let{ returnIntent.putExtra("user", it) }
                    returnIntent.putExtra("forGesture", forGesture)
                    activity.setResult(REQUEST_CHOOSE_APP, returnIntent)
                    activity.finish()
                }
            }
        }

        init { itemView.setOnClickListener(this) }
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        val appLabel = appsListDisplayed[i].label.toString()
        val appPackageName = appsListDisplayed[i].packageName.toString()
        val appUser = appsListDisplayed[i].user
        val appIcon = appsListDisplayed[i].icon
        val isSystemApp = appsListDisplayed[i].isSystemApp

        viewHolder.textView.text = appLabel
        viewHolder.img.setImageDrawable(appIcon)

        if (getSavedTheme(activity) == "dark") transformGrayscale(
            viewHolder.img
        )

        // decide when to show the options popup menu about
        if (isSystemApp || intention == ListActivity.ListActivityIntention.PICK) {
            viewHolder.menuDots.visibility = View.INVISIBLE
        }
        else {
            viewHolder.menuDots.visibility = View.VISIBLE

            viewHolder.menuDots.setOnClickListener{ showOptionsPopup(viewHolder, appPackageName, appUser) }
            viewHolder.menuDots.setOnLongClickListener{ showOptionsPopup(viewHolder, appPackageName, appUser) }
            viewHolder.textView.setOnLongClickListener{ showOptionsPopup(viewHolder, appPackageName, appUser) }
            viewHolder.img.setOnLongClickListener{ showOptionsPopup(viewHolder, appPackageName, appUser) }

            // ensure onClicks are actually caught
            viewHolder.textView.setOnClickListener{ viewHolder.onClick(viewHolder.textView) }
            viewHolder.img.setOnClickListener{ viewHolder.onClick(viewHolder.img) }
        }
    }

    @Suppress("SameReturnValue")
    private fun showOptionsPopup(viewHolder: ViewHolder, appPackageName: String, user: Int?): Boolean {
        //create the popup menu

        val popup = PopupMenu(activity, viewHolder.menuDots)
        popup.inflate(R.menu.menu_app)

        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.app_menu_delete -> {
                    intendedChoosePause = true
                    uninstallApp(appPackageName, user, activity)
                    true
                }
                R.id.app_menu_info -> {
                    intendedChoosePause = true
                    openAppSettings(appPackageName, user, activity)
                    true
                }
                else -> false
            }
        }

        popup.show()
        return true
    }

    override fun getItemCount(): Int { return appsListDisplayed.size }

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

        appsListDisplayed = ArrayList()
        appsListDisplayed.addAll(appsList)
    }

    /**
     * The function [filter] is used to search elements within this [RecyclerView].
     */
    fun filter(text: String) {
        // normalize text for search
        fun normalize(text: String): String{
            return text.lowercase(Locale.ROOT).replace("[^a-z0-9]".toRegex(), "")
        }
        appsListDisplayed.clear()
        if (text.isEmpty()) {
            appsListDisplayed.addAll(appsList)
        } else {
            val appsSecondary: MutableList<AppInfo> = ArrayList()
            val normalizedText: String = normalize(text)
            for (item in appsList) {
                val itemLabel: String = normalize(item.label.toString())

                if (itemLabel.startsWith(normalizedText)) {
                    appsListDisplayed.add(item)
                }else if(itemLabel.contains(normalizedText)){
                    appsSecondary.add(item)
                }
            }
            appsListDisplayed.addAll(appsSecondary)
        }

        // Launch apps automatically if only one result is found and the user wants it
        // Disabled at the moment. The Setting 'PREF_SEARCH_AUTO_LAUNCH' may be
        // modifiable at some later point.
        if (appsListDisplayed.size == 1 && intention == ListActivity.ListActivityIntention.VIEW
            && getPreferences(activity).getBoolean(PREF_SEARCH_AUTO_LAUNCH, false)) {
            val info = appsListDisplayed[0]
            launch(info.packageName.toString(), info.user, activity)

            val inputMethodManager = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(View(activity).windowToken, 0)
        }

        notifyDataSetChanged()
    }
}
