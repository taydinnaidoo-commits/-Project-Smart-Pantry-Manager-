package za.ac.richfield.smartpantry.data;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * The user's settings, kept in SharedPreferences (small key/value settings do
 * not need a database table).
 */
public class AppPreferences {

    private static final String FILE_NAME = "smart_pantry_settings";
    private static final String KEY_EXPIRY_ALERTS = "expiry_alerts";
    private static final String KEY_EXPIRY_DAYS = "expiry_days";
    private static final String KEY_SHOW_ALMOST_THERE = "show_almost_there";

    /** The choices offered for "warn me this many days before". */
    public static final int[] EXPIRY_DAY_OPTIONS = {1, 2, 3, 5, 7};
    private static final int DEFAULT_EXPIRY_DAYS = 3;

    private final SharedPreferences prefs;

    public AppPreferences(Context context) {
        prefs = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    /** Whether items close to expiring are highlighted on the pantry screen. */
    public boolean isExpiryAlertsEnabled() {
        return prefs.getBoolean(KEY_EXPIRY_ALERTS, true);
    }

    public void setExpiryAlertsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_EXPIRY_ALERTS, enabled).apply();
    }

    /** How many days before its expiry date an item counts as "expiring soon". */
    public int getExpiryWarningDays() {
        return prefs.getInt(KEY_EXPIRY_DAYS, DEFAULT_EXPIRY_DAYS);
    }

    public void setExpiryWarningDays(int days) {
        prefs.edit().putInt(KEY_EXPIRY_DAYS, days).apply();
    }

    /** Whether the separate "Almost there" list is shown under the suggestions. */
    public boolean isShowAlmostThere() {
        return prefs.getBoolean(KEY_SHOW_ALMOST_THERE, true);
    }

    public void setShowAlmostThere(boolean show) {
        prefs.edit().putBoolean(KEY_SHOW_ALMOST_THERE, show).apply();
    }
}
