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

    public PrefsManager(Context context) {
        prefs = context.getSharedPreferences("GrandPrefs", Context.MODE_PRIVATE);
        gson = new Gson();
    }

    // --- Белый список ---
    public Set<String> getWhitelist() {
        return prefs.getStringSet("whitelist", new HashSet<>());
    }
    public void addNumberToWhitelist(String number) {
        Set<String> list = getWhitelist();
        list.add(number);
        prefs.edit().putStringSet("whitelist", list).apply();
    }

    // --- Сетка кнопок ---
    public List<GridItem> getGridItems() {
        String json = prefs.getString("grid_items", null);
        if (json == null) {
            // Базовый набор по умолчанию при первом запуске
            List<GridItem> defaultList = new ArrayList<>();
            defaultList.add(new GridItem("SOS", "112", GridItem.Type.SOS));
            return defaultList;
        }
        Type type = new TypeToken<ArrayList<GridItem>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public void saveGridItems(List<GridItem> items) {
        prefs.edit().putString("grid_items", gson.toJson(items)).apply();
    }
}