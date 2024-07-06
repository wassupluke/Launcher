package de.jrpie.android.launcher.settings.actions

/**
 * Stores information used in [ActionsRecyclerAdapter] rows.
 *
 * Represents an action - something to be triggered by swiping, clicking etc.
 *
 * @param data - a string identifying the app / action / intent to be launched
 */
class ActionInfo(val actionText: CharSequence, val actionName: CharSequence, val data: CharSequence) {
}