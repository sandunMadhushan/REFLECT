package me.madhushan.reflect.utils;

import android.content.Context;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.List;

import me.madhushan.reflect.R;

/**
 * Loads daily inspiration quotes and background images from res/xml/inspirations.xml.
 *
 * To add a new quote or image simply edit inspirations.xml — no Java changes needed.
 */
public class InspirationLoader {

    /** A single inspiration entry: the quote text, author, and drawable resource id. */
    public static class Inspiration {
        public final String quote;
        public final String author;
        public final int    imageResId;

        Inspiration(String quote, String author, int imageResId) {
            this.quote      = quote;
            this.author     = author;
            this.imageResId = imageResId;
        }
    }

    private static List<String[]> cachedQuotes;
    private static List<String>   cachedImages;

    /**
     * Returns today's inspiration, chosen by seeding {@link java.util.Random} with the
     * current date so the same entry is shown all day but rotates every midnight.
     */
    public static Inspiration getTodaysInspiration(Context ctx) {
        ensureLoaded(ctx);

        // Seed with today's date (YYYYMMDD) so it's stable all day
        java.util.Calendar cal  = java.util.Calendar.getInstance();
        long dateSeed = cal.get(java.util.Calendar.YEAR)  * 10000L
                      + (cal.get(java.util.Calendar.MONTH) + 1) * 100L
                      + cal.get(java.util.Calendar.DAY_OF_MONTH);
        java.util.Random rnd = new java.util.Random(dateSeed);

        String[] q     = cachedQuotes.get(rnd.nextInt(cachedQuotes.size()));
        String   imgName = cachedImages.get(rnd.nextInt(cachedImages.size()));
        int      resId = ctx.getResources().getIdentifier(imgName, "drawable", ctx.getPackageName());

        return new Inspiration(q[0], "\u2014 " + q[1], resId);
    }

    // ── private ──────────────────────────────────────────────────────────────

    private static void ensureLoaded(Context ctx) {
        if (cachedQuotes != null) return;
        cachedQuotes = new ArrayList<>();
        cachedImages = new ArrayList<>();
        try {
            XmlPullParser parser = ctx.getResources().getXml(R.xml.inspirations);
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    switch (parser.getName()) {
                        case "quote":
                            String text   = parser.getAttributeValue(null, "text");
                            String author = parser.getAttributeValue(null, "author");
                            if (text != null && author != null)
                                cachedQuotes.add(new String[]{text, author});
                            break;
                        case "image":
                            String name = parser.getAttributeValue(null, "name");
                            if (name != null) cachedImages.add(name);
                            break;
                    }
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            // Fallback so the app never crashes
            if (cachedQuotes.isEmpty())
                cachedQuotes.add(new String[]{"Believe you can and you're halfway there.", "Theodore Roosevelt"});
            if (cachedImages.isEmpty())
                cachedImages.add("quote_img_1");
        }
    }
}



