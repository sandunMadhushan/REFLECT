package me.madhushan.reflect.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME  = "reflect_session";
    private static final String KEY_USER_ID   = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_NOTIFICATIONS = "notifications_enabled";
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

    /** Returns true if the user has granted notification permission. */
    public boolean getNotificationsEnabled() {
        return prefs.getBoolean(KEY_NOTIFICATIONS, false);
    }

    /** Save whether notifications are enabled. */
    public void setNotificationsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS, enabled).apply();
    }

    /** Clear the session (logout). */
    public void clearSession() {
        prefs.edit().clear().apply();
    }
}

