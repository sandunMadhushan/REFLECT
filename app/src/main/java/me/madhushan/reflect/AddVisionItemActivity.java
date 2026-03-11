package me.madhushan.reflect;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;

import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.madhushan.reflect.database.AppDatabase;
import me.madhushan.reflect.database.VisionBoardItem;
import me.madhushan.reflect.database.VisionBoardItemDao;
import me.madhushan.reflect.utils.SessionManager;

/**
 * Lets the user add a new Vision Board item, or edit an existing one.
 * Pass EXTRA_ITEM_ID (int) to edit; omit it to add.
 */
public class AddVisionItemActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM_ID = "vision_item_id";

    private TextInputEditText etTitle, etNote;
    private ImageView ivPreview;
    private LinearLayout layoutPlaceholder;
    private TextView tvChangeImage;
    private TextView catLifeGoal, catCareer, catHealth, catTravel, catMindset, catFinance;

    private String selectedCategory = "Life Goal";
    private String selectedImageUri = null;
    private int editItemId = -1;

    private VisionBoardItemDao dao;
    private SessionManager sessionManager;
    private ExecutorService executor;

    private final ActivityResultLauncher<String> imagePickerLauncher =
        registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                // Persist read permission across restarts
                try {
                    getContentResolver().takePersistableUriPermission(uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (Exception ignored) {}
                selectedImageUri = uri.toString();
                ivPreview.setImageURI(uri);
                ivPreview.setVisibility(android.view.View.VISIBLE);
                layoutPlaceholder.setVisibility(android.view.View.GONE);
                tvChangeImage.setVisibility(android.view.View.VISIBLE);
            }
        });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        setContentView(R.layout.activity_add_vision_item);

        dao            = AppDatabase.getInstance(this).visionBoardItemDao();
        sessionManager = new SessionManager(this);
        executor       = Executors.newSingleThreadExecutor();

        // Views
        etTitle         = findViewById(R.id.et_title);
        etNote          = findViewById(R.id.et_note);
        ivPreview       = findViewById(R.id.iv_preview);
        layoutPlaceholder = findViewById(R.id.layout_placeholder);
        tvChangeImage   = findViewById(R.id.tv_change_image);
        catLifeGoal     = findViewById(R.id.cat_life_goal);
        catCareer       = findViewById(R.id.cat_career);
        catHealth       = findViewById(R.id.cat_health);
        catTravel       = findViewById(R.id.cat_travel);
        catMindset      = findViewById(R.id.cat_mindset);
        catFinance      = findViewById(R.id.cat_finance);

        // Check if editing
        editItemId = getIntent().getIntExtra(EXTRA_ITEM_ID, -1);
        if (editItemId != -1) {
            ((TextView) findViewById(R.id.tv_header_title)).setText("Edit Vision");
            ((TextView) ((LinearLayout) findViewById(R.id.btn_save)).getChildAt(0)).setText("Update Vision");
            loadExistingItem(editItemId);
        }

        // Category chips
        catLifeGoal.setOnClickListener(v -> selectCategory("Life Goal"));
        catCareer.setOnClickListener(v -> selectCategory("Career"));
        catHealth.setOnClickListener(v -> selectCategory("Health"));
        catTravel.setOnClickListener(v -> selectCategory("Travel"));
        catMindset.setOnClickListener(v -> selectCategory("Mindset"));
        catFinance.setOnClickListener(v -> selectCategory("Finance"));

        // Select default
        selectCategory(selectedCategory);

        // Image picker
        findViewById(R.id.card_image_picker).setOnClickListener(v ->
                imagePickerLauncher.launch("image/*"));

        // Back
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Save
        findViewById(R.id.btn_save).setOnClickListener(v -> saveItem());
    }

    private void loadExistingItem(int itemId) {
        executor.execute(() -> {
            VisionBoardItem item = dao.getItemById(itemId);
            if (item == null) return;
            runOnUiThread(() -> {
                etTitle.setText(item.title);
                etNote.setText(item.note);
                selectCategory(item.category != null ? item.category : "Life Goal");
                if (item.imageUri != null && !item.imageUri.isEmpty()) {
                    selectedImageUri = item.imageUri;
                    try {
                        ivPreview.setImageURI(Uri.parse(item.imageUri));
                        ivPreview.setVisibility(android.view.View.VISIBLE);
                        layoutPlaceholder.setVisibility(android.view.View.GONE);
                        tvChangeImage.setVisibility(android.view.View.VISIBLE);
                    } catch (Exception ignored) {}
                }
            });
        });
    }

    private void selectCategory(String category) {
        selectedCategory = category;
        // Reset all to inactive style
        for (TextView tv : new TextView[]{ catLifeGoal, catCareer, catHealth, catTravel, catMindset, catFinance }) {
            tv.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_chip_inactive));
            tv.setTextColor(ContextCompat.getColor(this, R.color.colorTextPrimary));
        }
        // Highlight selected
        TextView selected = chipForCategory(category);
        if (selected != null) {
            selected.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_chip_active));
            selected.setTextColor(ContextCompat.getColor(this, R.color.white));
        }
    }

    private TextView chipForCategory(String category) {
        if (category == null) return catLifeGoal;
        switch (category) {
            case "Career":   return catCareer;
            case "Health":   return catHealth;
            case "Travel":   return catTravel;
            case "Mindset":  return catMindset;
            case "Finance":  return catFinance;
            default:         return catLifeGoal;
        }
    }

    private void saveItem() {
        String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        String note  = etNote.getText() != null ? etNote.getText().toString().trim() : "";

        if (TextUtils.isEmpty(title)) {
            etTitle.setError("Title is required");
            etTitle.requestFocus();
            return;
        }

        executor.execute(() -> {
            if (editItemId != -1) {
                // Update existing
                VisionBoardItem item = dao.getItemById(editItemId);
                if (item != null) {
                    item.title    = title;
                    item.category = selectedCategory;
                    item.note     = note;
                    item.imageUri = selectedImageUri;
                    dao.updateItem(item);
                }
            } else {
                // Insert new
                VisionBoardItem item = new VisionBoardItem();
                item.userId    = sessionManager.getUserId();
                item.title     = title;
                item.category  = selectedCategory;
                item.note      = note;
                item.imageUri  = selectedImageUri;
                item.createdAt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(new Date());
                dao.insertItem(item);
            }
            runOnUiThread(() -> {
                Toast.makeText(this, editItemId != -1 ? "Vision updated!" : "Vision added to your board!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}

