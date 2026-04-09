package com.git_blame_mama.grandlauncher;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PhoneUtilsTest {

    @Test
    public void normalize_fullFormatWithCountryCode_returns10Digits() {
        assertEquals("9991234567", PhoneUtils.normalize("+7 (999) 123-45-67"));
    }

    @Test
    public void normalize_mobileWith8Prefix_returns10Digits() {
        assertEquals("9991234567", PhoneUtils.normalize("89991234567"));
    }

    @Test
    public void normalize_mobileWith7Prefix_returns10Digits() {
        assertEquals("9991234567", PhoneUtils.normalize("79991234567"));
    }

    @Test
    public void normalize_alreadyNormalized_returnsSame() {
        assertEquals("9991234567", PhoneUtils.normalize("9991234567"));
    }

    @Test
    public void normalize_shortEmergencyNumber_returnsAsIs() {
        // Короткие номера (< 10 цифр) возвращаются без обрезки
        assertEquals("112", PhoneUtils.normalize("112"));
    }

    @Test
    public void normalize_null_returnsEmpty() {
        assertEquals("", PhoneUtils.normalize(null));
    }

    @Test
    public void normalize_emptyString_returnsEmpty() {
        assertEquals("", PhoneUtils.normalize(""));
    }

    @Test
    public void normalize_onlySymbols_returnsEmpty() {
        assertEquals("", PhoneUtils.normalize("+- ()"));
    }

    @Test
    public void normalize_withDashes_strips() {
        assertEquals("9161234567", PhoneUtils.normalize("916-123-45-67"));
    }

    @Test
    public void normalize_differentFormats_sameResult() {
        String a = PhoneUtils.normalize("+7(916)1234567");
        String b = PhoneUtils.normalize("89161234567");
        String c = PhoneUtils.normalize("79161234567");
        assertEquals(a, b);
        assertEquals(b, c);
    }
}
