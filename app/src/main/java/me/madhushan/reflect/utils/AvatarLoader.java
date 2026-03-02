package me.madhushan.reflect.utils;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;

import java.io.File;

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
     * Load avatar from SessionManager — local photo takes priority over Google URL.
     * Call this convenience method from any Activity/Fragment.
     */
    public static void loadFromSession(Context context, ImageView ivPhoto,
                                       TextView tvInitials, SessionManager session) {
        String localPath = session.getLocalPhotoPath();
        String googleUrl = session.getPhotoUrl();
        String fullName  = session.getUserName();
        String initials  = getInitials(fullName != null ? fullName : "");

        if (localPath != null && !localPath.isEmpty()) {
            File localFile = new File(localPath);
            if (localFile.exists()) {
                // Local file — load directly with cache-busting signature
                if (ivPhoto != null) {
                    ivPhoto.setVisibility(View.VISIBLE);
                    tvInitials.setVisibility(View.INVISIBLE);
                    if (ivPhoto.getParent() instanceof android.view.ViewGroup) {
                        ((android.view.ViewGroup) ivPhoto.getParent()).setBackground(null);
                    }
                    Glide.with(context)
                            .load(localFile)
                            .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                            .signature(new ObjectKey(localFile.getAbsolutePath()))
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .placeholder(R.drawable.bg_avatar_placeholder)
                            .error(R.drawable.bg_avatar_placeholder)
                            .into(ivPhoto);
                }
                return;
            } else {
                // File missing — clear stale path
                session.clearLocalPhoto();
            }
        }

        // Fall back to Google URL or initials
        load(ivPhoto, tvInitials, googleUrl, initials);
    }

    /**
     * Core load method.
     * @param ivPhoto    ImageView for the photo (can be null for home screen small avatar)
     * @param tvInitials TextView for initials fallback
     * @param photoUrl   Remote URL or "file://..." local path — null/empty = show initials
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




