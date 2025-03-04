package de.jrpie.android.launcher.preferences.legacy

import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import de.jrpie.android.launcher.apps.AppInfo
import de.jrpie.android.launcher.apps.AbstractAppInfo
import de.jrpie.android.launcher.preferences.LauncherPreferences
import de.jrpie.android.launcher.preferences.PREFERENCE_VERSION
import de.jrpie.android.launcher.preferences.serialization.MapAbstractAppInfoStringPreferenceSerializer
import de.jrpie.android.launcher.preferences.serialization.SetAbstractAppInfoPreferenceSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.HashSet

/**
 * Migrate preferences from version 3 (used until version 0.0.23) to the current format
 * (see [PREFERENCE_VERSION])
 */


fun deserializeSet(value: Set<String>?): Set<AppInfo>? {
    return value?.map {
        Json.decodeFromString<AppInfo>(it)
    }?.toHashSet()
}

fun deserializeMap(value: Set<String>?): HashMap<AppInfo, String>? {
    return value?.associateTo(HashMap()) {
        val entry = Json.decodeFromString<MapEntry>(it)
        Pair(entry.key, entry.value)
    }
}

@Serializable
private class MapEntry(val key: AppInfo, val value: String)

private fun migrateSetAppInfo(key: String, preferences: SharedPreferences, editor: Editor) {
    try {
        val serializer = SetAbstractAppInfoPreferenceSerializer()
        val set = HashSet<AbstractAppInfo>()

        deserializeSet(preferences.getStringSet(key, null))?.let {
            set.addAll(it)
        }
        editor.putStringSet(
            key,
            serializer.serialize(set as java.util.Set<AbstractAppInfo>) as Set<String>?
        )
    } catch (e: Exception) {
        e.printStackTrace()
        editor.putStringSet(key, null)
    }

}
private fun migrateMapAppInfoString(key: String, preferences: SharedPreferences, editor: Editor ) {
    try {
        val serializer = MapAbstractAppInfoStringPreferenceSerializer()
        val map = HashMap<AbstractAppInfo, String>()

        deserializeMap(preferences.getStringSet(key, null))?.let {
            map.putAll(it)
        }
        editor.putStringSet(key, serializer.serialize(map) as Set<String>?)
    } catch (e: Exception) {
        e.printStackTrace()
        editor.putStringSet(key, null)
    }
}

fun migratePreferencesFromVersion3() {
    assert(PREFERENCE_VERSION == 4)
    assert(LauncherPreferences.internal().versionCode() == 3)

    val preferences = LauncherPreferences.getSharedPreferences()
    val editor = preferences.edit()
    migrateSetAppInfo(LauncherPreferences.apps().keys().favorites(), preferences, editor)
    migrateSetAppInfo(LauncherPreferences.apps().keys().hidden(), preferences, editor)
    migrateMapAppInfoString(LauncherPreferences.apps().keys().customNames(), preferences, editor)

    editor.apply()



    LauncherPreferences.internal().versionCode(4)
}