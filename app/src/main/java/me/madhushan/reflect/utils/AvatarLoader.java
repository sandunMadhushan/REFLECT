package me.madhushan.reflect.utils;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;

import me.madhushan.reflect.R;

/**
 * Utility to load the user's avatar in any screen.
 *
 * - If the user signed in with Google and has a photo URL → load it with Glide,
 *   show the ImageView, hide the initials TextView.
 * - Otherwise → show the initials TextView, hide the ImageView.
 */
public class AvatarLoader {

    /**
     * @param ivPhoto    ImageView that holds the Google profile photo (can be null)
     * @param tvInitials TextView that shows initials fallback
     * @param photoUrl   URL from SessionManager.getPhotoUrl() — null if not a Google user
     * @param initials   2-letter initials string
     */
    public static void load(ImageView ivPhoto, TextView tvInitials,
                            String photoUrl, String initials) {

        if (photoUrl != null && !photoUrl.isEmpty() && ivPhoto != null) {
            // Show photo, hide initials, clear gradient background from parent
            ivPhoto.setVisibility(View.VISIBLE);
            tvInitials.setVisibility(View.INVISIBLE);
            // Remove the gradient background from the parent so only the photo shows
            if (ivPhoto.getParent() instanceof android.view.ViewGroup) {
                ((android.view.ViewGroup) ivPhoto.getParent()).setBackground(null);
            }

            Glide.with(ivPhoto.getContext())
                    .load(photoUrl)
                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                    .placeholder(R.drawable.bg_avatar_placeholder)
                    .error(R.drawable.bg_avatar_placeholder)
                    .into(ivPhoto);
        } else {
            // No photo — show initials, hide photo, restore gradient background
            if (ivPhoto != null) {
                ivPhoto.setVisibility(View.GONE);
                // Restore the gradient background on the parent
                if (ivPhoto.getParent() instanceof android.view.ViewGroup) {
                    android.view.ViewGroup parent = (android.view.ViewGroup) ivPhoto.getParent();
                    parent.setBackgroundResource(R.drawable.bg_logo_gradient);
                }
            }
            tvInitials.setVisibility(View.VISIBLE);
            tvInitials.setText(initials);
        }
    }

    /** Compute up-to-2-letter initials from a full name. */
    public static String getInitials(String fullName) {
        if (fullName == null || fullName.isEmpty()) return "?";
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) return String.valueOf(parts[0].charAt(0)).toUpperCase();
        return (String.valueOf(parts[0].charAt(0)) +
                String.valueOf(parts[parts.length - 1].charAt(0))).toUpperCase();
    }
}


