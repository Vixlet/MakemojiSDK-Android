package com.example.mojilib;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.widget.TextView;
import android.app.Application;

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
     * Initialize the library. Required to set in {@link Application#onCreate()}  so that the library can load resources.
     * @param c A context from your app. Moji will use the application context.
     */
    public static void initialize(Context c){
        context = c.getApplicationContext();
        resources = c.getResources();
        density = resources.getDisplayMetrics().density;
        MojiSpan.BASE_TEXT_PX_SCALED = MojiSpan.BASE_TEXT_PT*density;
        Picasso.Builder builder = new Picasso.Builder(context);
        picasso = builder.build();
        parser = new Parser();
        try {
            parser.setProperty(Parser.schemaProperty, new HTMLSchema());
        }catch (Exception e){}
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
    public static ParsedAttributes setText(String html, @NonNull TextView tv, boolean simple){
        ParsedAttributes parsedAttributes =parseHtml(html,tv,simple);
        tv.setText(parsedAttributes.spanned);
        if (!simple){
            tv.setPadding((int)(parsedAttributes.marginLeft *density),(int)(parsedAttributes.marginTop *density),
                    (int) (parsedAttributes.marginRight * density),(int)(parsedAttributes.marginBottom *density));
            if (parsedAttributes.color!=-1)tv.setTextColor(parsedAttributes.color);
            if (parsedAttributes.fontSizePt!=-1) tv.setTextSize(TypedValue.COMPLEX_UNIT_SP,parsedAttributes.fontSizePt);
        }
        return parsedAttributes;
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
}
