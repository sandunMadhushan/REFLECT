package me.madhushan.reflect.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME  = "reflect_session";
    private static final String KEY_USER_ID   = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    // KEY_NOTIFICATIONS       = user's in-app ON/OFF choice
    // KEY_NOTIF_DIALOG_SHOWN  = whether the FIRST-TIME system dialog has been shown
    //                           (NEVER cleared when user toggles — only on full logout)
    private static final String KEY_NOTIFICATIONS      = "notifications_enabled";
    private static final String KEY_NOTIF_DIALOG_SHOWN = "notifications_dialog_shown";
    private static final String KEY_PHOTO_URL          = "photo_url";
    private static final int    NO_USER = -1;

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /** Save a logged-in user's session. */
    public void saveSession(int userId, String fullName, String email) {
        prefs.edit()
             .putInt(KEY_USER_ID, userId)
             .putString(KEY_USER_NAME, fullName)
             .putString(KEY_USER_EMAIL, email)
             .apply();
    }

    /** Returns true if a user session exists. */
    public boolean isLoggedIn() {
        return prefs.getInt(KEY_USER_ID, NO_USER) != NO_USER;
    }

    /** Returns the stored user ID, or -1 if no session. */
    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, NO_USER);
    }

    /** Returns the stored user's full name. */
    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "");
    }

    /** Returns the stored user's email. */
    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, "");
    }

    /** Update the stored user name (after profile edit). */
    public void setUserName(String newName) {
        prefs.edit().putString(KEY_USER_NAME, newName).commit();
    }

    /** Returns the stored Google profile photo URL, or null if not a Google user. */
    public String getPhotoUrl() {
        return prefs.getString(KEY_PHOTO_URL, null);
    }

    /** Save the Google profile photo URL. */
    public void setPhotoUrl(String url) {
        prefs.edit().putString(KEY_PHOTO_URL, url).apply();
    }

    // ── Notification preferences ────────────────────────────────────────────

    /**
     * The user's explicit in-app notification toggle choice.
     * Default false = off until the user explicitly enables it.
     */
    public boolean getNotificationsEnabled() {
        return prefs.getBoolean(KEY_NOTIFICATIONS, false);
    }

    /**
     * Save the user's explicit toggle choice.
     * Uses commit() (synchronous) so the value is immediately
     * available when onResume reads it back.
     */
    public void setNotificationsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS, enabled).commit();
    }

    /**
     * Returns true if the first-time system permission dialog has already
     * been shown to this user. This flag is NEVER cleared on toggle OFF —
     * only on full logout/account delete.
     */
    public boolean hasNotifDialogBeenShown() {
        return prefs.getBoolean(KEY_NOTIF_DIALOG_SHOWN, false);
    }

    /** Mark that the first-time system permission dialog has been shown. */
    public void markNotifDialogShown() {
        prefs.edit().putBoolean(KEY_NOTIF_DIALOG_SHOWN, true).commit();
    }

    // ── Legacy aliases (keep for compatibility) ──────────────────────────────
    /** @deprecated use hasNotifDialogBeenShown() */
    public boolean hasNotificationPreferenceBeenSet() {
        return hasNotifDialogBeenShown();
    }
    /** @deprecated use markNotifDialogShown() */
    public void markNotificationPreferenceSet() {
        markNotifDialogShown();
    }
    /** Clears ONLY the ON/OFF value — does NOT clear the dialog-shown flag. */
    public void clearNotificationPreference() {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS, false).commit();
    }

    // ────────────────────────────────────────────────────────────────────────

    /** Clear the session (logout). */
    public void clearSession() {
        prefs.edit().clear().apply();
    }
}

