package de.jrpie.android.launcher.list.other

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.REQUEST_CHOOSE_APP
import de.jrpie.android.launcher.list.forApp

/**
 * The [OtherRecyclerAdapter] will only be displayed in the ListActivity,
 * if an app / intent / etc. is picked to be launched when an action is recognized.
 *
 * It lists `other` things to be launched that are not really represented by a URI,
 * rather by Launcher- internal conventions.
 */
class OtherRecyclerAdapter(val activity: Activity):
    RecyclerView.Adapter<OtherRecyclerAdapter.ViewHolder>() {

    private val othersList: MutableList<OtherInfo> = ArrayList()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var textView: TextView = itemView.findViewById(R.id.list_other_row_name)
        var iconView: ImageView = itemView.findViewById(R.id.list_other_row_icon)


        override fun onClick(v: View) {
            val pos = adapterPosition
            val content = othersList[pos]

            returnChoiceIntent(forApp, content.data.toString())
        }

        init { itemView.setOnClickListener(this) }
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        val otherLabel = othersList[i].label.toString()
        val icon = othersList[i].icon

        viewHolder.textView.text = otherLabel
        viewHolder.iconView.setImageResource(icon)
    }

    override fun getItemCount(): Int { return othersList.size }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view: View = inflater.inflate(R.layout.list_other_row, parent, false)
        return ViewHolder(view)
    }

    init {
        othersList.add(
            OtherInfo(activity.getString(R.string.list_other_settings),
            "launcher:settings",
                R.drawable.baseline_settings_24)
        )
        othersList.add(
            OtherInfo(activity.getString(R.string.list_other_list),
                "launcher:choose",
                R.drawable.baseline_menu_24)
        )
        othersList.add(
            OtherInfo(activity.getString(R.string.list_other_volume_up),
                "launcher:volumeUp",
                R.drawable.baseline_volume_up_24)
        )
        othersList.add(
            OtherInfo(activity.getString(R.string.list_other_volume_down),
                "launcher:volumeDown",
                R.drawable.baseline_volume_down_24)
        )

        if (Build.VERSION.SDK_INT >= 19) { // requires Android KitKat +
            othersList.add(
                OtherInfo(
                    activity.getString(R.string.list_other_track_next),
                    "launcher:nextTrack",
                    R.drawable.baseline_skip_next_24
                )
            )
            othersList.add(
                OtherInfo(
                    activity.getString(R.string.list_other_track_previous),
                    "launcher:previousTrack",
                    R.drawable.baseline_skip_previous_24
                )
            )
        }
    }

    private fun returnChoiceIntent(forApp: String, value: String) {
        val returnIntent = Intent()
        returnIntent.putExtra("value", value)
        returnIntent.putExtra("forApp", forApp)
        activity.setResult(REQUEST_CHOOSE_APP, returnIntent)
        activity.finish()
    }
}