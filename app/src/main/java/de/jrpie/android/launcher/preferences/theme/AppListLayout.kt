package de.jrpie.android.launcher.preferences.theme

import android.app.Activity
import android.content.Context
import android.util.DisplayMetrics
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

    ) {
    DEFAULT(
        { c -> LinearLayoutManager(c) },
        R.layout.list_apps_row,
        { v -> }
    ),

    // TODO work profile indicator
    TEXT(
        { c -> LinearLayoutManager(c) },
        R.layout.list_apps_row_variant_text,
        { v ->
        }
    ),
    GRID(
        { c ->
            val displayMetrics = c.resources.displayMetrics
            val width_sp = displayMetrics.widthPixels / displayMetrics.scaledDensity
            GridLayoutManager(c, (width_sp / 90).toInt()) },
        R.layout.list_apps_row_variant_grid,
        { v ->
        }
    ),
}