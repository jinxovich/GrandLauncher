package com.git_blame_mama.grandlauncher;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PrefsManager {

    static final String PREFS_NAME         = "GrandPrefs";
    static final String KEY_FAST_WHITELIST = "fast_whitelist_normalized";

    private static final String KEY_GRID_ITEMS        = "grid_items";
    private static final String KEY_ALLOWED_CONTACTS  = "allowed_contacts";
    private static final String KEY_WHITELIST_LEGACY  = "whitelist";
    static final String KEY_LAST_SCREEN_LOG           = "last_screen_log";

    private final SharedPreferences prefs;
    private final Gson gson;

    public PrefsManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson  = new Gson();
    }

    // ── Normalized whitelist (читается сервисом без Gson) ────────────────────

    /** Возвращает КОПИЮ нормализованного whitelist. Безопасно для изменения. */
    public Set<String> getFastWhitelist() {
        // Копируем — нельзя хранить ссылку на Set из getStringSet
        return new HashSet<>(prefs.getStringSet(KEY_FAST_WHITELIST, Collections.emptySet()));
    }

    // ── Allowed contacts ─────────────────────────────────────────────────────

    public List<AllowedContact> getAllowedContacts() {
        String json = prefs.getString(KEY_ALLOWED_CONTACTS, null);
        Type type = new TypeToken<ArrayList<AllowedContact>>() {}.getType();
        List<AllowedContact> contacts =
                (json == null) ? new ArrayList<>() : gson.fromJson(json, type);
        if (contacts == null) contacts = new ArrayList<>();

        // Миграция старого whitelist (только номера)
        Set<String> legacy = new HashSet<>(
                prefs.getStringSet(KEY_WHITELIST_LEGACY, Collections.emptySet()));
        boolean migrated = false;
        for (String number : legacy) {
            if (!containsNumber(contacts, number)) {
                contacts.add(new AllowedContact(number, number, "phone"));
                migrated = true;
            }
        }
        if (migrated) saveAllowedContacts(contacts);
        return contacts;
    }

    public void addOrUpdateAllowedContact(String name, String number, String iconKey) {
        String key = (iconKey == null || iconKey.trim().isEmpty()) ? "phone" : iconKey;
        List<AllowedContact> contacts = getAllowedContacts();
        boolean found = false;
        for (AllowedContact c : contacts) {
            if (c.number.equals(number)) {
                c.name    = name;
                c.iconKey = key;
                found     = true;
                break;
            }
        }
        if (!found) contacts.add(new AllowedContact(name, number, key));
        saveAllowedContacts(contacts);
    }

    public void removeAllowedContact(String number) {
        List<AllowedContact> contacts = getAllowedContacts();
        contacts.removeIf(c -> c.number.equals(number));
        saveAllowedContacts(contacts);
    }

    private void saveAllowedContacts(List<AllowedContact> contacts) {
        Set<String> rawNumbers        = new HashSet<>();
        Set<String> normalizedNumbers = new HashSet<>();

        for (AllowedContact c : contacts) {
            if (c.number != null && !c.number.trim().isEmpty()) {
                rawNumbers.add(c.number.trim());
                String n = normalizeNumber(c.number);
                if (!n.isEmpty()) normalizedNumbers.add(n);
            }
        }

        // commit() — синхронная запись, сервис увидит данные немедленно
        prefs.edit()
                .putString(KEY_ALLOWED_CONTACTS,    gson.toJson(contacts))
                .putStringSet(KEY_WHITELIST_LEGACY, rawNumbers)
                .putStringSet(KEY_FAST_WHITELIST,   normalizedNumbers)
                .commit();
    }

    // ── Диагностический лог скрининга ────────────────────────────────────────

    public void saveLastScreeningLog(String logLine) {
        prefs.edit().putString(KEY_LAST_SCREEN_LOG, logLine).apply();
    }

    public String getLastScreeningLog() {
        return prefs.getString(KEY_LAST_SCREEN_LOG, "");
    }

    // ── Grid items ────────────────────────────────────────────────────────────

    public List<GridItem> getGridItems() {
        String json = prefs.getString(KEY_GRID_ITEMS, null);
        if (json == null) {
            List<GridItem> def = new ArrayList<>();
            def.add(new GridItem("SOS", "112", GridItem.Type.SOS, "sos"));
            return def;
        }
        Type type = new TypeToken<ArrayList<GridItem>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public void saveGridItems(List<GridItem> items) {
        prefs.edit().putString(KEY_GRID_ITEMS, gson.toJson(items)).apply();
    }

    // ── Утилиты ───────────────────────────────────────────────────────────────

    /** @deprecated Use {@link PhoneUtils#normalize(String)} directly. */
    static String normalizeNumber(String raw) {
        return PhoneUtils.normalize(raw);
    }

    private boolean containsNumber(List<AllowedContact> contacts, String number) {
        for (AllowedContact c : contacts) {
            if (c.number.equals(number)) return true;
        }
        return false;
    }
}
