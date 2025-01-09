package de.jrpie.android.launcher.apps

import android.content.Context
import android.icu.text.Normalizer2
import android.os.Build
import de.jrpie.android.launcher.actions.Action
import de.jrpie.android.launcher.actions.AppAction
import de.jrpie.android.launcher.actions.Gesture
import de.jrpie.android.launcher.preferences.LauncherPreferences
import java.util.Locale
import kotlin.text.Regex.Companion.escape

class AppFilter(
    var context: Context,
    var query: String,
    var favoritesVisibility: AppSetVisibility = AppSetVisibility.VISIBLE,
    var hiddenVisibility: AppSetVisibility = AppSetVisibility.HIDDEN,
) {

    operator fun invoke(apps: List<DetailedAppInfo>): List<DetailedAppInfo> {
        var apps =
            apps.sortedBy { app -> app.getCustomLabel(context).toString().lowercase(Locale.ROOT) }

        val hidden = LauncherPreferences.apps().hidden() ?: setOf()
        val favorites = LauncherPreferences.apps().favorites() ?: setOf()

        apps = apps.filter { info ->
            favoritesVisibility.predicate(favorites, info)
                    && hiddenVisibility.predicate(hidden, info)
        }

        if (LauncherPreferences.apps().hideBoundApps()) {
            val boundApps = Gesture.entries
                .filter(Gesture::isEnabled)
                .mapNotNull { g -> (Action.forGesture(g) as? AppAction)?.app }
                .toSet()
            apps = apps.filterNot { info -> boundApps.contains(info.app) }
        }

        // normalize text for search
        val allowedSpecialCharacters = unicodeNormalize(query)
            .lowercase(Locale.ROOT)
            .toCharArray()
            .distinct()
            .filter { c -> !c.isLetter() }
            .map { c -> escape(c.toString()) }
            .fold("") { x, y -> x + y }
        val disallowedCharsRegex = "[^\\p{L}$allowedSpecialCharacters]".toRegex()

        fun normalize(text: String): String {
            return unicodeNormalize(text).replace(disallowedCharsRegex, "")
        }

        if (query.isEmpty()) {
            return apps
        } else {
            val r: MutableList<DetailedAppInfo> = ArrayList()
            val appsSecondary: MutableList<DetailedAppInfo> = ArrayList()
            val normalizedQuery: String = normalize(query)
            for (item in apps) {
                val itemLabel: String = normalize(item.getCustomLabel(context).toString())

                if (itemLabel.startsWith(normalizedQuery)) {
                    r.add(item)
                } else if (itemLabel.contains(normalizedQuery)) {
                    appsSecondary.add(item)
                }
            }
            r.addAll(appsSecondary)

            return r
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

        private fun unicodeNormalize(s: String): String {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val normalizer = Normalizer2.getNFKDInstance()
                return normalizer.normalize(s.lowercase(Locale.ROOT))
            }
            return s.lowercase(Locale.ROOT)
        }
    }
}