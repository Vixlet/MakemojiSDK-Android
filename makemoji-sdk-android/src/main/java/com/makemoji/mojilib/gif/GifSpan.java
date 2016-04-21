package com.makemoji.mojilib.gif;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.widget.TextView;

import com.makemoji.mojilib.Moji;
import com.makemoji.mojilib.MojiSpan;

import java.io.IOException;
import java.lang.ref.WeakReference;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by DouglasW on 4/20/2016.
 */
public class GifSpan extends MojiSpan implements GifConsumer {
    GifProducer producer;
    BitmapFactory.Options options;
    Bitmap bitmap;
    BitmapDrawable bitmapDrawable= new BitmapDrawable();
    /**
     * @param d           The placeholder drawable.
     * @param source      URL of the actual emoji
     * @param w           width
     * @param h           height
     * @param fontSize    pt size of parsed attributes
     * @param simple      if true, scale based on fontSize, otherwise refreshView's size
     * @param link        URL to callback when clicked.
     * @param refreshView view to size against and invalidate after image load.
     */
    public GifSpan(@NonNull Drawable d, String source, int w, int h, int fontSize, boolean simple, String link, TextView refreshView) {
        if (simple){ //scale based on current text size
            if (refreshView!=null)  mFontRatio = refreshView.getTextSize()/BASE_TEXT_PX_SCALED;
            else mFontRatio = BASE_TEXT_PX_SCALED;
        }
        else{//scale based on font size to be set
            mFontRatio = (fontSize*Moji.density)/BASE_TEXT_PX_SCALED;
        }
        mWidth = (int) (w * Moji.density *BASE_SIZE_MULT * mFontRatio);
        mHeight = (int) (h * Moji.density * BASE_SIZE_MULT * mFontRatio);

        mDrawable = d;
        mPlaceHolder = d;
        mSource = source;
        if (link!=null)mLink = link;

        mViewRef = new WeakReference<>(refreshView);
        int size = getDefaultSpanDimension(BASE_TEXT_PX_SCALED);
        options = new BitmapFactory.Options();
        load();


    }
    private void load(){
        producer = GifProducer.getProducerAndSub(this,null,mSource);
        if (producer!=null){
            mWidth = producer.getWidth();
            mHeight = producer.getHeight();
            return;
        }
        Moji.okHttpClient.newCall(new Request.Builder().url(mSource).build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                producer = GifProducer.getProducerAndSub(GifSpan.this,response.body().bytes(),mSource);
                mWidth = producer.getWidth();
                mHeight = producer.getHeight();
            }
        });
    }

    @Override
    public void onFrameAvailable(final Bitmap b) {
        final TextView v = mViewRef.get();
        if (v==null){
            Moji.handler.post(new Runnable() {//prevent concurrent modificication
                @Override
                public void run() {
                    if (producer!=null)producer.unsubscribe(GifSpan.this);
                }
            });
            return;
        }

            bitmapDrawable = new BitmapDrawable(Moji.context.getResources(),b);
            bitmapDrawable.setBounds(0,0,b.getWidth(),b.getHeight());
            Moji.handler.post(new Runnable() {
                @Override
                public void run() {
                    Moji.invalidateTextView(v);
                }
            });

    }

    @Override
    public void stopped() {
        producer = null;
    }
    @Override
    public int getSize(Paint paint, CharSequence text,
                       int start, int end,
                       Paint.FontMetricsInt fm) {
        Drawable d = bitmapDrawable;
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
        Drawable d = bitmapDrawable;
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

        canvas.translate(x, transY);
        d.draw(canvas);
        //if (bitmap!=null)canvas.drawBitmap(bitmap,0,0,paint);
        // d.setBounds(d.getBounds().left,d.getBounds().top,oldRight,oldBottom);
        canvas.restore();
    }
}
