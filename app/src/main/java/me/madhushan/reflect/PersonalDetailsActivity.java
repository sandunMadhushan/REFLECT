package me.madhushan.reflect;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.madhushan.reflect.database.AppDatabase;
import me.madhushan.reflect.database.UserDao;
import me.madhushan.reflect.utils.AvatarLoader;
import me.madhushan.reflect.utils.PasswordUtils;
import me.madhushan.reflect.utils.SessionManager;

public class PersonalDetailsActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private UserDao userDao;
    private ExecutorService executor;

    private TextView tvAvatarInitials;
    private ImageView ivAvatarPhoto;
    private EditText etFullName, etEmail, etCurrentPassword, etNewPassword, etConfirmPassword;
    private LinearLayout btnSave, btnDeleteAccount;

    // Temp URI for camera capture
    private Uri cameraImageUri;

    // ── Photo Picker (Android 13+ — no permission needed) ─────────────────
    private final ActivityResultLauncher<androidx.activity.result.PickVisualMediaRequest>
            photoPickerLauncher = registerForActivityResult(
                    new ActivityResultContracts.PickVisualMedia(), uri -> {
                        if (uri != null) saveAndDisplayPhoto(uri);
                    });

    // ── Open Document for Android 12 and below (persistent URI permission) ─
    private final ActivityResultLauncher<String[]> openDocumentLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri == null) return;
                try {
                    getContentResolver().takePersistableUriPermission(
                            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (SecurityException ignored) {}
                saveAndDisplayPhoto(uri);
            });

    // ── Open Document for Android 12 and below (persistent URI permission) ─
    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && cameraImageUri != null) saveAndDisplayPhoto(cameraImageUri);
            });

    // ── Camera permission request ──────────────────────────────────────────
    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) launchCamera();
                else Toast.makeText(this, "Camera permission denied.", Toast.LENGTH_SHORT).show();
            });

    // ── Gallery permission request (Android 12 and below) ─────────────────
    private final ActivityResultLauncher<String> storagePermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) openDocumentLauncher.launch(new String[]{"image/*"});
                else Toast.makeText(this, "Storage permission denied.", Toast.LENGTH_SHORT).show();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_details);

        sessionManager = new SessionManager(this);
        userDao = AppDatabase.getInstance(this).userDao();
        executor = Executors.newSingleThreadExecutor();

        initViews();
        populateData();
        setupListeners();
    }

    private void initViews() {
        tvAvatarInitials  = findViewById(R.id.tv_avatar_initials);
        ivAvatarPhoto     = findViewById(R.id.iv_avatar_photo);
        etFullName        = findViewById(R.id.et_full_name);
        etEmail           = findViewById(R.id.et_email);
        etCurrentPassword = findViewById(R.id.et_current_password);
        etNewPassword     = findViewById(R.id.et_new_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnSave           = findViewById(R.id.btn_save);
        btnDeleteAccount  = findViewById(R.id.btn_delete_account);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Tapping avatar area opens the photo picker
        ivAvatarPhoto.setOnClickListener(v -> showPhotoPickerDialog());
        tvAvatarInitials.setOnClickListener(v -> showPhotoPickerDialog());
    }

    private void populateData() {
        String name  = sessionManager.getUserName();
        String email = sessionManager.getUserEmail();
        if (name  == null) name  = "";
        if (email == null) email = "";

        etFullName.setText(name);
        etEmail.setText(email);
        AvatarLoader.loadFromSession(this, ivAvatarPhoto, tvAvatarInitials, sessionManager);
    }

    // ── Photo picker dialog ────────────────────────────────────────────────

    private void showPhotoPickerDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Change Profile Photo")
                .setItems(new String[]{"Take Photo", "Choose from Gallery", "Remove Photo"},
                        (dialog, which) -> {
                            if (which == 0) requestCameraAndCapture();
                            else if (which == 1) requestGalleryAndPick();
                            else removePhoto();
                        })
                .show();
    }

    private void requestCameraAndCapture() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void launchCamera() {
        try {
            File photoDir = new File(getCacheDir(), "camera_photos");
            if (!photoDir.exists()) photoDir.mkdirs();
            File photoFile = new File(photoDir, "profile_" + System.currentTimeMillis() + ".jpg");
            cameraImageUri = FileProvider.getUriForFile(
                    this, getPackageName() + ".fileprovider", photoFile);
            cameraLauncher.launch(cameraImageUri);
        } catch (Exception e) {
            Toast.makeText(this, "Cannot open camera.", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestGalleryAndPick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ — use the system Photo Picker (no permission needed)
            photoPickerLauncher.launch(
                    new androidx.activity.result.PickVisualMediaRequest.Builder()
                            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                            .build());
        } else {
            // Android 12 and below — use OpenDocument for persistent URI access
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                openDocumentLauncher.launch(new String[]{"image/*"});
            } else {
                storagePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
    }

    /**
     * Reads the image bytes fully on the MAIN thread while URI permission is
     * guaranteed valid, then writes to app-private storage on a background thread.
     */
    private void saveAndDisplayPhoto(Uri sourceUri) {
        // Read ALL bytes on main thread — URI access is guaranteed here
        final byte[] imageBytes;
        try (InputStream in = getContentResolver().openInputStream(sourceUri)) {
            if (in == null) {
                Toast.makeText(this, "Could not read selected image.", Toast.LENGTH_SHORT).show();
                return;
            }
            java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
            byte[] chunk = new byte[4096];
            int len;
            while ((len = in.read(chunk)) > 0) buffer.write(chunk, 0, len);
            imageBytes = buffer.toByteArray();
        } catch (Exception e) {
            Toast.makeText(this, "Could not open image. Try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Write bytes to private storage on background thread
        executor.execute(() -> {
            try {
                File destDir = new File(getFilesDir(), "profile_photos");
                if (!destDir.exists()) destDir.mkdirs();

                // Use a unique timestamp filename so Glide never serves a stale cached version
                String newFileName = "avatar_" + sessionManager.getUserId()
                        + "_" + System.currentTimeMillis() + ".jpg";
                File destFile = new File(destDir, newFileName);

                try (OutputStream out = new FileOutputStream(destFile)) {
                    out.write(imageBytes);
                }

                // Delete the old photo file to free space
                String oldPath = sessionManager.getLocalPhotoPath();
                if (oldPath != null) {
                    File oldFile = new File(oldPath);
                    if (oldFile.exists()) oldFile.delete();
                }

                // Save new path AFTER file is fully written
                sessionManager.setLocalPhotoPath(destFile.getAbsolutePath());
                final File finalFile = destFile;

                runOnUiThread(() -> {
                    ivAvatarPhoto.setVisibility(android.view.View.VISIBLE);
                    tvAvatarInitials.setVisibility(android.view.View.INVISIBLE);
                    if (ivAvatarPhoto.getParent() instanceof android.view.ViewGroup) {
                        ((android.view.ViewGroup) ivAvatarPhoto.getParent()).setBackground(null);
                    }
                    // Use unique signature + skip all caches so new image always loads fresh
                    Glide.with(this)
                            .load(finalFile)
                            .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                            .signature(new com.bumptech.glide.signature.ObjectKey(finalFile.getAbsolutePath()))
                            .skipMemoryCache(true)
                            .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                            .into(ivAvatarPhoto);
                    Toast.makeText(this, "Profile photo updated!", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Failed to save photo. Try again.", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void removePhoto() {
        // Delete the local photo file
        String localPath = sessionManager.getLocalPhotoPath();
        if (localPath != null) {
            File f = new File(localPath);
            if (f.exists()) f.delete();
        }
        sessionManager.clearLocalPhoto();

        // Reload avatar (will fall back to Google URL or initials)
        AvatarLoader.loadFromSession(this, ivAvatarPhoto, tvAvatarInitials, sessionManager);
        Toast.makeText(this, "Profile photo removed.", Toast.LENGTH_SHORT).show();
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveChanges());
        btnDeleteAccount.setOnClickListener(v -> confirmDeleteAccount());
    }

    private void saveChanges() {
        String newName    = etFullName.getText().toString().trim();
        String currentPwd = etCurrentPassword.getText().toString();
        String newPwd     = etNewPassword.getText().toString();
        String confirmPwd = etConfirmPassword.getText().toString();

        if (TextUtils.isEmpty(newName)) {
            etFullName.setError(getString(R.string.personal_details_error_name_empty));
            etFullName.requestFocus();
            return;
        }

        boolean changingPassword = !TextUtils.isEmpty(currentPwd) ||
                                   !TextUtils.isEmpty(newPwd) ||
                                   !TextUtils.isEmpty(confirmPwd);

        String email = sessionManager.getUserEmail();

        if (changingPassword) {
            if (newPwd.length() < 6) {
                etNewPassword.setError(getString(R.string.personal_details_error_pwd_short));
                etNewPassword.requestFocus();
                return;
            }
            if (!newPwd.equals(confirmPwd)) {
                etConfirmPassword.setError(getString(R.string.personal_details_error_pwd_mismatch));
                etConfirmPassword.requestFocus();
                return;
            }

            String hashedCurrent = PasswordUtils.hash(currentPwd);
            String finalNewName = newName;
            executor.execute(() -> {
                me.madhushan.reflect.database.User user = userDao.findByEmail(email);
                if (user == null || !user.passwordHash.equals(hashedCurrent)) {
                    runOnUiThread(() -> {
                        etCurrentPassword.setError(getString(R.string.personal_details_error_current_pwd));
                        etCurrentPassword.requestFocus();
                    });
                    return;
                }
                userDao.updateNameAndPassword(email, finalNewName, PasswordUtils.hash(newPwd));
                runOnUiThread(() -> {
                    sessionManager.setUserName(finalNewName);
                    tvAvatarInitials.setText(AvatarLoader.getInitials(finalNewName));
                    etCurrentPassword.setText("");
                    etNewPassword.setText("");
                    etConfirmPassword.setText("");
                    Toast.makeText(this, getString(R.string.personal_details_success_password), Toast.LENGTH_SHORT).show();
                    finish();
                });
            });
        } else {
            String finalNewName = newName;
            executor.execute(() -> {
                userDao.updateName(email, finalNewName);
                runOnUiThread(() -> {
                    sessionManager.setUserName(finalNewName);
                    tvAvatarInitials.setText(AvatarLoader.getInitials(finalNewName));
                    Toast.makeText(this, getString(R.string.personal_details_success_name), Toast.LENGTH_SHORT).show();
                    finish();
                });
            });
        }
    }

    private void confirmDeleteAccount() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.personal_details_delete_confirm_title)
                .setMessage(R.string.personal_details_delete_confirm_msg)
                .setPositiveButton(R.string.personal_details_delete_confirm_yes, (dialog, which) -> deleteAccount())
                .setNegativeButton(R.string.personal_details_delete_confirm_cancel, null)
                .show();
    }

    private void deleteAccount() {
        String email = sessionManager.getUserEmail();
        executor.execute(() -> {
            me.madhushan.reflect.database.User user = userDao.findByEmail(email);
            if (user != null) userDao.deleteUser(user);
            // Clean up local photo
            String localPath = sessionManager.getLocalPhotoPath();
            if (localPath != null) { File f = new File(localPath); if (f.exists()) f.delete(); }
            sessionManager.clearSession();
            runOnUiThread(() -> {
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) executor.shutdown();
    }
}














