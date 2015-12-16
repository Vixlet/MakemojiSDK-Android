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
import android.text.style.DynamicDrawableSpan;
import android.text.style.ReplacementSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.squareup.picasso252.Picasso;
import com.squareup.picasso252.Target;

import java.io.InputStream;
import java.lang.ref.WeakReference;

/**
 * Created by Scott Baar on 12/3/2015.
 */
class MojiSpan extends ReplacementSpan {

    private Drawable mDrawable;
    private Uri mContentUri;
    private int mResourceId;
    private Context mContext;
    private String mSource;
    private TextView mRefreshView;
    private int mWidth;
    private int mHeight;
    private int mFontSize;

    // the baseline "normal" font size in sp.
    static final int BASE_TEXT_PT = 14;
    //the text size in pixels, determined by BASE_TEXT_PT and screen density
    static float BASE_TEXT_PX_SCALED;
    private float mFontRatio;

    // to make mojis stand out from text, always multiply the size by this
    private static float BASE_SIZE_MULT = 1.25f;


    public MojiSpan(Drawable d, String source, int w, int h,int fontSize, boolean simple, TextView refreshView) {
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
        mSource = source;

        mRefreshView=refreshView;
        Moji.picasso.load(mSource)
                //.resize(mWidth,mHeight)
                .into(t);
    }

    /**
     * @param verticalAlignment one of {@link DynamicDrawableSpan#ALIGN_BOTTOM} or
     * {@link DynamicDrawableSpan#ALIGN_BASELINE}.
     */
    Target t = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            mDrawable = new BitmapDrawable(Moji.resources,bitmap);
            mDrawable.setBounds(0,0,mWidth,mHeight);
            mDrawableRef = new WeakReference<>(mDrawable);
            mRefreshView.postInvalidate();
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
        Drawable b = getCachedDrawable();
        canvas.save();
        int transY = bottom - b.getBounds().bottom;
        if (mVerticalAlignment == ALIGN_BASELINE) {
            transY -= paint.getFontMetricsInt().descent;
        }

        //b.setBounds(0,0,-mWidth,mHeight);
        canvas.translate(x, transY);
        b.draw(canvas);
        canvas.restore();
    }

    private Drawable getCachedDrawable() {
        WeakReference<Drawable> wr = mDrawableRef;
        Drawable d = null;

        if (wr != null)
            d = wr.get();

        if (d == null) {
            d = getDrawable();
            mDrawableRef = new WeakReference<Drawable>(d);
        }

        return d;
    }

    private WeakReference<Drawable> mDrawableRef;
}
