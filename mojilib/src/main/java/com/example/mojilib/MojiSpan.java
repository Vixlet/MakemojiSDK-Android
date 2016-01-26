package com.example.mojilib;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.style.ReplacementSpan;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import com.example.mojilib.model.MojiModel;
import com.squareup.picasso252.Picasso;
import com.squareup.picasso252.Target;

import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

/**
 * Fork of ImageSpan for custom emojis.
 * Created by Scott Baar on 12/3/2015.
 */
class MojiSpan extends ReplacementSpan implements Spanimatable {

    private Drawable mDrawable;
    private Uri mContentUri;
    private int mResourceId;
    private Context mContext;
    private String mSource;
    private int mWidth;
    private int mHeight;
    private int mFontSize;

    // the baseline "normal" font size in sp.
    static final int BASE_TEXT_PT = 14;

    //most incoming messages will be at this size. Use it to calculate default span dimension
    public static int DEFAULT_INCOMING_FONT_PT = 16;

    //most incoming images will be at this size in px. Use it to calculate default span dimension
    public static int DEFAULT_INCOMING_IMG_WH = 20;

    //the text size in pixels, determined by BASE_TEXT_PT and screen density
    static float BASE_TEXT_PX_SCALED;
    private float mFontRatio;

    // to make mojis stand out from text, always multiply the size by this
    private static float BASE_SIZE_MULT = 1.35f;

    //proportion to size the moji on next frame when being animated;
    private float currentAnimationScale = 1f;


    private SoftReference<Drawable> mDrawableRef;
    private WeakReference<TextView> mViewRef;
    private String mLink;
    boolean shouldAnimate;
    Drawable mPlaceHolder;
    private static final String TAG = "MojiSpan";
    private static boolean LOG = true;
    String name;
    int id = -1;


    /**
     *
     * @param d The placeholder drawable.
     * @param source URL of the actual emoji
     * @param w width
     * @param h height
     * @param fontSize pt size of parsed attributes
     * @param simple if true, scale based on fontSize, otherwise refreshView's size
     * @param link URL to callback when clicked.
     * @param refreshView view to size against and invalidate after image load.
     */
    public MojiSpan(@NonNull Drawable d, String source, int w, int h, int fontSize, boolean simple, String link, TextView refreshView) {
        //scale based on font size
        if (simple){ //scale based on current text size
            mFontRatio = refreshView.getTextSize()/BASE_TEXT_PX_SCALED;
        }
        else{//scale based on font size to be set
            mFontRatio = (fontSize*Moji.density)/BASE_TEXT_PX_SCALED;
        }
        mWidth = (int) (w * Moji.density *BASE_SIZE_MULT * mFontRatio);
        mHeight = (int) (h * Moji.density * BASE_SIZE_MULT * mFontRatio);

        mDrawable = d;
        mPlaceHolder = d;
        mSource = source;
        mLink = link;
        shouldAnimate = (link!=null && !link.isEmpty());

        mViewRef = new WeakReference<>(refreshView);
        Moji.picasso.load(mSource)
                .resize(mWidth,mHeight)
                .into(t);
        if (Moji.demo &&  Math.random() <= .2) {
                shouldAnimate =true;
                mLink = source;
        }
    }

    public static int getDefaultSpanDimension(float textSize){
        float ratio = (textSize)/BASE_TEXT_PX_SCALED;
        return (int) (DEFAULT_INCOMING_IMG_WH * Moji.density * BASE_SIZE_MULT * ratio);
    }
    public static MojiSpan fromModel(MojiModel model, TextView tv, @Nullable BitmapDrawable bitmapDrawable){
        Drawable d = bitmapDrawable!=null? bitmapDrawable: Moji.resources.getDrawable(R.drawable.mm_dotted_square);
        d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
        MojiSpan span = new MojiSpan(d,model.image_url,20,20,14,true,model.link_url,tv);
        span.name = model.name;
        span.id = model.id;
        return span;
    }

