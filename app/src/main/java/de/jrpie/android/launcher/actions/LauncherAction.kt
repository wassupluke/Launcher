package de.jrpie.android.launcher.actions

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences.Editor
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.os.SystemClock
import android.view.KeyEvent
import android.widget.Toast
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.apps.AppFilter
import de.jrpie.android.launcher.apps.AppInfo.Companion.INVALID_USER
import de.jrpie.android.launcher.ui.list.ListActivity
import de.jrpie.android.launcher.ui.settings.SettingsActivity

enum class LauncherAction(
    val id: String,
    val label: Int,
    val icon: Int,
    val launch: (Context) -> Unit
) : Action {
    SETTINGS(
        "launcher:settings",
        R.string.list_other_settings,
        R.drawable.baseline_settings_24,
        ::openSettings
    ),
    CHOOSE(
        "launcher:choose",
        R.string.list_other_list,
        R.drawable.baseline_menu_24,
        ::openAppsList
    ),
    CHOOSE_FROM_FAVORITES(
        "launcher:chooseFromFavorites",
        R.string.list_other_list_favorites,
        R.drawable.baseline_favorite_24,
        { context -> openAppsList(context, true)}
    ),
    VOLUME_UP(
        "launcher:volumeUp",
        R.string.list_other_volume_up,
        R.drawable.baseline_volume_up_24, ::audioVolumeUp
    ),
    VOLUME_DOWN(
        "launcher:volumeDown",
        R.string.list_other_volume_down,
        R.drawable.baseline_volume_down_24, ::audioVolumeDown
    ),
    TRACK_NEXT(
        "launcher:nextTrack",
        R.string.list_other_track_next,
        R.drawable.baseline_skip_next_24, ::audioNextTrack
    ),
    TRACK_PREV(
        "launcher:previousTrack",
        R.string.list_other_track_previous,
        R.drawable.baseline_skip_previous_24, ::audioPreviousTrack
    ),
    EXPAND_NOTIFICATIONS_PANEL(
        "launcher:expandNotificationsPanel",
        R.string.list_other_expand_notifications_panel,
        R.drawable.baseline_notifications_24,
        ::expandNotificationsPanel
    ),
    EXPAND_SETTINGS_PANEL(
        "launcher:expandSettingsPanel",
        R.string.list_other_expand_settings_panel,
        R.drawable.baseline_settings_applications_24,
        ::expandSettingsPanel
    ),
    LOCK_SCREEN(
        "launcher:lockScreen",
        R.string.list_other_lock_screen,
        R.drawable.baseline_lock_24px,
        LauncherDeviceAdmin::lockScreen
    ),
    NOP("launcher:nop", R.string.list_other_nop, R.drawable.baseline_not_interested_24, {});

    override fun invoke(context: Context, rect: Rect?): Boolean {
        launch(context)
        return true
    }

    override fun label(context: Context): String {
        return context.getString(label)
    }

    override fun getIcon(context: Context): Drawable? {
        return context.getDrawable(icon)
    }

    override fun bindToGesture(editor: Editor, id: String) {
        editor
            .putString("$id.app", this.id)
            .putInt("$id.user", INVALID_USER)
    }

    override fun writeToIntent(intent: Intent) {
        intent.putExtra("action_id", id)
    }

    override fun isAvailable(context: Context): Boolean {
        return true
    }

    companion object {
        fun byId(id: String): LauncherAction? {
            return entries.singleOrNull { it.id == id }
        }

        fun isOtherAction(id: String): Boolean {
            return id.startsWith("launcher")
        }
    }
}


/* Media player actions */

private fun audioNextTrack(context: Context) {

    val mAudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    val eventTime: Long = SystemClock.uptimeMillis()

    val downEvent =
        KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT, 0)
    mAudioManager.dispatchMediaKeyEvent(downEvent)

    val upEvent = KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_NEXT, 0)
    mAudioManager.dispatchMediaKeyEvent(upEvent)
}

private fun audioPreviousTrack(context: Context) {
    val mAudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    val eventTime: Long = SystemClock.uptimeMillis()

    val downEvent =
        KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS, 0)
    mAudioManager.dispatchMediaKeyEvent(downEvent)

    val upEvent =
        KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PREVIOUS, 0)
    mAudioManager.dispatchMediaKeyEvent(upEvent)
}

private fun audioVolumeUp(context: Context) {
    val audioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    audioManager.adjustStreamVolume(
        AudioManager.STREAM_MUSIC,
        AudioManager.ADJUST_RAISE,
        AudioManager.FLAG_SHOW_UI
    )
}

private fun audioVolumeDown(context: Context) {
    val audioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    audioManager.adjustStreamVolume(
        AudioManager.STREAM_MUSIC,
        AudioManager.ADJUST_LOWER,
        AudioManager.FLAG_SHOW_UI
    )
}
/* End media player actions */

private fun expandNotificationsPanel(context: Context) {
    /* https://stackoverflow.com/a/15582509 */
    try {
        @Suppress("SpellCheckingInspection")
        val statusBarService: Any? = context.getSystemService("statusbar")
        val statusBarManager = Class.forName("android.app.StatusBarManager")
        val showStatusBar = statusBarManager.getMethod("expandNotificationsPanel")
        showStatusBar.invoke(statusBarService)
    } catch (e: Exception) {
        Toast.makeText(
            context,
            context.getString(R.string.alert_cant_expand_status_bar_panel),
            Toast.LENGTH_LONG
        ).show()
    }
}

private fun expandSettingsPanel(context: Context) {
    /* https://stackoverflow.com/a/31898506 */
    try {
        @Suppress("SpellCheckingInspection")
        val statusBarService: Any? = context.getSystemService("statusbar")
        val statusBarManager = Class.forName("android.app.StatusBarManager")
        val showStatusBar = statusBarManager.getMethod("expandSettingsPanel")
        showStatusBar.invoke(statusBarService)
    } catch (e: Exception) {
        Toast.makeText(
            context,
            context.getString(R.string.alert_cant_expand_status_bar_panel),
            Toast.LENGTH_LONG
        ).show()
    }
}

private fun openSettings(context: Context) {
    context.startActivity(Intent(context, SettingsActivity::class.java))
}

fun openAppsList(context: Context, favorite: Boolean = false, hidden: Boolean = false) {
    val intent = Intent(context, ListActivity::class.java)
    intent.putExtra("intention", ListActivity.ListActivityIntention.VIEW.toString())
    intent.putExtra(
        "favoritesVisibility",
        if (favorite) {
            AppFilter.Companion.AppSetVisibility.EXCLUSIVE
        } else {
            AppFilter.Companion.AppSetVisibility.VISIBLE
        }
    )
    intent.putExtra(
        "hiddenVisibility",
        if (hidden) {
            AppFilter.Companion.AppSetVisibility.EXCLUSIVE
        } else {
            AppFilter.Companion.AppSetVisibility.HIDDEN
        }
    )

    context.startActivity(intent)
}





