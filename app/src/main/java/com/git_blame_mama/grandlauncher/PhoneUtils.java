package com.git_blame_mama.grandlauncher;

public final class PhoneUtils {

    private PhoneUtils() {}

    /**
     * Нормализует номер телефона для сравнения:
     * убирает форматирование (+, пробелы, дефисы) и оставляет последние 10 цифр.
     * Короткие номера (< 10 цифр, например "112") возвращаются как есть.
     */
    public static String normalize(String raw) {
        if (raw == null) return "";
        String digits = raw.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return "";
        return digits.length() > 10 ? digits.substring(digits.length() - 10) : digits;
    }
}
