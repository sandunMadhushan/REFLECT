package me.madhushan.reflect;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import me.madhushan.reflect.database.AppNotification;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.VH> {

    public interface OnItemClickListener {
        void onItemClick(AppNotification notification);
    }

    private final Context context;
    private final List<AppNotification> items;
    private final OnItemClickListener listener;

    public NotificationAdapter(Context context, List<AppNotification> items, OnItemClickListener listener) {
        this.context  = context;
        this.items    = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        AppNotification n = items.get(position);

        h.tvTitle.setText(n.title);
        h.tvMessage.setText(n.message);
        h.tvTime.setText(formatRelativeTime(n.createdAt));
        h.unreadDot.setVisibility(n.isRead == 0 ? View.VISIBLE : View.GONE);

        // Background tint for unread
        h.itemView.setAlpha(n.isRead == 0 ? 1f : 0.72f);

        // Icon by type
        int iconRes = iconForType(n.type);
        h.ivIcon.setImageResource(iconRes);

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(n);
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    private int iconForType(String type) {
        if (type == null) return R.drawable.ic_notifications;
        switch (type) {
            case AppNotification.TYPE_GOAL_ACHIEVED: return R.drawable.ic_check_circle;
            case AppNotification.TYPE_HABIT_STREAK:  return R.drawable.ic_fire;
            case AppNotification.TYPE_REFLECTION:    return R.drawable.ic_menu_book;
            case AppNotification.TYPE_HABIT_REMINDER:return R.drawable.ic_calendar;
            case AppNotification.TYPE_WELCOME:       return R.drawable.ic_favorite;
            default:                                 return R.drawable.ic_notifications;
        }
    }

    private String formatRelativeTime(String createdAt) {
        if (createdAt == null) return "";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date then = sdf.parse(createdAt);
            if (then == null) return createdAt;
            long diffMs = System.currentTimeMillis() - then.getTime();
            long mins  = TimeUnit.MILLISECONDS.toMinutes(diffMs);
            long hours = TimeUnit.MILLISECONDS.toHours(diffMs);
            long days  = TimeUnit.MILLISECONDS.toDays(diffMs);
            if (mins  < 1)  return "Just now";
            if (mins  < 60) return mins  + "m ago";
            if (hours < 24) return hours + "h ago";
            if (days  < 7)  return days  + "d ago";
            return new SimpleDateFormat("MMM d", Locale.getDefault()).format(then);
        } catch (ParseException e) {
            return createdAt;
        }
    }

    static class VH extends RecyclerView.ViewHolder {
        FrameLayout iconBg;
        ImageView   ivIcon;
        TextView    tvTitle, tvMessage, tvTime;
        View        unreadDot;

        VH(@NonNull View v) {
            super(v);
            iconBg    = v.findViewById(R.id.notif_icon_bg);
            ivIcon    = v.findViewById(R.id.notif_icon);
            tvTitle   = v.findViewById(R.id.notif_title);
            tvMessage = v.findViewById(R.id.notif_message);
            tvTime    = v.findViewById(R.id.notif_time);
            unreadDot = v.findViewById(R.id.notif_unread_dot);
        }
    }
}

