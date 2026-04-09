package com.git_blame_mama.grandlauncher;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the call-allow/block decision logic.
 * Uses PhoneUtils.normalize() to mirror what GrandCallScreeningService does.
 */
public class CallScreeningLogicTest {

    private static boolean isAllowed(String rawIncoming, Set<String> whitelist) {
        String normalized = PhoneUtils.normalize(rawIncoming);
        return !normalized.isEmpty() && whitelist.contains(normalized);
    }

    private static Set<String> whitelist(String... numbers) {
        Set<String> set = new HashSet<>();
        for (String n : numbers) {
            String norm = PhoneUtils.normalize(n);
            if (!norm.isEmpty()) set.add(norm);
        }
        return Collections.unmodifiableSet(set);
    }

    @Test
    public void knownContact_isAllowed() {
        Set<String> wl = whitelist("89991234567");
        assertTrue(isAllowed("+7 (999) 123-45-67", wl));
    }

    @Test
    public void unknownNumber_isBlocked() {
        Set<String> wl = whitelist("89991234567");
        assertFalse(isAllowed("89990000000", wl));
    }

    @Test
    public void emptyWhitelist_blocksEverything() {
        Set<String> wl = new HashSet<>();
        assertFalse(isAllowed("89991234567", wl));
    }

    @Test
    public void hiddenNumber_isBlocked() {
        Set<String> wl = whitelist("89991234567");
        // Скрытый номер приходит как пустая строка
        assertFalse(isAllowed("", wl));
    }

    @Test
    public void sameNumberDifferentFormats_allAllowed() {
        // Вайтлист хранит нормализованный номер в формате "последние 10 цифр"
        Set<String> wl = whitelist("79161234567");
        assertTrue(isAllowed("+7(916)1234567", wl));
        assertTrue(isAllowed("89161234567", wl));
        assertTrue(isAllowed("79161234567", wl));
        assertTrue(isAllowed("9161234567", wl));
    }

    @Test
    public void emergencyNumber_allowedWhenInWhitelist() {
        Set<String> wl = whitelist("112");
        assertTrue(isAllowed("112", wl));
    }

    @Test
    public void multipleContacts_onlyMatchingIsAllowed() {
        Set<String> wl = whitelist("89991110000", "89992220000", "89993330000");
        assertTrue(isAllowed("89992220000", wl));
        assertFalse(isAllowed("89994440000", wl));
    }
}
