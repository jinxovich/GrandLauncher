package com.git_blame_mama.grandlauncher;

public class GridItem {
    public enum Type { CONTACT, APP, SOS }

    public String label; // Подпись (напр. "Дочь", "Камера")
    public String data;  // Номер телефона ИЛИ package name (напр. "com.whatsapp")
    public Type type;
    public String iconKey; // Ключ иконки для контактов (для APP не используется)

    public GridItem(String label, String data, Type type) {
        this(label, data, type, null);
    }

    public GridItem(String label, String data, Type type, String iconKey) {
        this.label = label;
        this.data = data;
        this.type = type;
        this.iconKey = iconKey;
    }
}