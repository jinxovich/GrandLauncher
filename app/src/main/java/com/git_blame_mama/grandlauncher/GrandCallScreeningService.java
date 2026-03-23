package com.git_blame_mama.grandlauncher;

import android.telecom.Call;
import android.telecom.CallScreeningService;
import android.net.Uri;
import java.util.Set;

public class GrandCallScreeningService extends CallScreeningService {

    @Override
    public void onScreenCall(Call.Details callDetails) {
        Uri handle = callDetails.getHandle();
        if (handle == null) return;

        String incomingNumber = handle.getSchemeSpecificPart();
        PrefsManager prefs = new PrefsManager(this);
        Set<String> whitelist = prefs.getWhitelist();

        // Проверяем, есть ли номер в белом списке
        boolean isWhitelisted = false;
        for (String savedNumber : whitelist) {
            // Упрощенная проверка (в реале нужно нормализовать номера, убирая +7/8 пробелы)
            if (incomingNumber.contains(savedNumber) || savedNumber.contains(incomingNumber)) {
                isWhitelisted = true;
                break;
            }
        }

        CallResponse.Builder responseBuilder = new CallResponse.Builder();

        if (isWhitelisted) {
            // Пропускаем звонок
            responseBuilder.setDisallowCall(false);
            responseBuilder.setRejectCall(false);
        } else {
            // БЛОКИРУЕМ ЗВОНОК
            responseBuilder.setDisallowCall(true);
            responseBuilder.setRejectCall(true);
            responseBuilder.setSkipCallLog(false); // Оставить в истории как пропущенный
            responseBuilder.setSkipNotification(true); // Не беспокоить дедушку уведомлением
        }

        respondToCall(callDetails, responseBuilder.build());
    }
}