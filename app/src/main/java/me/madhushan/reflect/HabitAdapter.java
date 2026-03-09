package me.madhushan.reflect;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.List;

/**
 * HabitAdapter — RecyclerView adapter for the Today's Habits list.
 *
 * Binds {@link HabitModel} data to {@code item_habit_row.xml}.
 * No Room / database dependency — works purely with dummy data.
 *
 * Place this file at:
 *   app/src/main/java/me/madhushan/reflect/HabitAdapter.java
 */
public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitViewHolder> {

    /** Callback fired whenever the user flips a habit's toggle switch. */
    public interface OnHabitToggleListener {
        void onToggle(HabitModel habit, boolean isCompleted);
    }

    private final Context context;
    private final List<HabitModel> habits;
    private OnHabitToggleListener toggleListener;

    public HabitAdapter(Context context, List<HabitModel> habits) {
        this.context = context;
        this.habits  = habits;
    }

    /** Register a listener to be notified when a toggle changes. */
    public void setOnHabitToggleListener(OnHabitToggleListener listener) {
        this.toggleListener = listener;
    }

    // ── RecyclerView.Adapter overrides ────────────────────────────────────────

    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_habit_row, parent, false);
        return new HabitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
        HabitModel habit = habits.get(position);

        // Title
        holder.tvName.setText(habit.name != null ? habit.name : "");

        // Subtitle / description
        holder.tvSubtitle.setText(habit.description != null ? habit.description : "Daily");

        // Streak number (shown next to the 🔥 icon)
        holder.tvStreak.setText(String.valueOf(habit.streak));

        // Assign a cycling icon style based on position (indigo / green / pink / orange)
        applyIconStyle(holder.ivIcon, position);

        // Toggle switch — clear listener first to avoid triggering on rebind
        holder.switchDone.setOnCheckedChangeListener(null);
        holder.switchDone.setChecked(habit.completed);

        holder.switchDone.setOnCheckedChangeListener((btn, checked) -> {
            habit.completed = checked;           // update model in place
            if (toggleListener != null) {
                toggleListener.onToggle(habit, checked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return habits.size();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Cycle through 4 icon colours (indigo → green → pink → orange) and pick
     * the matching drawable to mimic the design sheet.
     */
    private void applyIconStyle(ImageView iv, int position) {
        switch (position % 4) {
            case 0: // Morning Meditation — indigo bg + meditation icon
                iv.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_ht_icon_indigo));
                iv.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_meditation));
                break;
            case 1: // Drink Water — green bg + water drop icon
                iv.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_ht_icon_green));
                iv.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_water_drop));
                break;
            case 2: // Read — pink bg + book icon
                iv.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_ht_icon_pink));
                iv.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_book));
                break;
            case 3: // Evening Workout — orange bg + fitness icon
            default:
                iv.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_ht_icon_orange));
                iv.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_fitness));
                break;
        }
    }

    // ── ViewHolder ────────────────────────────────────────────────────────────

    static class HabitViewHolder extends RecyclerView.ViewHolder {
        final ImageView      ivIcon;
        final TextView       tvName;
        final TextView       tvSubtitle;
        final TextView       tvStreak;
        final SwitchMaterial switchDone;

        HabitViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon     = itemView.findViewById(R.id.iv_habit_icon);
            tvName     = itemView.findViewById(R.id.tv_habit_name);
            tvSubtitle = itemView.findViewById(R.id.tv_habit_subtitle);
            tvStreak   = itemView.findViewById(R.id.tv_habit_streak);
            switchDone = itemView.findViewById(R.id.switch_habit_done);
        }
    }
}
