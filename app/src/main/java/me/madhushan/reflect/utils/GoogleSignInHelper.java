package me.madhushan.reflect.utils;

import android.content.Context;
import android.util.Log;

import androidx.credentials.ClearCredentialStateRequest;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.ClearCredentialException;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.fragment.app.FragmentActivity;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import me.madhushan.reflect.R;

/**
 * Helper that wraps the Credential Manager Google Sign-In flow.
 *
 * Usage:
 *   GoogleSignInHelper helper = new GoogleSignInHelper(activity);
 *   helper.signIn(new GoogleSignInHelper.Callback() { ... });
 */
public class GoogleSignInHelper {

    private static final String TAG = "GoogleSignInHelper";

    public interface Callback {
        /** Called on the main thread when sign-in succeeds. */
        void onSuccess(String idToken, String displayName, String email, String photoUrl);
        /** Called on the main thread when sign-in fails or is cancelled. */
        void onFailure(String errorMessage);
    }

    private final FragmentActivity activity;
    private final CredentialManager credentialManager;
    private final Executor executor = Executors.newSingleThreadExecutor();

    public GoogleSignInHelper(FragmentActivity activity) {
        this.activity = activity;
        this.credentialManager = CredentialManager.create(activity);
    }

    /** Launch the Google sign-in bottom sheet. */
    public void signIn(Callback callback) {
        String webClientId = activity.getString(R.string.default_web_client_id);

        if (webClientId.startsWith("YOUR_WEB_CLIENT_ID")) {
            callback.onFailure("Google Sign-In is not configured yet.\n\nAdd your Web Client ID in strings.xml.");
            return;
        }

        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)   // show all accounts, not just previously used
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)            // always show picker
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        credentialManager.getCredentialAsync(
                activity,
                request,
                null,
                executor,
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        handleSignInResult(result, callback);
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        Log.e(TAG, "Google sign-in failed", e);
                        activity.runOnUiThread(() ->
                                callback.onFailure("Google sign-in failed: " + e.getMessage()));
                    }
                });
    }

    /** Sign out / clear Google credential state. */
    public void signOut() {
        credentialManager.clearCredentialStateAsync(
                new ClearCredentialStateRequest(),
                null,
                executor,
                new CredentialManagerCallback<Void, ClearCredentialException>() {
                    @Override public void onResult(Void result) {
                        Log.d(TAG, "Google credential state cleared");
                    }
                    @Override public void onError(ClearCredentialException e) {
                        Log.e(TAG, "Failed to clear credential state", e);
                    }
                });
    }

    private void handleSignInResult(GetCredentialResponse response, Callback callback) {
        if (response.getCredential() instanceof CustomCredential) {
            CustomCredential credential = (CustomCredential) response.getCredential();
            if (GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(credential.getType())) {
                try {
                    GoogleIdTokenCredential googleCredential =
                            GoogleIdTokenCredential.createFrom(credential.getData());

                    String idToken     = googleCredential.getIdToken();
                    String displayName = googleCredential.getDisplayName() != null
                            ? googleCredential.getDisplayName() : "Google User";
                    String email       = googleCredential.getId(); // email address
                    String photoUrl    = googleCredential.getProfilePictureUri() != null
                            ? googleCredential.getProfilePictureUri().toString() : null;

                    activity.runOnUiThread(() ->
                            callback.onSuccess(idToken, displayName, email, photoUrl));
                } catch (Exception e) {
                    Log.e(TAG, "Failed to parse Google ID token credential", e);
                    activity.runOnUiThread(() ->
                            callback.onFailure("Failed to parse Google credentials."));
                }
            } else {
                activity.runOnUiThread(() ->
                        callback.onFailure("Unexpected credential type."));
            }
        } else {
            activity.runOnUiThread(() ->
                    callback.onFailure("Unexpected credential type."));
        }
    }
}

