package com.git_blame_mama.grandlauncher;

import android.content.SharedPreferences;
import android.net.Uri;
import android.telecom.Call;
import android.telecom.CallScreeningService;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class GrandCallScreeningService extends CallScreeningService {

    private static final String TAG = "GrandCallScreening";

    @Override
    public void onScreenCall(Call.Details callDetails) {
        // Исходящие — пропускаем без проверки
        if (callDetails.getCallDirection() != Call.Details.DIRECTION_INCOMING) {
            respondSafely(callDetails, buildAllow());
            return;
        }

        // Читаем whitelist мгновенно: только getStringSet, никакого Gson
        SharedPreferences prefs = getSharedPreferences(PrefsManager.PREFS_NAME, MODE_PRIVATE);
        Set<String> whitelist = new HashSet<>(
                prefs.getStringSet(PrefsManager.KEY_FAST_WHITELIST, Collections.emptySet()));

        String incoming = normalizeHandle(callDetails.getHandle());
        boolean allowed = !incoming.isEmpty() && whitelist.contains(incoming);

        String ts = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        String logLine;

        if (allowed) {
            logLine = ts + "  ✓ ПРОПУЩЕН  " + incoming
                    + "  (список: " + whitelist.size() + " контактов)";
            Log.i(TAG, logLine);
            CallNotificationHelper.notifyAllowed(this, incoming);
            respondSafely(callDetails, buildAllow());
        } else {
            logLine = ts + "  ✗ ЗАБЛОКИРОВАН  "
                    + (incoming.isEmpty() ? "скрытый номер" : incoming)
                    + "  (список: " + whitelist.size() + " контактов)";
            Log.i(TAG, logLine);
            CallNotificationHelper.notifyBlocked(this,
                    incoming.isEmpty() ? "скрытый номер" : incoming);
            respondSafely(callDetails, buildBlock());
        }

        prefs.edit().putString(PrefsManager.KEY_LAST_SCREEN_LOG, logLine).apply();
    }

    // ── Нормализация номера ───────────────────────────────────────────────────

    private static String normalizeHandle(Uri handle) {
        if (handle == null) return "";
        String raw = handle.getSchemeSpecificPart();
        if (raw == null || raw.isEmpty()) return "";
        return PhoneUtils.normalize(raw);
    }

    // ── Ответы ────────────────────────────────────────────────────────────────

    private CallResponse buildAllow() {
        return new CallResponse.Builder()
                .setDisallowCall(false)
                .setRejectCall(false)
                .setSkipCallLog(false)
                .setSkipNotification(false)
                .build();
    }

    private CallResponse buildBlock() {
        return new CallResponse.Builder()
                .setDisallowCall(true)
                .setRejectCall(true)
                .setSilenceCall(true)
                .setSkipCallLog(false)
                .setSkipNotification(true)
                .build();
    }

    private void respondSafely(Call.Details details, CallResponse response) {
        try {
            respondToCall(details, response);
        } catch (Exception e) {
            Log.e(TAG, "respondToCall failed", e);
        }
    }
}
