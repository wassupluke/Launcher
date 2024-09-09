package de.jrpie.android.launcher.settings.actions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import de.jrpie.android.launcher.*
import de.jrpie.android.launcher.list.ListActivity
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.jrpie.android.launcher.list.other.LauncherAction
import de.jrpie.android.launcher.databinding.SettingsActionsRecyclerBinding
import java.lang.Exception

/**
 *  The [SettingsFragmentActionsRecycler] is a fragment containing the [ActionsRecyclerAdapter],
 *  which displays all selected actions / apps.
 *
 *  It is used in the Tutorial and in Settings
 */
class SettingsFragmentActionsRecycler : Fragment(), UIObject {


    private var sharedPreferencesListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            actionViewAdapter?.updateActions()
        }
    private lateinit var binding: SettingsActionsRecyclerBinding
    private var actionViewAdapter: ActionsRecyclerAdapter? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SettingsActionsRecyclerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super<Fragment>.onStart()

        // set up the list / recycler
        val actionViewManager = LinearLayoutManager(context)
        actionViewAdapter = ActionsRecyclerAdapter( requireActivity() )

        binding.settingsActionsRview.apply {
            // improve performance (since content changes don't change the layout size)
            setHasFixedSize(true)
            layoutManager = actionViewManager
            adapter = actionViewAdapter
        }
        LauncherPreferences.getSharedPreferences().registerOnSharedPreferenceChangeListener(sharedPreferencesListener)

        super<UIObject>.onStart()
    }

    override fun onDestroy() {
        LauncherPreferences.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(sharedPreferencesListener)

        super.onDestroy()
    }
}

class ActionsRecyclerAdapter(val activity: Activity):
    RecyclerView.Adapter<ActionsRecyclerAdapter.ViewHolder>() {

    private val gesturesList: ArrayList<Gesture>

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var textView: TextView = itemView.findViewById(R.id.settings_actions_row_name)
        var img: ImageView = itemView.findViewById(R.id.settings_actions_row_icon_img)
        var chooseButton: Button = itemView.findViewById(R.id.settings_actions_row_button_choose)
        var removeAction: ImageView = itemView.findViewById(R.id.settings_actions_row_remove)

        override fun onClick(v: View) { }

        init { itemView.setOnClickListener(this) }
    }

    private fun updateViewHolder(gesture: Gesture, viewHolder: ViewHolder) {
        val app = gesture.getApp(activity)
        val content = app.first
        viewHolder.img.visibility = View.VISIBLE
        viewHolder.removeAction.visibility = View.VISIBLE
        viewHolder.chooseButton.visibility = View.INVISIBLE
        if (content.isEmpty()){
            viewHolder.img.visibility = View.INVISIBLE
            viewHolder.removeAction.visibility = View.GONE
            viewHolder.chooseButton.visibility = View.VISIBLE
        }
        else if (LauncherAction.isOtherAction(content)) {
            LauncherAction.byId(content)?.let {
                viewHolder.img.setImageResource(it.icon)
            }
        } else {
            // Set image icon (by packageName)
            try {
                viewHolder.img.setImageDrawable(getAppIcon(activity, content, app.second))
            } catch (e : Exception) {
                // Probably the app was uninstalled
                // the button is shown, user asked to select an action
                viewHolder.img.visibility = View.INVISIBLE
                viewHolder.removeAction.visibility = View.GONE
                viewHolder.chooseButton.visibility = View.VISIBLE
            }
        }
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        val gesture = gesturesList[i]
        val vibrantColor = LauncherPreferences.theme().vibrant()
        viewHolder.textView.text = gesture.getLabel(activity)
        setButtonColor(viewHolder.chooseButton, vibrantColor)
        if (LauncherPreferences.theme().theme() == "dark") transformGrayscale(
            viewHolder.img
        )
        updateViewHolder(gesture, viewHolder)
        viewHolder.img.setOnClickListener{ chooseApp(gesture) }
        viewHolder.chooseButton.setOnClickListener{ chooseApp(gesture) }
        viewHolder.removeAction.setOnClickListener{ gesture.removeApp(activity) }
    }

    override fun getItemCount(): Int { return gesturesList.size }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view: View = inflater.inflate(R.layout.settings_actions_row, parent, false)
        return ViewHolder(view)
    }

    init {
        val doubleActions = LauncherPreferences.enabled_gestures().doubleSwipe()
        val edgeActions = LauncherPreferences.enabled_gestures().edgeSwipe()
        gesturesList = Gesture.values().filter {
            (doubleActions || !it.isDoubleVariant())
                    && (edgeActions || !it.isEdgeVariant())} as ArrayList<Gesture>
    }

    fun updateActions() {
        val doubleActions = LauncherPreferences.enabled_gestures().doubleSwipe()
        val edgeActions = LauncherPreferences.enabled_gestures().edgeSwipe()
        this.gesturesList.clear()
        gesturesList.addAll(Gesture.values().filter {
            (doubleActions || !it.isDoubleVariant())
                    && (edgeActions || !it.isEdgeVariant())})

        notifyDataSetChanged()
    }

    /*  */
    private fun chooseApp(gesture: Gesture) {
        val intent = Intent(activity, ListActivity::class.java)
        intent.putExtra("intention", ListActivity.ListActivityIntention.PICK.toString())
        intent.putExtra("forGesture", gesture.id) // for which action we choose the app
        activity.startActivityForResult(intent,
            REQUEST_CHOOSE_APP
        )
    }
}