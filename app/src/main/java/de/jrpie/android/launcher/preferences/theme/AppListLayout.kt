package de.jrpie.android.launcher.preferences.theme

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.ui.list.apps.AppsRecyclerAdapter


// TODO: move this to de.jrpie.android.launcher.ui.list.apps ?
@Suppress("unused")
enum class AppListLayout(
    val layoutManager: (context: Context) -> RecyclerView.LayoutManager,
    val layoutResource: Int,
    val prepareView: (viewHolder: AppsRecyclerAdapter.ViewHolder) -> Unit,
    val useBadgedText: Boolean

    ) {
    DEFAULT(
        { c -> LinearLayoutManager(c) },
        R.layout.list_apps_row,
        { v -> },
        false
    ),

    TEXT(
        { c -> LinearLayoutManager(c) },
        R.layout.list_apps_row_variant_text,
        { v -> },
        true
    ),
    GRID(
        { c ->
            val displayMetrics = c.resources.displayMetrics
            val widthSp = displayMetrics.widthPixels / displayMetrics.scaledDensity
            GridLayoutManager(c, (widthSp / 90).toInt()) },
        R.layout.list_apps_row_variant_grid,
        { v ->
        },
        false
    ),
}