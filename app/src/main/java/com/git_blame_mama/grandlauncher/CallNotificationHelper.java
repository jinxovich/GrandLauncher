package com.git_blame_mama.grandlauncher;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationManagerCompat;

/**
 * Показывает уведомление каждый раз, когда GrandCallScreeningService получает входящий звонок.
 * Если уведомление НЕ появляется при звонке — значит onScreenCall() вообще не вызывается,
 * то есть роль ROLE_CALL_SCREENING не активна.
 */
public class CallNotificationHelper {

    static final String CHANNEL_ID = "grand_call_screening";
    private static final int NOTIF_ID_BASE = 9000;

    static void createChannel(Context ctx) {
        NotificationChannel ch = new NotificationChannel(
                CHANNEL_ID,
                "Защита звонков",
                NotificationManager.IMPORTANCE_HIGH
        );
        ch.setDescription("Показывает результат обработки каждого входящего звонка");
        ch.enableVibration(false);
        ch.setSound(null, null);
        NotificationManager nm = ctx.getSystemService(NotificationManager.class);
        if (nm != null) nm.createNotificationChannel(ch);
    }

    static void notifyBlocked(Context ctx, String number) {
        post(ctx, "⛔ ЗАБЛОКИРОВАН", "Номер: " + (number.isEmpty() ? "скрытый" : number),
                NOTIF_ID_BASE + 1);
    }

    static void notifyAllowed(Context ctx, String number) {
        post(ctx, "✓ Разрешённый звонок", "Номер: " + number, NOTIF_ID_BASE + 2);
    }

    private static void post(Context ctx, String title, String text, int id) {
        Notification n = new Notification.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.sym_action_call)
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(true)
                .build();

        NotificationManagerCompat nm = NotificationManagerCompat.from(ctx);
        try {
            nm.notify(id, n);
        } catch (SecurityException ignored) {
            // POST_NOTIFICATIONS ещё не выдано — молча пропускаем
        }
    }
}
