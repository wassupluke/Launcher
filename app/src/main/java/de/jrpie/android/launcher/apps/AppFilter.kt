package de.jrpie.android.launcher.apps

import de.jrpie.android.launcher.preferences.LauncherPreferences
import java.util.*
import kotlin.text.Regex.Companion.escapeReplacement

class AppFilter(
    var search: String,
    var favoritesVisibility: AppSetVisibility = AppSetVisibility.VISIBLE,
    var hiddenVisibility: AppSetVisibility = AppSetVisibility.HIDDEN,
) {
    operator fun invoke(apps: List<DetailedAppInfo>): List<DetailedAppInfo> {
        var apps = apps

        val hidden = LauncherPreferences.apps().hidden() ?: setOf()
        val favorites = LauncherPreferences.apps().favorites() ?: setOf()
        apps = apps.filter { info ->
            favoritesVisibility.predicate(favorites, info)
                    && hiddenVisibility.predicate(hidden, info)
        }

        // normalize text for search
        var allowedSpecialCharacters = search
            .lowercase(Locale.ROOT)
            .toCharArray()
            .distinct()
            .filter { c -> !c.isLetter() }
            .map { c -> escapeReplacement(c.toString()) }
            .fold("") { x, y -> x + y }
        var disallowedCharsRegex = "[^\\p{L}$allowedSpecialCharacters]".toRegex()

        fun normalize(text: String): String {
            return text.lowercase(Locale.ROOT).replace(disallowedCharsRegex, "")
        }
        if (search.isEmpty()) {
            return apps;
        } else {
            val r: MutableList<DetailedAppInfo> = ArrayList()
            val appsSecondary: MutableList<DetailedAppInfo> = ArrayList()
            val normalizedText: String = normalize(search)
            for (item in apps) {
                val itemLabel: String = normalize(item.label.toString())

                if (itemLabel.startsWith(normalizedText)) {
                    r.add(item)
                } else if (itemLabel.contains(normalizedText)) {
                    appsSecondary.add(item)
                }
            }
            r.addAll(appsSecondary)

            return r;
        }
    }

    companion object {
        enum class AppSetVisibility(
            val predicate: (set: Set<AppInfo>, DetailedAppInfo) -> Boolean
        ) {
            VISIBLE({ _, _ -> true }),
            HIDDEN({ set, appInfo -> !set.contains(appInfo.app) }),
            EXCLUSIVE({ set, appInfo -> set.contains(appInfo.app) }),
            ;
        }
    }
}