package me.madhushan.reflect;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;

/**
 * Application class — called before any Activity.
 * Sets night mode to follow the device system setting so every
 * Activity (Splash → Login → Register → Home) automatically uses
 * light or dark resources from values/ vs values-night/.
 */
public class ReflectApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Follow device system dark/light setting throughout the whole app
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }
}

