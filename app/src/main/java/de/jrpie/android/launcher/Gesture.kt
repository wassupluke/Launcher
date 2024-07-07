package de.jrpie.android.launcher

import android.app.Activity
import android.content.Context

/**
 * @param id internal id to serialize the action. Used as a key in shared preferences.
 * @param defaultsResource res id of array of default actions for the gesture.
 * @param labelResource res id of the name of the gesture.
 * @param animationIn res id of transition animation (in) when using the gesture to launch an app.
 * @param animationOut res id of transition animation (out) when using the gesture to launch an app.
 */
enum class Gesture (val id: String, private val labelResource: Int,
                    private val defaultsResource: Int,
                    private val animationIn: Int = android.R.anim.fade_in,
                    private val animationOut: Int = android.R.anim.fade_out){
    VOLUME_UP("action_volumeUpApp", R.string.settings_gesture_vol_up, R.array.default_volume_up, 0,0),
    VOLUME_DOWN("action_volumeDownApp", R.string.settings_gesture_vol_down, R.array.default_volume_down,0,0),
    TIME("action_timeApp", R.string.settings_gesture_time, R.array.default_time),
    DATE("action_dateApp", R.string.settings_gesture_date, R.array.default_date),
    LONG_CLICK("action_longClickApp", R.string.settings_gesture_long_click, R.array.default_long_click, 0,0),
    DOUBLE_CLICK("action_doubleClickApp", R.string.settings_gesture_double_click, R.array.default_double_click,0,0),
    SWIPE_UP("action_upApp", R.string.settings_gesture_up, R.array.default_up, R.anim.bottom_up),
    SWIPE_UP_DOUBLE( "action_doubleUpApp", R.string.settings_gesture_double_up, R.array.default_double_up, R.anim.bottom_up),
    SWIPE_DOWN("action_downApp", R.string.settings_gesture_down, R.array.default_down, R.anim.top_down),
    SWIPE_DOWN_DOUBLE("action_doubleDownApp", R.string.settings_gesture_double_down, R.array.default_double_down, R.anim.top_down),
    SWIPE_LEFT("action_leftApp", R.string.settings_gesture_left, R.array.default_left, R.anim.right_left),
    SWIPE_LEFT_DOUBLE("action_doubleLeftApp", R.string.settings_gesture_double_left, R.array.default_double_left, R.anim.right_left),
    SWIPE_RIGHT("action_rightApp", R.string.settings_gesture_right, R.array.default_right, R.anim.left_right),
    SWIPE_RIGHT_DOUBLE("action_doubleRightApp", R.string.settings_gesture_double_right, R.array.default_double_right, R.anim.left_right);

    fun getApp(context: Context): String {
        return getPreferences(context).getString(this.id, "")!!
    }

    fun removeApp(context: Context) {
        getPreferences(context).edit()
            .putString(this.id, "") // clear it
            .apply()
    }

    fun setApp(context: Context, app: String) {
        getPreferences(context).edit()
            .putString(this.id, app)
            .apply()
    }

    fun getLabel(context: Context): String {
        return context.resources.getString(this.labelResource)
    }

    fun pickDefaultApp(context: Context) : String {
        return context.resources
            .getStringArray(this.defaultsResource)
            .firstOrNull { isInstalled(it, context) }
            ?: ""
    }

    fun getDoubleVariant(): Gesture {
        return when(this) {
            SWIPE_UP -> SWIPE_UP_DOUBLE
            SWIPE_DOWN -> SWIPE_DOWN_DOUBLE
            SWIPE_LEFT -> SWIPE_LEFT_DOUBLE
            SWIPE_RIGHT -> SWIPE_RIGHT_DOUBLE
            else -> this
        }
    }

    fun isDoubleVariant(): Boolean {
        return when(this){
            SWIPE_UP_DOUBLE,
            SWIPE_DOWN_DOUBLE,
            SWIPE_LEFT_DOUBLE,
            SWIPE_RIGHT_DOUBLE -> true
            else -> false
        }
    }

    operator fun invoke(activity: Activity) {
        launch(this.getApp(activity), activity, this.animationIn, this.animationOut)
    }

    companion object {
        fun byId(id: String): Gesture? {
            return Gesture.values().firstOrNull {it.id == id }
        }
    }

}
