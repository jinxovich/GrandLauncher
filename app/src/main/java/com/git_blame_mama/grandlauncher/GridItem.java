package com.git_blame_mama.grandlauncher;

public class GridItem {
    public enum Type { CONTACT, APP, SOS }

    public String label; // Подпись (напр. "Дочь", "Камера")
    public String data;  // Номер телефона ИЛИ package name (напр. "com.whatsapp")
    public Type type;

    public GridItem(String label, String data, Type type) {
        this.label = label;
        this.data = data;
        this.type = type;
    }
}