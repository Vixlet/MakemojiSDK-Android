package com.example.mojilib;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.widget.TextView;
import android.app.Application;

import com.squareup.picasso252.LruCache;
import com.squareup.picasso252.Picasso;

import org.ccil.cowan.tagsoup2.HTMLSchema;
import org.ccil.cowan.tagsoup2.Parser;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.content.pm.ApplicationInfo.FLAG_LARGE_HEAP;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.HONEYCOMB;

/**
 * Created by Scott Baar on 12/14/2015.
 */
public class Moji {
    static Context context;
    static Resources resources;

    //We use our own static import of Picasso to set our own caches, logging behavior, etc
    // and to not conflict with app-side picasso implementations
    static Picasso picasso;

    //our own html parser to create custom spans.
    private static Parser parser;

    //screen density
    static float density;

    //the index to tag textviews with our custom text watcher
    /**
     * Initialize the library. Required to set in {@link Application#onCreate()}  so that the library can load resources.
     * @param c The application object. Needed for resources and to register activity callbacks.
     * @param cacheSizeBytes the in-memory cache size in bytes
     */
    public static void initialize(Application c, int cacheSizeBytes){
        context = c.getApplicationContext();
        resources = c.getResources();
        density = resources.getDisplayMetrics().density;
        MojiSpan.BASE_TEXT_PX_SCALED = MojiSpan.BASE_TEXT_PT*density;

        c.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {
                Spanimator.onResume();
            }

            @Override
            public void onActivityPaused(Activity activity) {
                Spanimator.onPause();
            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });

        Picasso.Builder builder = new Picasso.Builder(context);


        int cacheSieBytes = calculateMemoryCacheSize(context);
        builder.memoryCache(new LruCache(cacheSieBytes));
        picasso = builder.build();
        parser = new Parser();
        try {
            parser.setProperty(Parser.schemaProperty, new HTMLSchema());
        }catch (Exception e){}
    }
    //calls initialize with the default cache size, 5%
    public static void initialize(Application c){
        initialize(c,calculateMemoryCacheSize(c));
    }

    /**
     * Parses an html message and converts it into a spanned, using the TextView to size the emoji spans properly.
     * Optionally applies other style attributes from html such as color, margin, and text size.
     * Call this after a TextView's
     * @param html the html message to parse
     * @param tv the TextView to set the text on. Used for sizing the emoji spans.
     * @param simple If true, will not apply any styling information beyond setting the parsed message.
     * @return Returns the parsed attributes from the html so you can cherry pick which styles to apply.
     */
    @UiThread
    public static ParsedAttributes setText(String html, @NonNull TextView tv, boolean simple){
        ParsedAttributes parsedAttributes =parseHtml(html,tv,simple);
        //if (tv.getTag(R.id._makemoji_textwatcher_tag_id)==null)
          //  setTextWatcher(tv);
        CharSequence cs = tv.getText();
        if (cs instanceof Spanned)
            unsubSpanimatable((Spanned)cs);

        tv.setText(parsedAttributes.spanned);

        MojiSpan[] mojiSpans = parsedAttributes.spanned.getSpans(0, parsedAttributes.spanned.length(), MojiSpan.class);
       // if (mojiSpans.length>0)Log.d("TextWatcher","Adding spans "+ mojiSpans.length);
        for (MojiSpan mojiSpan : mojiSpans) {
            if (mojiSpan.shouldAnimate())
                Spanimator.subscribe(Spanimator.HYPER_PULSE, mojiSpan);
        }
        if (!simple){
            tv.setPadding((int)(parsedAttributes.marginLeft *density),(int)(parsedAttributes.marginTop *density),
                    (int) (parsedAttributes.marginRight * density),(int)(parsedAttributes.marginBottom *density));
            if (parsedAttributes.color!=-1)tv.setTextColor(parsedAttributes.color);
            if (parsedAttributes.fontSizePt!=-1) tv.setTextSize(TypedValue.COMPLEX_UNIT_SP,parsedAttributes.fontSizePt);
        }
        return parsedAttributes;
    }
    static void unsubSpanimatable(Spanned spanned){
        Log.d("unsub","unsub called");
        MojiSpan[] mojiSpans = spanned.getSpans(0, spanned.length(), MojiSpan.class);
        // if (mojiSpans.length>0)Log.d("TextWatcher","Removing spans "+ mojiSpans.length);
        for (MojiSpan mojiSpan : mojiSpans) {
            if (mojiSpan.shouldAnimate())
                Spanimator.unsubscrube(Spanimator.HYPER_PULSE, mojiSpan);
        }

    }

    /**
     * Parse the html message without side effect. Returns the spanned and attributes
     * @param html the html message to parse
     * @param tv An optional textview to size the emoji spans.
     * @return
     */
    public static ParsedAttributes parseHtml(String html, @Nullable TextView tv){
        return parseHtml(html,tv,false);
    }
    public static ParsedAttributes parseHtml(String html, @Nullable TextView tv, boolean simple){
        return new SpanBuilder(html,null,null,parser,simple,tv).convert();
    }

    /**
     * watches the textview changing so we can subscribe and unsubscribe
     * @param tv
     */
    private static void setTextWatcher(TextView tv){
        tv.addTextChangedListener(textWatcher);
        tv.setTag(R.id._makemoji_textwatcher_tag_id,textWatcher);

    }
    private static TextWatcher textWatcher = new TextWatcher(){
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
           /* if (s instanceof Spanned) {
                Spanned spanned = (Spanned) s;
                MojiSpan[] mojiSpans = spanned.getSpans(0, spanned.length(), MojiSpan.class);
                if (mojiSpans.length>0)Log.d("TextWatcher","Removing spans "+ mojiSpans.length);
                for (MojiSpan mojiSpan : mojiSpans) {
                    if (mojiSpan.shouldAnimate()) {
                        Spanimator.unsubscrube(Spanimator.HYPER_PULSE, mojiSpan);
                    }
                }
            }*/
           if (s instanceof Spanned)
               unsubSpanimatable((Spanned)s);
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable spanned) {

        }};
    private static int calculateMemoryCacheSize(Context context) {
        ActivityManager am =(ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        boolean largeHeap = (context.getApplicationInfo().flags & FLAG_LARGE_HEAP) != 0;
        int memoryClass = am.getMemoryClass();
        if (largeHeap && SDK_INT >= HONEYCOMB) {
            memoryClass = ActivityManagerHoneycomb.getLargeMemoryClass(am);
        }
        // Target ~5% of the available heap.
        return 1024 * 1024 * memoryClass / 20;
    }
    @TargetApi(HONEYCOMB)
    private static class ActivityManagerHoneycomb {
        static int getLargeMemoryClass(ActivityManager activityManager) {
            return activityManager.getLargeMemoryClass();
        }
    }
}
