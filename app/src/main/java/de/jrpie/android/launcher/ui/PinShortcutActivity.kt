package de.jrpie.android.launcher.ui

import android.app.AlertDialog
import android.app.Service
import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.LauncherApps.PinItemRequest
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.actions.Action
import de.jrpie.android.launcher.actions.Gesture
import de.jrpie.android.launcher.actions.ShortcutAction
import de.jrpie.android.launcher.actions.shortcuts.PinnedShortcutInfo
import de.jrpie.android.launcher.databinding.ActivityPinShortcutBinding
import de.jrpie.android.launcher.preferences.LauncherPreferences

class PinShortcutActivity : AppCompatActivity(), UIObject {
    private lateinit var binding: ActivityPinShortcutBinding

    private var isBound = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super<AppCompatActivity>.onCreate(savedInstanceState)
        super<UIObject>.onCreate()
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            finish()
            return
        }

        binding = ActivityPinShortcutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val launcherApps = getSystemService(Service.LAUNCHER_APPS_SERVICE) as LauncherApps

        val request = launcherApps.getPinItemRequest(intent)
        if (request == null || request.requestType != PinItemRequest.REQUEST_TYPE_SHORTCUT) {
            finish()
            return
        }

        binding.pinShortcutLabel.text = request.shortcutInfo!!.shortLabel ?: "?"
        binding.pinShortcutLabel.setCompoundDrawables(
            launcherApps.getShortcutBadgedIconDrawable(request.shortcutInfo, 0).also {
                val size = (40 * resources.displayMetrics.density).toInt()
                it.setBounds(0,0, size, size)
            }, null, null, null)

        binding.pinShortcutButtonBind.setOnClickListener {
            AlertDialog.Builder(this, R.style.AlertDialogCustom)
                .setTitle(getString(R.string.pin_shortcut_button_bind))
                .setView(R.layout.dialog_select_gesture)
                .setNegativeButton(android.R.string.cancel, null)
                .create().also { it.show() }.let { dialog ->
                    val viewManager = LinearLayoutManager(dialog.context)
                    val viewAdapter = GestureRecyclerAdapter (dialog.context) { gesture ->
                        if (!isBound) {
                            isBound = true
                            request.accept()
                        }
                        val editor = LauncherPreferences.getSharedPreferences().edit()
                        ShortcutAction(PinnedShortcutInfo(request.shortcutInfo!!)).bindToGesture(editor, gesture.id)
                        editor.apply()
                        dialog.dismiss()
                    }
                    dialog.findViewById<RecyclerView>(R.id.dialog_select_gesture_recycler).apply {
                        setHasFixedSize(true)
                        layoutManager = viewManager
                        adapter = viewAdapter
                    }
                }
        }

        binding.pinShortcutClose.setOnClickListener { finish() }
    }

    override fun onStart() {
        super<AppCompatActivity>.onStart()
        super<UIObject>.onStart()
    }

    override fun getTheme(): Resources.Theme {
        return modifyTheme(super.getTheme())
    }

    inner class GestureRecyclerAdapter(val context: Context, val onClick: (Gesture) -> Unit): RecyclerView.Adapter<GestureRecyclerAdapter.ViewHolder>() {
        val gestures = Gesture.entries.filter { it.isEnabled() }.toList()
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val label = itemView.findViewById<TextView>(R.id.dialog_select_gesture_row_name)
            val description = itemView.findViewById<TextView>(R.id.dialog_select_gesture_row_description)
            val icon = itemView.findViewById<ImageView>(R.id.dialog_select_gesture_row_icon)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view: View = inflater.inflate(R.layout.dialog_select_gesture_row, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val gesture = gestures[position]
            holder.label.text = gesture.getLabel(context)
            holder.description.text = gesture.getDescription(context)
            holder.icon.setImageDrawable(
                Action.forGesture(gesture)?.getIcon(context)
            )
            holder.itemView.setOnClickListener {
                onClick(gesture)
            }
        }

        override fun getItemCount(): Int {
            return gestures.size
        }
    }
}