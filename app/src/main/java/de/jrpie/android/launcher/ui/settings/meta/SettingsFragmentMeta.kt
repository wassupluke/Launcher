package de.jrpie.android.launcher.ui.settings.meta

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.fragment.app.Fragment
import de.jrpie.android.launcher.BuildConfig
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.copyToClipboard
import de.jrpie.android.launcher.getDeviceInfo
import de.jrpie.android.launcher.openInBrowser
import de.jrpie.android.launcher.openTutorial
import de.jrpie.android.launcher.preferences.resetPreferences
import de.jrpie.android.launcher.ui.LegalInfoActivity
import de.jrpie.android.launcher.ui.UIObject

/**
 * The [SettingsFragmentMeta] is a used as a tab in the SettingsActivity.
 *
 * It is used to change settings and access resources about Launcher,
 * that are not directly related to the behaviour of the app itself.
 *
 * (greek `meta` = above, next level)
 */
class SettingsFragmentMeta : Fragment(), UIObject {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    SettingsMetaScreen(
                        context = requireContext(),
                        onResetConfirmed = { requireActivity().finish() }
                    )
                }
            }
        }
    }

    override fun onStart() {
        super<Fragment>.onStart()
        super<UIObject>.onStart()
    }

    override fun setOnClicks() {
        // No longer needed as click handlers are defined in Compose
    }
}

// Here we define what the settings meta screen looks like
@Composable
fun SettingsMetaScreen(
    context: Context,
    onResetConfirmed: () -> Unit
) {
    val openAlertDialog = remember { mutableStateOf(false) }

    // Here we tell the screen to lay out the settings buttons in a column.
    // This could also be a row, but that's hella ugly in this use case.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // View Tutorial
        SettingsButton(
            text = stringResource(R.string.settings_meta_show_tutorial),
            onClick = { openTutorial(context) }
        )

        // Reset Settings
        SettingsButton(
            text = stringResource(R.string.settings_meta_reset),
            onClick = { openAlertDialog.value = true }
        )

        SettingsButtonSpacer()

        // View Code
        SettingsButton(
            text = stringResource(R.string.settings_meta_view_code),
            onClick = { openInBrowser(context.getString(R.string.settings_meta_link_github), context) }
        )

        // Report a Bug (Placeholder for dialog, simplified for now)
        SettingsButton(
            text = stringResource(R.string.settings_meta_report_bug),
            onClick = {
                // Simplified: Directly open bug report link
                // TODO: Implement Compose dialog for bug reporting if needed
                openInBrowser(context.getString(R.string.settings_meta_report_bug_link), context)
            }
        )

        SettingsButtonSpacer()

        // Join Chat
        SettingsButton(
            text = stringResource(R.string.settings_meta_join_chat),
            onClick = { openInBrowser(context.getString(R.string.settings_meta_chat_url), context) }
        )

        // Contact Fork Developer
        SettingsButton(
            text = stringResource(R.string.settings_meta_fork_contact),
            onClick = { openInBrowser(context.getString(R.string.settings_meta_fork_contact_url), context) }
        )

        // Donate
        SettingsButton(
            text = stringResource(R.string.settings_meta_donate),
            onClick = { openInBrowser(context.getString(R.string.settings_meta_donate_url), context) }
        )

        SettingsButtonSpacer()

        // Privacy Policy
        SettingsButton(
            text = stringResource(R.string.settings_meta_privacy),
            onClick = { openInBrowser(context.getString(R.string.settings_meta_privacy_url), context) }
        )

        // Legal Info
        SettingsButton(
            text = stringResource(R.string.settings_meta_licenses),
            onClick = {
                context.startActivity(Intent(context, LegalInfoActivity::class.java))
            }
        )

        // Version
        Text(
            text = BuildConfig.VERSION_NAME,
            // TODO: Implement matching font to Launcher Appearance font
            textAlign = TextAlign.Right,
            color = colorResource(R.color.finnmglasTheme_text_color),
            modifier = Modifier
                .fillMaxWidth(1f)
                .padding(
                    top = 16.dp,
                    bottom = 8.dp,
                    end = 8.dp
                )
                .clickable {
                    val deviceInfo = getDeviceInfo()
                    copyToClipboard(context, deviceInfo)
                }
        )

        // Reset Settings Dialog
        if (openAlertDialog.value) {
            AlertDialogResetSettings(
                onDismissRequest = { openAlertDialog.value = false },
                onConfirmation = {
                    openAlertDialog.value = false
                    resetPreferences(context)
                    onResetConfirmed()
                },
                dialogTitle = stringResource(R.string.settings_meta_reset),
                dialogText = stringResource(R.string.settings_meta_reset_confirm),
                icon = Icons.Default.Warning
            )
        }
    }
}

// Here we define what a settings button looks like
@Composable
fun SettingsButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(8.dp),
        shape = RoundedCornerShape(4.dp),
        // TODO: colors can be changed to match the dynamic system theme
        // https://developer.android.com/codelabs/jetpack-compose-theming#0
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(R.color.finnmglasTheme_accent_color),
            contentColor = colorResource(R.color.finnmglasTheme_text_color)
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.align(Alignment.CenterVertically),
            color = colorResource(R.color.finnmglasTheme_text_color)
        )
    }
}

// Here we define some space to put between the buttons
@Composable
fun SettingsButtonSpacer() {
    Spacer(modifier = Modifier.height(48.dp))
}

// Here we define what an alert dialog looks like
@Composable
fun AlertDialogResetSettings(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String,
    icon: ImageVector
) {
    AlertDialog(
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = stringResource(android.R.string.dialog_alert_title),
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(text = dialogTitle)
        },
        text = {
            Text(text = dialogText)
        },
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = onConfirmation
                )
            {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text(stringResource(android.R.string.cancel))
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    )
}