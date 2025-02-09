@file:Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")

package de.jrpie.android.launcher.preferences.serialization

import de.jrpie.android.launcher.apps.AppInfo
import eu.jonahbauer.android.preference.annotations.serializer.PreferenceSerializationException
import eu.jonahbauer.android.preference.annotations.serializer.PreferenceSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


// Serializers for [LauncherPreference$Config]
@Suppress("UNCHECKED_CAST")
class SetAppInfoPreferenceSerializer :
    PreferenceSerializer<java.util.Set<AppInfo>?, java.util.Set<java.lang.String>?> {
    @Throws(PreferenceSerializationException::class)
    override fun serialize(value: java.util.Set<AppInfo>?): java.util.Set<java.lang.String> {
        return value?.map(AppInfo::serialize)?.toHashSet() as java.util.Set<java.lang.String>
    }

    @Throws(PreferenceSerializationException::class)
    override fun deserialize(value: java.util.Set<java.lang.String>?): java.util.Set<AppInfo>? {
        return value?.map (java.lang.String::toString)?.map(AppInfo::deserialize)?.toHashSet() as? java.util.Set<AppInfo>
    }
}

@Suppress("UNCHECKED_CAST")
class MapAppInfoStringPreferenceSerializer :
    PreferenceSerializer<java.util.HashMap<AppInfo, String>?, java.util.Set<java.lang.String>?> {

    @Serializable
    private class MapEntry(val key: AppInfo, val value: String)

    @Throws(PreferenceSerializationException::class)
    override fun serialize(value: java.util.HashMap<AppInfo, String>?): java.util.Set<java.lang.String>? {
        return value?.map { (key, value) ->
            Json.encodeToString(MapEntry(key, value))
        }?.toHashSet() as? java.util.Set<java.lang.String>
    }

    @Throws(PreferenceSerializationException::class)
    override fun deserialize(value: java.util.Set<java.lang.String>?): java.util.HashMap<AppInfo, String>? {
        return value?.associateTo(HashMap()) {
            val entry = Json.decodeFromString<MapEntry>(it.toString())
            Pair(entry.key, entry.value)
        }
    }
}
