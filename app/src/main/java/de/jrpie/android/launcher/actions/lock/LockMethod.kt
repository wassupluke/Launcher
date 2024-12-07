package de.jrpie.android.launcher.actions.lock

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.preferences.LauncherPreferences


@Suppress("unused")
enum class LockMethod(
    private val lock: (Context) -> Unit,
    private val isEnabled: (Context) -> Boolean,
    private val enable: (Context) -> Unit
) {
    DEVICE_ADMIN(
        LauncherDeviceAdmin::lockScreen,
        LauncherDeviceAdmin::isDeviceAdmin,
        LauncherDeviceAdmin::lockScreen
    ),
    ACCESSIBILITY_SERVICE(
        LauncherAccessibilityService::lockScreen,
        LauncherAccessibilityService::isEnabled,
        LauncherAccessibilityService::showEnableDialog
    ),
    ;

    fun lockOrEnable(context: Context) {
        if (!this.isEnabled(context)) {
            chooseMethod(context)
            return
        }
        this.lock(context)
    }

    companion object {
        fun chooseMethod(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                // only device admin is available
                setMethod(context, DEVICE_ADMIN)
                return
            }
            val builder = AlertDialog.Builder(context, R.style.AlertDialogCustom)
            builder.setNegativeButton("cancel") { _, _ -> }
            builder.setCustomTitle(
                LayoutInflater.from(context).inflate(R.layout.dialog_select_lock_method, null)
            )

            builder.setItems(
                arrayOf(
                    context.getString(R.string.screen_lock_method_use_accessibility),
                    context.getString(R.string.screen_lock_method_use_device_admin)
                )
            ) { _, i ->
                val method = when (i) {
                    0 -> ACCESSIBILITY_SERVICE
                    1 -> DEVICE_ADMIN
                    else -> return@setItems
                }
                setMethod(context, method)
            }
            builder.show()

            return
        }

        private fun setMethod(context: Context, m: LockMethod) {
            LauncherPreferences.actions().lockMethod(m)
            if (!m.isEnabled(context))
                m.enable(context)
        }
    }
}