package de.jrpie.android.launcher.preferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import de.jrpie.android.launcher.R;
import de.jrpie.android.launcher.actions.lock.LockMethod;
import de.jrpie.android.launcher.apps.AppInfo;
import de.jrpie.android.launcher.preferences.theme.Background;
import de.jrpie.android.launcher.preferences.theme.ColorTheme;
import de.jrpie.android.launcher.preferences.theme.Font;
import eu.jonahbauer.android.preference.annotations.Preference;
import eu.jonahbauer.android.preference.annotations.PreferenceGroup;
import eu.jonahbauer.android.preference.annotations.Preferences;
import eu.jonahbauer.android.preference.annotations.serializer.PreferenceSerializationException;
import eu.jonahbauer.android.preference.annotations.serializer.PreferenceSerializer;

@Preferences(
        name = "de.jrpie.android.launcher.preferences.LauncherPreferences",
        makeFile = true,
        r = R.class,
        value = {
                @PreferenceGroup(name = "internal", prefix = "settings_internal_", suffix = "_key", value = {
                        @Preference(name = "started", type = boolean.class, defaultValue = "false"),
                        @Preference(name = "started_time", type = long.class),
                        @Preference(name = "version_code", type = int.class, defaultValue = "-1"),
                }),
                @PreferenceGroup(name = "apps", prefix = "settings_apps_", suffix = "_key", value = {
                        @Preference(name = "favorites", type = Set.class, serializer = LauncherPreferences$Config.AppInfoSetSerializer.class),
                        @Preference(name = "hidden", type = Set.class, serializer = LauncherPreferences$Config.AppInfoSetSerializer.class),
                        @Preference(name = "custom_names", type = HashMap.class, serializer = LauncherPreferences$Config.MapAppInfoStringSerializer.class),
                        @Preference(name = "hide_bound_apps", type = boolean.class, defaultValue = "false"),
                }),
                @PreferenceGroup(name = "list", prefix = "settings_list_", suffix = "_key", value = {
                        @Preference(name = "layout", type = ListLayout.class, defaultValue = "DEFAULT")
                }),
                @PreferenceGroup(name = "gestures", prefix = "settings_gesture_", suffix = "_key", value = {
                }),
                @PreferenceGroup(name = "general", prefix = "settings_general_", suffix = "_key", value = {
                        @Preference(name = "choose_home_screen", type = void.class)
                }),
                @PreferenceGroup(name = "theme", prefix = "settings_theme_", suffix = "_key", value = {
                        @Preference(name = "wallpaper", type = void.class),
                        @Preference(name = "color_theme", type = ColorTheme.class, defaultValue = "DEFAULT"),
                        @Preference(name = "background", type = Background.class, defaultValue = "DIM"),
                        @Preference(name = "font", type = Font.class, defaultValue = "HACK"),
                        @Preference(name = "text_shadow", type = boolean.class, defaultValue = "false"),
                        @Preference(name = "monochrome_icons", type = boolean.class, defaultValue = "false"),
                }),
                @PreferenceGroup(name = "clock", prefix = "settings_clock_", suffix = "_key", value = {
                        @Preference(name = "font", type = Font.class, defaultValue = "HACK"),
                        @Preference(name = "color", type = int.class, defaultValue = "0xffffffff"),
                        @Preference(name = "date_visible", type = boolean.class, defaultValue = "true"),
                        @Preference(name = "time_visible", type = boolean.class, defaultValue = "true"),
                        @Preference(name = "flip_date_time", type = boolean.class, defaultValue = "false"),
                        @Preference(name = "localized", type = boolean.class, defaultValue = "false"),
                        @Preference(name = "show_seconds", type = boolean.class, defaultValue = "true"),
                }),
                @PreferenceGroup(name = "display", prefix = "settings_display_", suffix = "_key", value = {
                        @Preference(name = "screen_timeout_disabled", type = boolean.class, defaultValue = "false"),
                        @Preference(name = "full_screen", type = boolean.class, defaultValue = "true"),
                        @Preference(name = "rotate_screen", type = boolean.class, defaultValue = "true"),
                }),
                @PreferenceGroup(name = "functionality", prefix = "settings_functionality_", suffix = "_key", value = {
                        @Preference(name = "search_auto_launch", type = boolean.class, defaultValue = "true"),
                        @Preference(name = "search_auto_open_keyboard", type = boolean.class, defaultValue = "true"),
                }),
                @PreferenceGroup(name = "enabled_gestures", prefix = "settings_enabled_gestures_", suffix = "_key", value = {
                        @Preference(name = "double_swipe", type = boolean.class, defaultValue = "true"),
                        @Preference(name = "edge_swipe", type = boolean.class, defaultValue = "true"),
                        @Preference(name = "edge_swipe_edge_width", type = int.class, defaultValue = "15"),
                }),
                @PreferenceGroup(name = "actions", prefix = "settings_actions_", suffix = "_key", value = {
                        @Preference(name = "lock_method", type = LockMethod.class, defaultValue = "DEVICE_ADMIN"),
                }),
        })
public final class LauncherPreferences$Config {
    public static class AppInfoSetSerializer implements PreferenceSerializer<Set<AppInfo>, Set<String>> {

        @Override
        public Set<String> serialize(Set<AppInfo> value) throws PreferenceSerializationException {
            if (value == null) return null;

            var serialized = new HashSet<String>(value.size());
            for (var app : value) {
                serialized.add(app.serialize());
            }

            return serialized;
        }

        @Override
        public Set<AppInfo> deserialize(Set<String> value) throws PreferenceSerializationException {
            if (value == null) return null;

            var deserialized = new HashSet<AppInfo>(value.size());

            for (var s : value) {
                deserialized.add(AppInfo.Companion.deserialize(s));
            }

            return deserialized;
        }
    }

    public static class MapAppInfoStringSerializer implements PreferenceSerializer<HashMap<AppInfo, String>, Set<String>> {

        @Override
        public Set<String> serialize(HashMap<AppInfo, String> value) throws PreferenceSerializationException {
            if (value == null) return null;

            var serialized = new HashSet<String>(value.size());

            for (var entry : value.entrySet()) {
                JSONObject obj = new JSONObject();
                try {
                    obj.put("key", entry.getKey().serialize());
                    obj.put("value", entry.getValue());
                    serialized.add(obj.toString());
                } catch (JSONException ignored) {
                }
            }

            return serialized;
        }

        @Override
        public HashMap<AppInfo, String> deserialize(Set<String> value) throws PreferenceSerializationException {
            if (value == null) return null;

            var deserialized = new HashMap<AppInfo, String>();

            for (var entry : value) {
                try {
                    JSONObject obj = new JSONObject(entry);
                    AppInfo info = AppInfo.Companion.deserialize(obj.getString("key"));
                    String s = obj.getString("value");
                    deserialized.put(info, s);
                } catch (JSONException ignored) {
                }
            }

            return deserialized;
        }
    }
}
