package com.git_blame_mama.grandlauncher;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PrefsManager {
    private final SharedPreferences prefs;
    private final Gson gson;
    private static final String KEY_WHITELIST = "whitelist";
    private static final String KEY_GRID_ITEMS = "grid_items";
    private static final String KEY_ALLOWED_CONTACTS = "allowed_contacts";

    public PrefsManager(Context context) {
        prefs = context.getSharedPreferences("GrandPrefs", Context.MODE_PRIVATE);
        gson = new Gson();
    }

    // --- Белый список ---
    public Set<String> getWhitelist() {
        List<AllowedContact> contacts = getAllowedContacts();
        if (!contacts.isEmpty()) {
            return buildWhitelistSet(contacts);
        }
        return new HashSet<>(prefs.getStringSet(KEY_WHITELIST, new HashSet<>()));
    }

    public void addNumberToWhitelist(String number) {
        List<AllowedContact> contacts = getAllowedContacts();
        boolean exists = false;
        for (AllowedContact contact : contacts) {
            if (contact.number.equals(number)) {
                exists = true;
                break;
            }
        }
        if (!exists) {
            contacts.add(new AllowedContact(number, number, "phone"));
            saveAllowedContacts(contacts);
        }
    }

    public List<AllowedContact> getAllowedContacts() {
        String json = prefs.getString(KEY_ALLOWED_CONTACTS, null);
        Type type = new TypeToken<ArrayList<AllowedContact>>() {}.getType();
        List<AllowedContact> contacts = json == null ? new ArrayList<>() : gson.fromJson(json, type);
        if (contacts == null) {
            contacts = new ArrayList<>();
        }

        // Миграция старого формата whitelist (только номера)
        Set<String> legacyWhitelist = prefs.getStringSet(KEY_WHITELIST, new HashSet<>());
        boolean migrated = false;
        for (String number : legacyWhitelist) {
            if (!containsNumber(contacts, number)) {
                contacts.add(new AllowedContact(number, number, "phone"));
                migrated = true;
            }
        }
        if (migrated) {
            saveAllowedContacts(contacts);
        }
        return contacts;
    }

    public void addOrUpdateAllowedContact(String name, String number, String iconKey) {
        String resolvedIconKey = (iconKey == null || iconKey.trim().isEmpty()) ? "phone" : iconKey;
        List<AllowedContact> contacts = getAllowedContacts();
        boolean updated = false;
        for (AllowedContact contact : contacts) {
            if (contact.number.equals(number)) {
                contact.name = name;
                contact.iconKey = resolvedIconKey;
                updated = true;
                break;
            }
        }
        if (!updated) {
            contacts.add(new AllowedContact(name, number, resolvedIconKey));
        }
        saveAllowedContacts(contacts);
    }

    public void removeAllowedContact(String number) {
        List<AllowedContact> contacts = getAllowedContacts();
        contacts.removeIf(contact -> contact.number.equals(number));
        saveAllowedContacts(contacts);
    }

    private void saveAllowedContacts(List<AllowedContact> contacts) {
        prefs.edit()
                .putString(KEY_ALLOWED_CONTACTS, gson.toJson(contacts))
                .putStringSet(KEY_WHITELIST, buildWhitelistSet(contacts))
                .apply();
    }

    private Set<String> buildWhitelistSet(List<AllowedContact> contacts) {
        Set<String> whitelist = new HashSet<>();
        for (AllowedContact contact : contacts) {
            if (contact.number != null && !contact.number.trim().isEmpty()) {
                whitelist.add(contact.number.trim());
            }
        }
        return whitelist;
    }

    private boolean containsNumber(List<AllowedContact> contacts, String number) {
        for (AllowedContact contact : contacts) {
            if (contact.number.equals(number)) {
                return true;
            }
        }
        return false;
    }

    // --- Сетка кнопок ---
    public List<GridItem> getGridItems() {
        String json = prefs.getString(KEY_GRID_ITEMS, null);
        if (json == null) {
            // Базовый набор по умолчанию при первом запуске
            List<GridItem> defaultList = new ArrayList<>();
            defaultList.add(new GridItem("SOS", "112", GridItem.Type.SOS, "sos"));
            return defaultList;
        }
        Type type = new TypeToken<ArrayList<GridItem>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public void saveGridItems(List<GridItem> items) {
        prefs.edit().putString(KEY_GRID_ITEMS, gson.toJson(items)).apply();
    }
}