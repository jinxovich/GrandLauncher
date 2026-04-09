package com.git_blame_mama.grandlauncher;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

public final class IconResolver {

    private IconResolver() {}

    /** Resolves the icon for a grid item (contact, app, or SOS). */
    public static Drawable resolve(Context context, GridItem item) {
        if (item.type == GridItem.Type.APP) {
            try {
                return context.getPackageManager().getApplicationIcon(item.data);
            } catch (PackageManager.NameNotFoundException e) {
                return ContextCompat.getDrawable(context, android.R.drawable.sym_def_app_icon);
            }
        }
        if (item.type == GridItem.Type.SOS) {
            return ContextCompat.getDrawable(context, android.R.drawable.stat_notify_error);
        }
        return ContextCompat.getDrawable(context, forKey(item.iconKey));
    }

    /** Resolves an icon drawable resource id by iconKey string (for AllowedContact). */
    public static Drawable resolveByKey(Context context, String iconKey) {
        return ContextCompat.getDrawable(context, forKey(iconKey));
    }

    private static int forKey(String iconKey) {
        if (iconKey == null || iconKey.isEmpty()) {
            return android.R.drawable.sym_action_call;
        }
        switch (iconKey) {
            case "family":   return android.R.drawable.ic_menu_myplaces;
            case "home":     return android.R.drawable.ic_menu_mylocation;
            case "doctor":   return android.R.drawable.ic_menu_info_details;
            case "favorite": return android.R.drawable.btn_star_big_on;
            case "phone":
            default:         return android.R.drawable.sym_action_call;
        }
    }
}
