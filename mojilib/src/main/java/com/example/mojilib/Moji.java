package com.example.mojilib;

import android.content.Context;
import android.content.res.Resources;
import android.text.Spanned;
import android.widget.TextView;

import com.squareup.picasso252.Picasso;

import org.ccil.cowan.tagsoup2.HTMLSchema;
import org.ccil.cowan.tagsoup2.Parser;

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
    static Parser parser;

    static float density;

    /**
     * Required to set in Application class so that the library can load resources.
     * @param c A context from your app. Moji will use the application context.
     */
    public static void setContext(Context c){
        context = c.getApplicationContext();
        resources = c.getResources();
        density = resources.getDisplayMetrics().density;
        Picasso.Builder builder = new Picasso.Builder(context);
        picasso = builder.build();
        parser = new Parser();
        try {
            parser.setProperty(Parser.schemaProperty, new HTMLSchema());
        }catch (Exception e){}
    }
    public static void setText(String html, TextView tv){
        Spanned spanned =getSpanned(html,tv);
        tv.setText(spanned);
    }
    public static Spanned getSpanned(String html, TextView tv){
        return  new SpanBuilder(html,null,null,parser,tv).convert();
    }
}
