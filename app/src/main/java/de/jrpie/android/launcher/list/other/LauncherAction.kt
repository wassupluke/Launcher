package de.jrpie.android.launcher.list.other

import android.app.Activity
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.audioNextTrack
import de.jrpie.android.launcher.audioPreviousTrack
import de.jrpie.android.launcher.audioVolumeDown
import de.jrpie.android.launcher.audioVolumeUp
import de.jrpie.android.launcher.expandNotificationsPanel
import de.jrpie.android.launcher.openAppsList
import de.jrpie.android.launcher.openSettings

enum class LauncherAction(
    val id: String,
    val label: Int,
    val icon: Int,
    val launch: (Activity) -> Unit
) {
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
    NOP("launcher:nop", R.string.list_other_nop, R.drawable.baseline_not_interested_24, {});

    companion object {
        fun byId(id: String): LauncherAction? {
            return LauncherAction.values().singleOrNull { it.id == id }
        }

        fun isOtherAction(id: String): Boolean {
            return id.startsWith("launcher")
        }
    }
}