    Target t = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            if (LOG)Log.d(TAG,"loaded "+name);
            mDrawable = new BitmapDrawable(Moji.resources,bitmap);
            mDrawable.setBounds(0,0,mWidth,mHeight);
            mDrawableRef = new SoftReference<>(mDrawable);
            TextView tv = mViewRef.get();
            Moji.invalidateTextView(tv);
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };



    public Drawable getDrawable() {
        Drawable drawable = null;

        if (mDrawable != null) {
            drawable = mDrawable;
        } else  if (mContentUri != null) {
            Bitmap bitmap = null;
            try {
                InputStream is = mContext.getContentResolver().openInputStream(
                        mContentUri);
                bitmap = BitmapFactory.decodeStream(is);
                drawable = new BitmapDrawable(mContext.getResources(), bitmap);
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                        drawable.getIntrinsicHeight());
                is.close();
            } catch (Exception e) {
                Log.e("sms", "Failed to loaded content " + mContentUri, e);
            }
        } else {
            try {
                drawable = mContext.getResources().getDrawable(mResourceId);
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                        drawable.getIntrinsicHeight());
            } catch (Exception e) {
                Log.e("sms", "Unable to find resource: " + mResourceId);
            }
        }

        return drawable;
    }

    /**
     * Returns the source string that was saved during construction.
     */
    public String getSource() {
        return mSource;
    }



    /**
     * A constant indicating that the bottom of this span should be aligned
     * with the bottom of the surrounding text, i.e., at the same level as the
     * lowest descender in the text.
     */
    public static final int ALIGN_BOTTOM = 0;

    /**
     * A constant indicating that the bottom of this span should be aligned
     * with the baseline of the surrounding text.
     */
    public static final int ALIGN_BASELINE = 1;

    protected final int mVerticalAlignment = ALIGN_BOTTOM;

    public int getVerticalAlignment() {
        return mVerticalAlignment;
    }

    /**
     * Your subclass must implement this method to provide the bitmap
     * to be drawn.  The dimensions of the bitmap must be the same
     * from each call to the next.
     */

    @Override
    public int getSize(Paint paint, CharSequence text,
                       int start, int end,
                       Paint.FontMetricsInt fm) {
        Drawable d = getCachedDrawable();
        Rect rect = d.getBounds();
        rect.bottom = mHeight;
        rect.right = mWidth;
        //rect.bottom=100;

        if (fm != null) {
            fm.ascent = -rect.bottom;
            fm.descent = 0;

            fm.top = fm.ascent;
            fm.bottom = 0;
        }

        //return 100;
        return rect.right;
    }

    @Override
    public void draw(Canvas canvas, CharSequence text,
                     int start, int end, float x,
                     int top, int y, int bottom, Paint paint) {
        if (LOG)Log.d(TAG,"draw "+name);
        Drawable d = getCachedDrawable();
        canvas.save();
        //save bounds before applying animation scale. for a size pulse only
        //int oldRight = d.getBounds().right;
        //int oldBottom = d.getBounds().bottom;
       // int newWidth = (int)(oldRight*currentAnimationScale);
        //d.setBounds(d.getBounds().left,d.getBounds().top,newWidth,(int)(oldBottom*currentAnimationScale));
        int transY = bottom - d.getBounds().bottom;
        if (mVerticalAlignment == ALIGN_BASELINE) {
            transY -= paint.getFontMetricsInt().descent;
        }

        d.setAlpha((int)(255 * currentAnimationScale));
        canvas.translate(x, transY);
        d.draw(canvas);
       // d.setBounds(d.getBounds().left,d.getBounds().top,oldRight,oldBottom);
        canvas.restore();
    }

    private Drawable getCachedDrawable() {
        SoftReference<Drawable> wr = mDrawableRef;
        Drawable d = null;

        if (wr != null)
            d = wr.get();

        if (d == null) {
            d = mPlaceHolder;
        }

        return d;
    }

    public boolean shouldAnimate(){
        return shouldAnimate;

    }
    public String getLink(){
        return mLink;
    }
    @Override
    public void onAnimationUpdate(@Spanimator.Spanimation int spanimation, float progress, float min, float max) {
        if (shouldAnimate) {
            currentAnimationScale = progress;
            TextView tv = mViewRef.get();
            if (tv != null) {
                Moji.invalidateTextView(tv);
            }

            else
                Spanimator.unsubscribe(Spanimator.HYPER_PULSE, this);//no longer attatched to view.
        }
    }

    @Override
    public void onAnimationPause() {

    }
    @Override
    public void onUnsubscribed(){
        mDrawable = null;//get rid of hard reference to bitmap
    }
    public void onSubscribed(){
        if (mDrawableRef!=null && mDrawableRef.get()==null) //if bitmap was gced, get it again. don't bother refetching for a new size.
            Moji.picasso.load(mSource)
                    //.resize(mWidth,mHeight)
                    .into(t);
    }
    public void setTextView(TextView tv){
        mViewRef = new WeakReference<>(tv);
    }

    public String toHtml() {
        return "<img style=\\\"vertical-align:text-bottom;width:20px;height:20px;\""
                + "src=\"" + mSource + "\"" +
                (id == -1 ? "" : "id=\"" + id + "\"")//insert id if this came from a model
                + ">";
    }
}
