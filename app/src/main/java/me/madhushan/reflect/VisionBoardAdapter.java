package me.madhushan.reflect;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import me.madhushan.reflect.database.VisionBoardItem;

public class VisionBoardAdapter extends RecyclerView.Adapter<VisionBoardAdapter.VisionViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(VisionBoardItem item);
        void onItemLongClick(VisionBoardItem item);
    }

    private final Context context;
    private final List<VisionBoardItem> items = new ArrayList<>();
    private OnItemClickListener listener;

    public VisionBoardAdapter(Context context) {
        this.context = context;
    }

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<VisionBoardItem> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VisionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_vision_board, parent, false);
        return new VisionViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VisionViewHolder holder, int position) {
        VisionBoardItem item = items.get(position);

        // Title
        holder.tvTitle.setText(item.title);

        // Category label
        holder.tvCategory.setText(item.category != null ? item.category.toUpperCase() : "");
        applyCategoryStyle(holder.tvCategory, item.category);

        // Image: load from URI if available, else show a colour placeholder
        if (item.imageUri != null && !item.imageUri.isEmpty()) {
            try {
                holder.ivImage.setImageURI(Uri.parse(item.imageUri));
                holder.ivImage.setBackgroundColor(0x00000000);
            } catch (Exception e) {
                setPlaceholderColor(holder.ivImage, item.category);
            }
        } else {
            holder.ivImage.setImageURI(null);
            setPlaceholderColor(holder.ivImage, item.category);
        }

        // Click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });

        // Long-press → delete confirmation
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onItemLongClick(item);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /** Apply a colour tint to the category badge based on category name. */
    private void applyCategoryStyle(TextView tv, String category) {
        if (category == null) return;
        int bgColor;
        switch (category.toLowerCase()) {
            case "career":    bgColor = 0xFF2D9E3F; break; // green
            case "health":    bgColor = 0xFF0E8EC4; break; // blue
            case "travel":    bgColor = 0xFF8E44AD; break; // purple
            case "mindset":   bgColor = 0xFFE67E22; break; // orange
            case "finance":   bgColor = 0xFFC0392B; break; // red
            default:          bgColor = 0xFFE05C2C; break; // orange-red for Life Goal
        }
        tv.setBackgroundColor(bgColor);
        // Apply rounded corners programmatically via the drawable wrapper
        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setColor(bgColor);
        bg.setCornerRadius(32f);
        tv.setBackground(bg);
    }

    /** Set a solid background color as placeholder when there's no image. */
    private void setPlaceholderColor(ImageView iv, String category) {
        int color;
        switch (category != null ? category.toLowerCase() : "") {
            case "career":    color = 0xFF1A3A2A; break;
            case "health":    color = 0xFF0D2B3E; break;
            case "travel":    color = 0xFF1E1033; break;
            case "mindset":   color = 0xFF2A1800; break;
            case "finance":   color = 0xFF2A0A0A; break;
            default:          color = 0xFF1A1030; break;
        }
        iv.setBackgroundColor(color);
        iv.setImageDrawable(null);
    }

    static class VisionViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvCategory, tvTitle;
        FrameLayout overlayDelete;

        VisionViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage       = itemView.findViewById(R.id.iv_card_image);
            tvCategory    = itemView.findViewById(R.id.tv_card_category);
            tvTitle       = itemView.findViewById(R.id.tv_card_title);
            overlayDelete = itemView.findViewById(R.id.overlay_delete);
        }
    }
}

