package me.madhushan.reflect;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.madhushan.reflect.database.AppDatabase;
import me.madhushan.reflect.database.VisionBoardItem;
import me.madhushan.reflect.database.VisionBoardItemDao;
import me.madhushan.reflect.utils.SessionManager;

public class VisionBoardActivity extends AppCompatActivity {

    private VisionBoardAdapter adapter;
    private VisionBoardItemDao dao;
    private SessionManager sessionManager;
    private ExecutorService executor;

    private LinearLayout layoutEmpty;
    private RecyclerView recyclerView;
    private TextView tvItemCount;
    private TextView tvSectionLabel;

    private final ActivityResultLauncher<Intent> addEditLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                loadItems(); // refresh after add / edit
            }
        });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        setContentView(R.layout.visionboard);

        dao            = AppDatabase.getInstance(this).visionBoardItemDao();
        sessionManager = new SessionManager(this);
        executor       = Executors.newSingleThreadExecutor();

        // Views
        layoutEmpty  = findViewById(R.id.layout_empty);
        recyclerView = findViewById(R.id.rv_vision_board);
        tvItemCount  = findViewById(R.id.tv_item_count);

        // RecyclerView — 2-column grid
        adapter = new VisionBoardAdapter(this);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);

        // Item interactions
        adapter.setListener(new VisionBoardAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(VisionBoardItem item) {
                showItemOptionsDialog(item);
            }

            @Override
            public void onItemLongClick(VisionBoardItem item) {
                confirmDelete(item);
            }
        });

        // Back button
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // FAB → add new item
        FloatingActionButton fab = findViewById(R.id.fab_add);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddVisionItemActivity.class);
            addEditLauncher.launch(intent);
        });

        loadItems();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadItems();
    }

    private void loadItems() {
        int userId = sessionManager.getUserId();
        executor.execute(() -> {
            List<VisionBoardItem> items = dao.getItemsForUser(userId);
            runOnUiThread(() -> {
                adapter.setItems(items);
                if (items.isEmpty()) {
                    layoutEmpty.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    tvItemCount.setVisibility(View.GONE);
                } else {
                    layoutEmpty.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    tvItemCount.setVisibility(View.VISIBLE);
                    tvItemCount.setText(items.size() + " vision" + (items.size() == 1 ? "" : "s") + " on your board");
                }
            });
        });
    }

    /** Tap on a card → show options: Edit or Delete */
    private void showItemOptionsDialog(VisionBoardItem item) {
        String[] options = { "✏️  Edit this vision", "🗑️  Delete this vision" };
        new AlertDialog.Builder(this)
            .setTitle(item.title)
            .setItems(options, (dialog, which) -> {
                if (which == 0) {
                    Intent intent = new Intent(this, AddVisionItemActivity.class);
                    intent.putExtra(AddVisionItemActivity.EXTRA_ITEM_ID, item.id);
                    addEditLauncher.launch(intent);
                } else {
                    confirmDelete(item);
                }
            })
            .show();
    }

    /** Long-press or delete option → confirm then delete */
    private void confirmDelete(VisionBoardItem item) {
        new AlertDialog.Builder(this)
            .setTitle("Delete Vision")
            .setMessage("Remove \"" + item.title + "\" from your board?")
            .setPositiveButton("Delete", (dialog, which) -> {
                executor.execute(() -> {
                    dao.deleteItem(item);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Vision removed", Toast.LENGTH_SHORT).show();
                        loadItems();
                    });
                });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
