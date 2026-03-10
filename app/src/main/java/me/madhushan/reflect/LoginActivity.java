package me.madhushan.reflect;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.madhushan.reflect.database.AppDatabase;
import me.madhushan.reflect.database.User;
import me.madhushan.reflect.database.UserDao;
import me.madhushan.reflect.utils.FacebookSignInHelper;
import me.madhushan.reflect.utils.GoogleSignInHelper;
import me.madhushan.reflect.utils.PasswordUtils;
import me.madhushan.reflect.utils.SessionManager;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin, btnGoogle, btnFacebook;
    private TextView tvForgotPassword, tvRegister;

    private AppDatabase db;
    private SessionManager sessionManager;
    private GoogleSignInHelper googleSignInHelper;
    private FacebookSignInHelper facebookSignInHelper;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db                    = AppDatabase.getInstance(this);
        sessionManager        = new SessionManager(this);
        googleSignInHelper    = new GoogleSignInHelper(this);
        facebookSignInHelper  = new FacebookSignInHelper(this);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        tilEmail         = findViewById(R.id.til_email);
        tilPassword      = findViewById(R.id.til_password);
        etEmail          = findViewById(R.id.et_email);
        etPassword       = findViewById(R.id.et_password);
        btnLogin         = findViewById(R.id.btn_login);
        btnGoogle        = findViewById(R.id.btn_google);
        btnFacebook      = findViewById(R.id.btn_facebook);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        tvRegister       = findViewById(R.id.tv_register);
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> handleLogin());

        tvForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        btnGoogle.setOnClickListener(v -> handleGoogleSignIn());

        btnFacebook.setOnClickListener(v -> handleFacebookSignIn());

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }

    private void handleLogin() {
        String email    = text(etEmail);
        String password = text(etPassword);

        // ── Validation ──────────────────────────────────────────────────────
        boolean valid = true;

        if (email.isEmpty()) {
            tilEmail.setError("Email address is required");
            valid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Enter a valid email address");
            valid = false;
        } else {
            tilEmail.setError(null);
        }

        if (password.isEmpty()) {
            tilPassword.setError("Password is required");
            valid = false;
        } else if (password.length() < 6) {
            tilPassword.setError("Password must be at least 6 characters");
            valid = false;
        } else {
            tilPassword.setError(null);
        }

        if (!valid) return;

        // ── Disable button while authenticating ────────────────────────────
        btnLogin.setEnabled(false);

        final String finalEmail    = email;
        final String finalPassword = password;

        // ── Room DB query on background thread ──────────────────────────────
        executor.execute(() -> {
            UserDao dao          = db.userDao();
            String  passwordHash = PasswordUtils.hash(finalPassword);
            User    user         = dao.findByEmailAndPassword(finalEmail, passwordHash);

            runOnUiThread(() -> {
                btnLogin.setEnabled(true);
                if (user != null) {
                    // Save session and go to home
                    sessionManager.saveSession(user.id, user.fullName, user.email);
                    goToHome();
                } else {
                    // Show generic error — don't reveal which field is wrong
                    tilEmail.setError(" ");
                    tilPassword.setError("Incorrect email or password");
                }
            });
        });
    }

    private void handleGoogleSignIn() {
        btnGoogle.setEnabled(false);
        btnGoogle.setText(R.string.google_signing_in);

        googleSignInHelper.signIn(new GoogleSignInHelper.Callback() {
            @Override
            public void onSuccess(String idToken, String displayName, String email, String photoUrl) {
                // Save Google photo URL in session before navigating
                if (photoUrl != null && !photoUrl.isEmpty()) {
                    sessionManager.setPhotoUrl(photoUrl);
                }
                handleGoogleUser(displayName, email);
            }

            @Override
            public void onFailure(String errorMessage) {
                btnGoogle.setEnabled(true);
                btnGoogle.setText(R.string.btn_google);
                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleGoogleUser(String displayName, String email) {
        executor.execute(() -> {
            UserDao dao = db.userDao();
            User existing = dao.findByEmail(email);

            if (existing != null) {
                // User already registered — just log them in
                final User user = existing;
                runOnUiThread(() -> {
                    btnGoogle.setEnabled(true);
                    btnGoogle.setText(R.string.btn_google);
                    sessionManager.saveSession(user.id, user.fullName, user.email);
                    goToHome();
                });
            } else {
                // First time Google sign-in — auto-register with a random password hash
                User newUser = new User();
                newUser.fullName     = displayName;
                newUser.email        = email;
                // Store a Google-specific placeholder — no plain-text password needed
                newUser.passwordHash = "GOOGLE_AUTH_" + PasswordUtils.hash(email);

                long newId = dao.insertUser(newUser);
                runOnUiThread(() -> {
                    btnGoogle.setEnabled(true);
                    btnGoogle.setText(R.string.btn_google);
                    if (newId > 0) {
                        sessionManager.saveSession((int) newId, displayName, email);
                        Toast.makeText(LoginActivity.this,
                                "Welcome to Reflect, " + displayName + "! 🎉",
                                Toast.LENGTH_SHORT).show();
                        goToHome();
                    } else {
                        Toast.makeText(LoginActivity.this,
                                "Sign-in failed. Please try again.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void handleFacebookSignIn() {
        btnFacebook.setEnabled(false);
        btnFacebook.setText(R.string.facebook_signing_in);

        facebookSignInHelper.signIn(new FacebookSignInHelper.Callback() {
            @Override
            public void onSuccess(String displayName, String email, String photoUrl) {
                if (photoUrl != null && !photoUrl.isEmpty()) {
                    sessionManager.setPhotoUrl(photoUrl);
                }
                handleFacebookUser(displayName, email);
            }

            @Override
            public void onFailure(String errorMessage) {
                btnFacebook.setEnabled(true);
                btnFacebook.setText(R.string.btn_facebook);
                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleFacebookUser(String displayName, String email) {
        executor.execute(() -> {
            UserDao dao = db.userDao();
            User existing = dao.findByEmail(email);

            if (existing != null) {
                // Already registered — log them in
                final User user = existing;
                runOnUiThread(() -> {
                    btnFacebook.setEnabled(true);
                    btnFacebook.setText(R.string.btn_facebook);
                    sessionManager.saveSession(user.id, user.fullName, user.email);
                    goToHome();
                });
            } else {
                // First-time Facebook login — auto-register
                User newUser = new User();
                newUser.fullName     = displayName;
                newUser.email        = email;
                newUser.passwordHash = "FACEBOOK_AUTH_" + PasswordUtils.hash(email);

                long newId = dao.insertUser(newUser);
                runOnUiThread(() -> {
                    btnFacebook.setEnabled(true);
                    btnFacebook.setText(R.string.btn_facebook);
                    if (newId > 0) {
                        sessionManager.saveSession((int) newId, displayName, email);
                        Toast.makeText(LoginActivity.this,
                                "Welcome to Reflect, " + displayName + "! 🎉",
                                Toast.LENGTH_SHORT).show();
                        goToHome();
                    } else {
                        Toast.makeText(LoginActivity.this,
                                "Facebook sign-in failed. Please try again.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /**
     * Required by the Facebook SDK to process the login activity result.
     * Must be called from onActivityResult in the host Activity.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Forward to Facebook SDK first
        facebookSignInHelper.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void goToHome() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private String text(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
