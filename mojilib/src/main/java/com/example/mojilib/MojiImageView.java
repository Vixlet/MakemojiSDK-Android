package com.example.mojilib;

import android.content.Context;
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

   private int forceDimen = -1;
    public void forceDimen(int dimen){
        forceDimen = dimen;

    }

    public void setModel(MojiModel m){
        model = m;
        if (forceDimen!=-1)
            Picasso.with(getContext()).load(m.image_url).resize(forceDimen,forceDimen).into(this);
        else
            Picasso.with(Moji.context).load(model.image_url).fit().centerInside().into(this);

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
