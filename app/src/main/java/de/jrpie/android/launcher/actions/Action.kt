package de.jrpie.android.launcher.actions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences.Editor
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.widget.Toast
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.preferences.LauncherPreferences
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


@Serializable
sealed interface Action {
    fun invoke(context: Context, rect: Rect? = null): Boolean
    fun label(context: Context): String
    fun getIcon(context: Context): Drawable?
    fun isAvailable(context: Context): Boolean


    fun bindToGesture(prefEditor: Editor, id: String) {
        prefEditor.putString(id, Json.encodeToString(this))
    }

    fun writeToIntent(intent: Intent) {
        intent.putExtra("action", Json.encodeToString(this))
    }

    companion object {

        fun forGesture(gesture: Gesture): Action? {
            val id = gesture.id

            val preferences = LauncherPreferences.getSharedPreferences()
            val json = preferences.getString(id, "null")!!
            return Json.decodeFromString(json)
        }

        fun resetToDefaultActions(context: Context) {
            val editor = LauncherPreferences.getSharedPreferences().edit()
            val boundActions = HashSet<String>()
            Gesture.entries.forEach { gesture ->
                context.resources
                    .getStringArray(gesture.defaultsResource)
                    .filterNot { boundActions.contains(it) }
                    .map { Pair(it, Json.decodeFromString<Action>(it)) }
                    .firstOrNull { it.second.isAvailable(context) }
                    ?.apply {
                        boundActions.add(first)
                        second.bindToGesture(editor, gesture.id)
                    }
            }
            editor.apply()
        }

        fun setActionForGesture(gesture: Gesture, action: Action?) {
            if (action == null) {
                clearActionForGesture(gesture)
                return
            }
            val editor = LauncherPreferences.getSharedPreferences().edit()
            action.bindToGesture(editor, gesture.id)
            editor.apply()
        }

        fun clearActionForGesture(gesture: Gesture) {
            LauncherPreferences.getSharedPreferences().edit()
                .remove(gesture.id)
                .apply()
        }

        fun launch(
            action: Action?,
            context: Context,
            animationIn: Int = android.R.anim.fade_in,
            animationOut: Int = android.R.anim.fade_out
        ) {
            if (action != null && action.invoke(context)) {
                if (context is Activity) {
                    context.overridePendingTransition(animationIn, animationOut)
                }
            } else {
                Toast.makeText(
                    context,
                    context.getString(R.string.toast_cant_open_message),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        fun fromIntent(data: Intent): Action? {
            val json = data.getStringExtra("action") ?: return null
            return Json.decodeFromString(json)
        }
    }
}