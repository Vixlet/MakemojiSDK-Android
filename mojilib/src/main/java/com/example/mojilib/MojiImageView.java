package com.example.mojilib;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.example.mojilib.model.MojiModel;
import com.squareup.picasso252.Picasso;

/**
 * Created by Scott Baar on 1/24/2016.
 */
public class MojiImageView extends ImageView  implements Spanimatable{
    MojiModel model;
    float currentAnimationScale =1f;
    public MojiImageView(Context context) {
        super(context);
    }

    public MojiImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MojiImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    private float textSize;
    void setTextSize(float size){
        textSize=size;
    }

   private int forceDimen = -1;
    public void forceDimen(int dimen){
        forceDimen = dimen;

    }

    Bitmap makeBMFromString(int dimen,String s){
        Paint paint = new Paint();
        paint.setTextSize(textSize);
        Bitmap image = Bitmap.createBitmap(dimen,dimen, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);
        canvas.drawText(s,0,-paint.ascent(),paint);
        return image;
    }
    public void setModel(MojiModel m){
        model = m;
        Drawable d = getResources().getDrawable(R.drawable.mm_dotted_square);
        if (!model.image_url.isEmpty()) {
            if (forceDimen != -1)
                Picasso.with(getContext()).load(m.image_url).resize(forceDimen, forceDimen).placeholder(d).into(this);
            else
                Picasso.with(Moji.context).load(model.image_url).fit().centerInside().placeholder(d).into(this);
        }
        else {
            setImageBitmap(makeBMFromString(forceDimen,m.character));

        }
        if (m.link_url==null || m.link_url.isEmpty()){
            Spanimator.unsubscribe(Spanimator.HYPER_PULSE,this);
            setAlpha(1f);
        }
        else {
            Spanimator.subscribe(Spanimator.HYPER_PULSE,this);
        }

    }
    @Override
    public void onAnimationUpdate(@Spanimator.Spanimation int spanimation, float progress, float min, float max) {
        setAlpha((int)(255*progress));

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
}
