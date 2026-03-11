package me.madhushan.reflect;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.madhushan.reflect.database.AppDatabase;
import me.madhushan.reflect.database.AppNotification;
import me.madhushan.reflect.database.AppNotificationDao;
import me.madhushan.reflect.utils.SessionManager;

public class NotificationsActivity extends AppCompatActivity {

    private AppNotificationDao dao;
    private SessionManager sessionManager;
    private ExecutorService executor;

    private RecyclerView rvNotifications;
    private LinearLayout layoutEmpty;
    private TextView btnMarkAllRead;
    private NotificationAdapter adapter;
    private final List<AppNotification> notifications = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        sessionManager   = new SessionManager(this);
        dao              = AppDatabase.getInstance(this).appNotificationDao();
        executor         = Executors.newSingleThreadExecutor();

        rvNotifications = findViewById(R.id.rv_notifications);
        layoutEmpty     = findViewById(R.id.layout_empty);
        btnMarkAllRead  = findViewById(R.id.btn_mark_all_read);

        adapter = new NotificationAdapter(this, notifications, notif -> {
            // Mark as read on click
            if (notif.isRead == 0) {
                notif.isRead = 1;
                executor.execute(() -> {
                    dao.markRead(notif.id);
                    runOnUiThread(() -> {
                        int idx = notifications.indexOf(notif);
                        if (idx >= 0) adapter.notifyItemChanged(idx);
                        updateMarkAllBtn();
                    });
                });
            }
        });

        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        rvNotifications.setAdapter(adapter);
        rvNotifications.addItemDecoration(new SpaceItemDecoration(10));

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        btnMarkAllRead.setOnClickListener(v -> {
            int userId = sessionManager.getUserId();
            executor.execute(() -> {
                dao.markAllRead(userId);
                runOnUiThread(() -> {
                    for (AppNotification n : notifications) n.isRead = 1;
                    adapter.notifyDataSetChanged();
                    btnMarkAllRead.setVisibility(View.GONE);
                });
            });
        });

        loadNotifications();
    }

    private void loadNotifications() {
        int userId = sessionManager.getUserId();
        executor.execute(() -> {
            List<AppNotification> list = dao.getNotificationsForUser(userId);
            runOnUiThread(() -> {
                notifications.clear();
                notifications.addAll(list);
                adapter.notifyDataSetChanged();

                boolean empty = notifications.isEmpty();
                layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
                rvNotifications.setVisibility(empty ? View.GONE : View.VISIBLE);
                updateMarkAllBtn();
            });
        });
    }

    private void updateMarkAllBtn() {
        boolean hasUnread = notifications.stream().anyMatch(n -> n.isRead == 0);
        btnMarkAllRead.setVisibility(hasUnread ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) executor.shutdown();
    }

    /** Simple vertical spacing between RecyclerView items. */
    static class SpaceItemDecoration extends RecyclerView.ItemDecoration {
        private final int spacePx;
        SpaceItemDecoration(int spaceDp) {
            // Convert dp to px at creation time — safe since we pass context-independent value
            this.spacePx = spaceDp;
        }
        @Override
        public void getItemOffsets(@androidx.annotation.NonNull android.graphics.Rect outRect,
                                   @androidx.annotation.NonNull View view,
                                   @androidx.annotation.NonNull RecyclerView parent,
                                   @androidx.annotation.NonNull RecyclerView.State state) {
            float density = view.getContext().getResources().getDisplayMetrics().density;
            outRect.bottom = (int)(spacePx * density);
        }
    }
}

