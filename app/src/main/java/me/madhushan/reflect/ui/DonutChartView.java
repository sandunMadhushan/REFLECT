package me.madhushan.reflect.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple donut (ring) chart for the Progress Analytics screen.
 * Each segment is defined by a color and a percentage (0–100).
 */
public class DonutChartView extends View {

    public static class Segment {
        public final int   color;
        public final float percent; // 0–100
        public Segment(int color, float percent) {
            this.color   = color;
            this.percent = percent;
        }
    }

    private final List<Segment> segments = new ArrayList<>();
    private final Paint paint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF oval    = new RectF();
    private float strokeWidth   = 40f;

    public DonutChartView(Context ctx) { super(ctx); }
    public DonutChartView(Context ctx, AttributeSet attrs) { super(ctx, attrs); }
    public DonutChartView(Context ctx, AttributeSet attrs, int defStyle) { super(ctx, attrs, defStyle); }

    /** Replace current segments and redraw. */
    public void setSegments(List<Segment> segs) {
        segments.clear();
        segments.addAll(segs);
        invalidate();
    }

    /** Stroke width in dp. */
    public void setStrokeWidthDp(float dp) {
        strokeWidth = dp * getResources().getDisplayMetrics().density;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (segments.isEmpty()) return;

        int w = getWidth();
        int h = getHeight();
        float cx = w / 2f;
        float cy = h / 2f;
        float radius = Math.min(cx, cy) - strokeWidth / 2f;

        oval.set(cx - radius, cy - radius, cx + radius, cy + radius);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        paint.setStrokeCap(Paint.Cap.BUTT);

        // Draw background ring
        paint.setColor(Color.parseColor("#E2E8F0"));
        canvas.drawOval(oval, paint);

        float startAngle = -90f; // Start from top
        float gap        = 2f;   // degrees gap between segments
        for (Segment seg : segments) {
            float sweep = seg.percent / 100f * (360f - gap * segments.size());
            paint.setColor(seg.color);
            canvas.drawArc(oval, startAngle, sweep, false, paint);
            startAngle += sweep + gap;
        }
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        // Always square
        int size = resolveSize((int)(120 * getResources().getDisplayMetrics().density), widthSpec);
        setMeasuredDimension(size, size);
    }
}

