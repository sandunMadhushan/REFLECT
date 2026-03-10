package me.madhushan.reflect.utils;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import me.madhushan.reflect.BuildConfig;

import org.json.JSONObject;

import java.util.Arrays;

/**
 * FacebookSignInHelper
 *
 * Wraps the Facebook Login SDK into a simple helper with a Callback interface,
 * matching the style of GoogleSignInHelper.java already used in the project.
 *
 * Usage in Activity:
 *   1. Construct in onCreate() BEFORE setContentView() or at field level with register().
 *   2. Call signIn() when the Facebook button is clicked.
 *   3. Handle onSuccess / onFailure in your Callback implementation.
 *
 * Permissions requested: "email" and "public_profile"
 * (No special App Review needed for these two basic permissions.)
 */
public class FacebookSignInHelper {

    public interface Callback {
        /** Called on the UI thread with the user's name, email, and optional photo URL. */
        void onSuccess(String displayName, String email, String photoUrl);

        /** Called on the UI thread with a human-readable error message. */
        void onFailure(String errorMessage);
    }

    private final AppCompatActivity activity;
    private final CallbackManager callbackManager;

    /**
     * Must be constructed BEFORE Activity's onStart() — typically in onCreate()
     * or as an instance field initialiser so the ActivityResultLauncher is
     * registered at the right lifecycle stage.
     */
    public FacebookSignInHelper(AppCompatActivity activity) {
        this.activity = activity;
        this.callbackManager = CallbackManager.Factory.create();

        // Register the Facebook callback
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        fetchUserProfile(loginResult.getAccessToken(), currentCallback);
                    }

                    @Override
                    public void onCancel() {
                        if (currentCallback != null) {
                            currentCallback.onFailure("Facebook sign-in was cancelled.");
                        }
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        if (currentCallback != null) {
                            currentCallback.onFailure("Facebook sign-in failed: " + exception.getMessage());
                        }
                    }
                });
    }

    // Holds the callback for the current sign-in attempt
    private Callback currentCallback;

    /**
     * Call this from your Activity's onActivityResult() override.
     * Required so the Facebook SDK can process its login result.
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Start the Facebook Login flow.
     * @param callback receives onSuccess or onFailure on the UI thread.
     */
    public void signIn(Callback callback) {
        this.currentCallback = callback;

        // Guard: check that the App ID has been set in local.properties
        if (BuildConfig.FACEBOOK_APP_ID.startsWith("YOUR_") ||
                BuildConfig.FACEBOOK_APP_ID.startsWith("MISSING_")) {
            callback.onFailure("Facebook Sign-In is not configured.\n\nAdd FACEBOOK_APP_ID and FACEBOOK_CLIENT_TOKEN to local.properties.");
            return;
        }

        // If the user is already logged in via Facebook, use the cached token
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken != null && !accessToken.isExpired()) {
            fetchUserProfile(accessToken, callback);
            return;
        }

        // Otherwise launch the Facebook Login dialog
        LoginManager.getInstance().logInWithReadPermissions(
                activity,
                Arrays.asList("email", "public_profile")
        );
    }

    /**
     * After a successful login, call the Graph API to get name, email, and photo.
     */
    private void fetchUserProfile(AccessToken token, Callback callback) {
        GraphRequest request = GraphRequest.newMeRequest(token, (object, response) -> {
            activity.runOnUiThread(() -> {
                try {
                    if (object == null) {
                        callback.onFailure("Could not fetch Facebook profile. Please try again.");
                        return;
                    }

                    String name     = object.optString("name", "");
                    String email    = object.optString("email", "");
                    String photoUrl = "";

                    // Extract profile picture URL
                    if (object.has("picture")) {
                        JSONObject picture = object.getJSONObject("picture");
                        if (picture.has("data")) {
                            photoUrl = picture.getJSONObject("data").optString("url", "");
                        }
                    }

                    if (email.isEmpty()) {
                        // Facebook may not return email if user hasn't granted it
                        // or if there is no email associated with their Facebook account.
                        // Use a Facebook UID-based fallback email.
                        String uid = object.optString("id", "unknown");
                        email = "fb_" + uid + "@facebook.reflect";
                    }

                    callback.onSuccess(name, email, photoUrl);

                } catch (Exception e) {
                    callback.onFailure("Error reading Facebook profile: " + e.getMessage());
                }
            });
        });

        // Request fields: id, name, email, and a 200x200 profile picture
        android.os.Bundle params = new android.os.Bundle();
        params.putString("fields", "id,name,email,picture.type(large)");
        request.setParameters(params);
        request.executeAsync();
    }

    /** Log out from Facebook (call when user logs out of the app). */
    public static void logOut() {
        LoginManager.getInstance().logOut();
    }
}





