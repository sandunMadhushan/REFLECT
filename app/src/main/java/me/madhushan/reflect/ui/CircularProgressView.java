package me.madhushan.reflect.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import me.madhushan.reflect.R;

/**
 * A lightweight circular progress ring drawn entirely via Canvas.
 * Track colour is resolved from the theme at construction time so it
 * correctly switches between light and dark mode.
 */
public class CircularProgressView extends View {

    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF oval = new RectF();

    private float progress = 0.625f; // 5/8 default

    public CircularProgressView(Context context) {
        super(context);
        init(context);
    }

    public CircularProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CircularProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        float strokeWidth = dpToPx(4);

        // Resolve track colour from theme attr (colorProgressTrack)
        TypedValue tv = new TypedValue();
        int trackColor;
        if (context.getTheme().resolveAttribute(R.attr.colorProgressTrack, tv, true)) {
            trackColor = tv.data;
        } else {
            trackColor = 0xFFE2E8F0; // fallback light grey
        }

        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeWidth(strokeWidth);
        trackPaint.setColor(trackColor);

        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(strokeWidth);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setColor(0xFF4e51e9); // brand primary
    }

    /** Set progress 0.0f – 1.0f */
    public void setProgress(float progress) {
        this.progress = Math.max(0f, Math.min(1f, progress));
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float stroke = dpToPx(4);
        float inset  = stroke / 2f;
        oval.set(inset, inset, getWidth() - inset, getHeight() - inset);

        // Track (full circle)
        canvas.drawArc(oval, -90, 360, false, trackPaint);
        // Progress arc
        canvas.drawArc(oval, -90, 360 * progress, false, progressPaint);
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
}

