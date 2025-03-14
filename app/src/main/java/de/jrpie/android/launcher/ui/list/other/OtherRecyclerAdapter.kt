package de.jrpie.android.launcher.ui.list.other

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.REQUEST_CHOOSE_APP
import de.jrpie.android.launcher.actions.LauncherAction
import de.jrpie.android.launcher.ui.list.forGesture

/**
 * The [OtherRecyclerAdapter] will only be displayed in the ListActivity,
 * if an app / intent / etc. is picked to be launched when an action is recognized.
 *
 * It lists `other` things to be launched that are not really represented by a URI,
 * rather by Launcher- internal conventions.
 */
class OtherRecyclerAdapter(val activity: Activity) :
    RecyclerView.Adapter<OtherRecyclerAdapter.ViewHolder>() {

    private val othersList: Array<LauncherAction> =
        LauncherAction.entries.filter { it.isAvailable(activity) }.toTypedArray()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var textView: TextView = itemView.findViewById(R.id.list_other_row_name)
        var iconView: ImageView = itemView.findViewById(R.id.list_other_row_icon)


        override fun onClick(v: View) {
            val pos = bindingAdapterPosition
            val content = othersList[pos]

            forGesture?.let { returnChoiceIntent(it, content) }
        }

        init {
            itemView.setOnClickListener(this)
        }
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        val otherLabel = activity.getString(othersList[i].label)
        val icon = othersList[i].icon

        viewHolder.textView.text = otherLabel
        viewHolder.iconView.setImageResource(icon)
    }

    override fun getItemCount(): Int {
        return othersList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view: View = inflater.inflate(R.layout.list_other_row, parent, false)
        return ViewHolder(view)
    }

    private fun returnChoiceIntent(forGesture: String, action: LauncherAction) {
        val returnIntent = Intent()
        returnIntent.putExtra("forGesture", forGesture)
        action.writeToIntent(returnIntent)
        activity.setResult(REQUEST_CHOOSE_APP, returnIntent)
        activity.finish()
    }
}