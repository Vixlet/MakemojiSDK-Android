package com.makemoji.mojilib;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.makemoji.mojilib.model.MojiModel;

/**
 * Don't use this outsid
 * Created by Scott Baar on 1/24/2016.
 */
class MojiImageView extends ImageView  implements Spanimatable{
    MojiModel model;
    float currentAnimationScale =1f;
    boolean animate;
    public MojiImageView(Context context) {
        super(context);
    }

    public MojiImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MojiImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

   private int forceDimen = -1;
    public void forceDimen(int dimen){
        forceDimen = dimen;

    }
//http://stackoverflow.com/questions/12166476/android-canvas-drawtext-set-font-size-from-width
    Bitmap makeBMFromString(int dimen,String s){
        Paint paint = new Paint();
        int adJusteddimen=dimen - 2*(getPaddingTop()+getPaddingBottom());
        //paint.setStyle(Paint.Style.FILL);
       // paint.setTextAlign(Paint.Align.LEFT);
        Bitmap image = Bitmap.createBitmap(dimen,dimen, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);

        final float testTextSize = 250f;

        // Get the bounds of the text, using our testTextSize.
        paint.setTextSize(testTextSize);
        Rect bounds = new Rect();
        paint.getTextBounds(s, 0, s.length(), bounds);

        // Calculate the desired size as a proportion of our testTextSize.
        float desiredTextSize = testTextSize * adJusteddimen / Math.max(bounds.height(),bounds.width());
        // Set the paint for that size.
        //paint.setTextSize(Math.min(desiredTextSize,testTextSize*2f));
        paint.setTextSize(desiredTextSize);
        paint.getTextBounds(s, 0, s.length(), bounds);

        //Log.d(VIEW_LOG_TAG,model.name + " " + model.character + " " +bounds.top + " "+ bounds.bottom + " "+ paint.getTextSize());
        canvas.drawText(s,-bounds.left, -bounds.top +getPaddingBottom(),paint);
        return image;
    }
    public void setModel(MojiModel m){
        if (m.equals(model))return;
        model = m;
        Moji.picasso.cancelRequest(this);
        setContentDescription(""+model.name);
        int size = MojiSpan.getDefaultSpanDimension(MojiSpan.BASE_TEXT_PX_SCALED);
        Drawable d = getResources().getDrawable(R.drawable.mm_placeholder);
            if (!model.image_url.isEmpty()) {
                    Moji.picasso.load(m.image_url)
                            .resize(size, size)
                            .placeholder(d).into(this);
            } else {
                setImageDrawable(new BitmapDrawable(makeBMFromString(forceDimen, m.character)));
                //setScaleType(ScaleType.CENTER_INSIDE);

            }
        if ((m.link_url==null || m.link_url.isEmpty())){
            Spanimator.unsubscribe(Spanimator.HYPER_PULSE,this);
            animate =false;
            setAlpha(255);
        }
        else {
            animate = true;
            Spanimator.subscribe(Spanimator.HYPER_PULSE,this);
            setAlpha((int)(255*Spanimator.getValue(Spanimator.HYPER_PULSE)));
        }

    }
    @Override
    public void onAnimationUpdate(@Spanimator.Spanimation int spanimation, float progress, float min, float max) {
        if (animate) setAlpha((int)(255*progress));

    }

    @Override
    public void onAnimationPause() {

    }

    @Override
    public void onSubscribed() {

    }

    @Override
    public void onUnsubscribed() {
        currentAnimationScale = 1f;

    }
    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        if (forceDimen==-1){
            super.onMeasure(widthMeasureSpec,heightMeasureSpec);
            return;
        }
        setMeasuredDimension(forceDimen,forceDimen);

    }
}
