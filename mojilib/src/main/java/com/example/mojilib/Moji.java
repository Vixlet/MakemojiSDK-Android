package com.example.mojilib;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.CharacterStyle;
import android.util.Log;
import android.util.TypedValue;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.Application;
import android.widget.Toast;

import com.example.mojilib.model.MojiModel;
import com.squareup.picasso252.LruCache;
import com.squareup.picasso252.Picasso;

import org.ccil.cowan.tagsoup2.HTMLSchema;
import org.ccil.cowan.tagsoup2.Parser;
import org.w3c.dom.Text;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;

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

    //our own html parser to create custom spans. keep one for each thread.
    private static Map<Long,SoftReference<Parser>> parsers =new HashMap<>();
    //screen density
    static float density;

    static MojiApi mojiApi;
    //randomly seed some mojispans with links when in demo mode
    static boolean demo = false;
    static Handler handler = new Handler(Looper.getMainLooper());
    /**
     * Initialize the library. Required to set in {@link Application#onCreate()}  so that the library can load resources.
     * and activity lifecycle callbacks.
     * @param app The application object. Needed for resources and to register activity callbacks.
     * @param cacheSizeBytes the in-memory cache size in bytes
     */
    public static void initialize(Application app, int cacheSizeBytes){
        context = app.getApplicationContext();
        resources = context.getResources();
        density = resources.getDisplayMetrics().density;
        MojiSpan.BASE_TEXT_PX_SCALED = MojiSpan.BASE_TEXT_PT*density;

        app.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}
            @Override
            public void onActivityStarted(Activity activity) {}
            @Override
            public void onActivityResumed(Activity activity) {
                Spanimator.onResume();
            }
            @Override
            public void onActivityPaused(Activity activity) {
                Spanimator.onPause();
            }
            @Override
            public void onActivityStopped(Activity activity) {}
            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}
            @Override
            public void onActivityDestroyed(Activity activity) {}});

        Picasso.Builder builder = new Picasso.Builder(context);

        int cacheSieBytes = calculateMemoryCacheSize(context);
        builder.memoryCache(new LruCache(cacheSieBytes));
        //builder.loggingEnabled(true);
        picasso = builder.build();
        OkHttpClient okHttpClient = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {

                Request original = chain.request();

                // Customize the request
                Request request = original.newBuilder()
                        .header("makemoji-sdkkey", "940ced93abf2ca4175a4a865b38f1009d8848a58")
                        .header("makemoji-deviceId", "uniqueidiandsnasdfad")
                        .method(original.method(), original.body())
                        .build();

                Response response = chain.proceed(request);
                return response;
            }
        }).build();

        Retrofit retrofit = new Retrofit.Builder().baseUrl(MojiApi.BASE_URL).client(okHttpClient).
                addConverterFactory(GsonConverterFactory.create()).build();
        mojiApi = retrofit.create(MojiApi.class);
        mojiApi.getTrending().enqueue(new SmallCB<List<MojiModel>>() {
            @Override
            public void done(retrofit2.Response<List<MojiModel>> response, @Nullable Throwable t) {
               if (t==null){
                   List<MojiModel> mojiModels = response.body();
               }
            }
        });
    }
    //calls initialize with the default cache size, 5%
    public static void initialize(Application app){
        initialize(app,calculateMemoryCacheSize(app));
    }

    /**
     * Parses an html message and converts it into a spanned, using the TextView to size the emoji spans properly.
     * Optionally applies other style attributes from html such as color, margin, and text size.
     * @param html the html message to parse
     * @param tv the TextView to set the text on. Used for sizing the emoji spans.
     * @param simple If true, will not apply any styling information beyond setting the parsed message with emojis.
     * @return Returns the parsed attributes from the html so you can cherry pick which styles to apply.
     */
    @UiThread
    public static ParsedAttributes setText(@NonNull String html, @NonNull TextView tv, boolean simple){
        ParsedAttributes parsedAttributes =parseHtml(html,tv,simple);
        setText(parsedAttributes.spanned,tv);
        if (!simple){
            tv.setPadding((int)(parsedAttributes.marginLeft *density),(int)(parsedAttributes.marginTop *density),
                    (int) (parsedAttributes.marginRight * density),(int)(parsedAttributes.marginBottom *density));
            if (parsedAttributes.color!=-1)tv.setTextColor(parsedAttributes.color);
            if (parsedAttributes.fontSizePt!=-1) tv.setTextSize(TypedValue.COMPLEX_UNIT_SP,parsedAttributes.fontSizePt);
        }
        return parsedAttributes;
    }

    /**
     * Set the spanned into the textview, subscribing and unsubscribing from animation as appropriate. Use this if you cache the spanned
     * after calling #parseHtml
     * @param spanned The spanned produced from #parseHtml
     * @param tv The textview to change.
     */
    @UiThread
    public static void setText(Spanned spanned, TextView tv){
        CharSequence cs = tv.getText();
        if (cs instanceof Spanned)
            unsubSpanimatable((Spanned)cs);
        tv.setText(spanned);
        subSpanimatable(spanned,tv);

    }

    /**
     * Call this to unsubscribe the spanned created from #setText from animation. Only need to call this yourself if you
     * are going to be calling TextView.setText manually.
     * @param spanned All moji spans in spanned will be unsubscribed
     */
    public static void unsubSpanimatable(Spanned spanned){
        MojiSpan[] mojiSpans = spanned.getSpans(0, spanned.length(), MojiSpan.class);
        for (MojiSpan mojiSpan : mojiSpans) {
                Spanimator.unsubscribe(Spanimator.HYPER_PULSE, mojiSpan);
        }
    }
    /**
     * Call this to subscribe the spanned created from #setText to animate. Only need to call this yourself if you
     * are going to be calling TextView.setText  manually.
     * @param spanned All moji spans in spanned will be subscribed to @Spanimator
     */
    public static void subSpanimatable(Spanned spanned, TextView tv){
        MojiSpan[] mojiSpans = spanned.getSpans(0, spanned.length(), MojiSpan.class);
        for (MojiSpan mojiSpan : mojiSpans) {
                Spanimator.subscribe(Spanimator.HYPER_PULSE, mojiSpan);
                mojiSpan.setTextView(tv);
        }
    }

    /**
     * Parse the html message without side effect. Returns the spanned and attributes.
     * @param html the html message to parse
     * @param tv An optional textview to size the emoji spans.
     * @return An @ParsedAttributes object containing the spanned and style attributes.
     */
    @CheckResult
    public static ParsedAttributes parseHtml(@NonNull String html, @Nullable TextView tv, boolean simple){
        return new SpanBuilder(html,null,null,getParser(),simple,tv).convert();
    }

    //gets the parser for the current thread.
    private static Parser getParser(){
       SoftReference<Parser> softReference = parsers.get(Thread.currentThread().getId());

        Parser parser;
        if (softReference!=null && softReference.get()!=null)return softReference.get();

        parser= new Parser();
        try {
            parser.setProperty(Parser.schemaProperty, new HTMLSchema());
        }catch (Exception e){}
        parsers.put(Thread.currentThread().getId(),new SoftReference<>(parser));
        return parser;
    }
    private static  HyperMojiListener defaultHyperMojiListener = new HyperMojiListener() {
        @Override
        public void onClick(String url) {
            Toast.makeText(context,"default hypermoji click "+ url,Toast.LENGTH_SHORT).show();
        }
    };
    private static HyperMojiListener customDefaultHyperMojiListener;
    static HyperMojiListener getDefaultHyperMojiClickBehavior(){
        return customDefaultHyperMojiListener==null?defaultHyperMojiListener:customDefaultHyperMojiListener;
    }

    /**
     * When a hypermoji is clicked but no OnClickListener has been set with
     * setTag(R.id._makemoji_hypermoji_listener_tag_id,HyperMojiListener), the default HyperMojiListener will be called.
     * @param hyperMojiListener
     */
    public static void setDefaultHyperMojiListener(HyperMojiListener hyperMojiListener ){
        customDefaultHyperMojiListener = hyperMojiListener;
    }

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
    protected static void loadImage(ImageView iv, String url){
        if (url==null || url.isEmpty())return;
        if (url.equals(iv.getTag()))return;
        Picasso.with(Moji.context).load(url).into(iv);
        iv.setTag(url);
    }
     static Activity getActivity(Context context) {
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity)context;
            }
            context = ((ContextWrapper)context).getBaseContext();
        }
        return null;
    }
    public static String toHtml(Spanned spanned){
     StringBuilder sb = new StringBuilder();
        sb.append("<p dir=\"auto\" style=\"margin-bottom:16px;font-family:'.Helvetica Neue Interface';font-size:16px;\"><span style=\"color:#000000;\">");
        int next =0;
        int end = spanned.length();
        for (int i = 0; i < end; i = next) {
            next = spanned.nextSpanTransition(i, end, CharacterStyle.class);
            MojiSpan[] style = spanned.getSpans(i, next,
                    MojiSpan.class);
            for (int j = 0; j < style.length; j++) {
                sb.append(style[j].toHtml());
            }
            withinStyle(sb,spanned,i,next);
        }
        return sb.toString();
    }

    private static void withinStyle(StringBuilder out, CharSequence text,
                                    int start, int end) {
        for (int i = start; i < end; i++) {
            char c = text.charAt(i);

            if (c == '<') {
                out.append("&lt;");
            } else if (c == '>') {
                out.append("&gt;");
            } else if (c == '&') {
                out.append("&amp;");
            }
            else if(c==0xFFFC){
                //do nothing
            } else if (c >= 0xD800 && c <= 0xDFFF) {
                if (c < 0xDC00 && i + 1 < end) {
                    char d = text.charAt(i + 1);
                    if (d >= 0xDC00 && d <= 0xDFFF) {
                        i++;
                        int codepoint = 0x010000 | (int) c - 0xD800 << 10 | (int) d - 0xDC00;
                        out.append("&#").append(codepoint).append(";");
                    }
                }
            } else if (c > 0x7E || c < ' ') {
                out.append("&#").append((int) c).append(";");
            } else if (c == ' ') {
                while (i + 1 < end && text.charAt(i + 1) == ' ') {
                    out.append("&nbsp;");
                    i++;
                }

                out.append(' ');
            } else {
                out.append(c);
            }
        }
    }
    static void invalidateTextView(TextView tv){
        if (tv ==null)return;

        long lastInvalidated = tv.getTag(R.id._makemoji_last_invalidated_id)==null?0:
                (long)tv.getTag(R.id._makemoji_last_invalidated_id);
        Long now = System.currentTimeMillis();
        if (lastInvalidated+15>now) return;

        tv.invalidate();
        if (tv instanceof EditText)tv.requestLayout();
        tv.setTag(R.id._makemoji_last_invalidated_id,now);

    }

}
