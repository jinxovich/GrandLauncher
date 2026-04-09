package com.git_blame_mama.grandlauncher;

import android.app.Application;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 29)
public class PrefsManagerTest {

    private PrefsManager prefs;

    @Before
    public void setUp() {
        Application app = RuntimeEnvironment.getApplication();
        prefs = new PrefsManager(app);
    }

    // ── saveAllowedContacts / getAllowedContacts roundtrip ──────────────────

    @Test
    public void addContact_thenGet_returnsIt() {
        prefs.addOrUpdateAllowedContact("Мама", "89991234567", "family");

        List<AllowedContact> contacts = prefs.getAllowedContacts();
        assertEquals(1, contacts.size());
        assertEquals("Мама", contacts.get(0).name);
        assertEquals("89991234567", contacts.get(0).number);
        assertEquals("family", contacts.get(0).iconKey);
    }

    @Test
    public void addContact_updatesSameBySameNumber() {
        prefs.addOrUpdateAllowedContact("Старое имя", "89991234567", "phone");
        prefs.addOrUpdateAllowedContact("Новое имя", "89991234567", "family");

        List<AllowedContact> contacts = prefs.getAllowedContacts();
        assertEquals(1, contacts.size());
        assertEquals("Новое имя", contacts.get(0).name);
        assertEquals("family", contacts.get(0).iconKey);
    }

    @Test
    public void addTwoContacts_bothPresent() {
        prefs.addOrUpdateAllowedContact("Мама", "89991110000", "family");
        prefs.addOrUpdateAllowedContact("Доктор", "89992220000", "doctor");

        assertEquals(2, prefs.getAllowedContacts().size());
    }

    @Test
    public void removeContact_removesIt() {
        prefs.addOrUpdateAllowedContact("Мама", "89991234567", "family");
        prefs.removeAllowedContact("89991234567");

        assertTrue(prefs.getAllowedContacts().isEmpty());
    }

    // ── saveAllowedContacts обновляет KEY_FAST_WHITELIST ────────────────────

    @Test
    public void addContact_fastWhitelistContainsNormalizedNumber() {
        prefs.addOrUpdateAllowedContact("Мама", "+7 (999) 123-45-67", "family");

        Set<String> wl = prefs.getFastWhitelist();
        assertTrue(wl.contains("9991234567"));
    }

    @Test
    public void addContact_fastWhitelistDoesNotContainRawFormat() {
        prefs.addOrUpdateAllowedContact("Мама", "+7 (999) 123-45-67", "family");

        Set<String> wl = prefs.getFastWhitelist();
        assertFalse(wl.contains("+7 (999) 123-45-67"));
    }

    @Test
    public void removeContact_removedFromFastWhitelist() {
        prefs.addOrUpdateAllowedContact("Мама", "89991234567", "family");
        prefs.removeAllowedContact("89991234567");

        Set<String> wl = prefs.getFastWhitelist();
        assertFalse(wl.contains("9991234567"));
    }

    @Test
    public void addMultipleContacts_allInFastWhitelist() {
        prefs.addOrUpdateAllowedContact("Мама", "89991110000", "family");
        prefs.addOrUpdateAllowedContact("Доктор", "89992220000", "doctor");

        Set<String> wl = prefs.getFastWhitelist();
        assertTrue(wl.contains("9991110000"));
        assertTrue(wl.contains("9992220000"));
    }
}
