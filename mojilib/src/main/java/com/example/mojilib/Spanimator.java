package com.example.mojilib;

import android.animation.ValueAnimator;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.util.Log;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Coordinates and syncs the animation of MojiSpans. Implement Spanimatable to listen and sync your own animations
 * Currently only supports animation #HYPER_PULSE
 * Created by Scott Baar on 12/18/2015.
 */
public class Spanimator {


    public static final int HYPER_PULSE = 0;
    public static final float HYPER_PULSE_MAX = 1f;
    public static final float HYPER_PULSE_MIN = .25f;


    private static Map<Spanimatable,Boolean> subscribers = Collections.synchronizedMap(new WeakHashMap<Spanimatable,Boolean>());
    private static ValueAnimator hyperAnimation;
    private static Handler mainHandler = new Handler(Looper.getMainLooper());


    @Retention(RetentionPolicy.SOURCE)
    @IntDef({HYPER_PULSE})
    public @interface Spanimation {}

    public static void subscribe(@Spanimation int spanimation, Spanimatable spanimatable){
        subscribers.put(spanimatable,true);
        setupStartAnimation(spanimation);
    }
    public static void unsubscrube(@Spanimation int spanimation, Spanimatable spanimatable ){
        subscribers.remove(spanimatable);
        if (subscribers.isEmpty() && hyperAnimation!=null){
            hyperAnimation.end();
            hyperAnimation = null;
        }
    }
    private static void setupStartAnimation(@Spanimation int spanimation){
        if (hyperAnimation!=null) {
            if (!hyperAnimation.isRunning())hyperAnimation.start();
            return;
        }
        int duration = Moji.resources.getInteger(R.integer._makemoji_pulse_duration);
        Interpolator interpolator = new DecelerateInterpolator();
        hyperAnimation = ValueAnimator.ofFloat(HYPER_PULSE_MAX,HYPER_PULSE_MIN).setDuration(duration);
        hyperAnimation.setInterpolator(interpolator);
        hyperAnimation.setRepeatCount(ValueAnimator.INFINITE);
        hyperAnimation.setRepeatMode(ValueAnimator.REVERSE);
        hyperAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Set<Spanimatable> set = subscribers.keySet();
                Log.d("Spanimator","spanimator subscruber size "+ set.size());
                float progress = (float) animation.getAnimatedValue();
                for (Spanimatable spanimatable : set){
                    if (spanimatable!=null){
                        spanimatable.onAnimationUpdate(HYPER_PULSE,progress,HYPER_PULSE_MIN,HYPER_PULSE_MAX);
                    }
                }
            }
        });
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
            if (hyperAnimation!=null)    hyperAnimation.start();
            }
        });


    }
    static void onResume(){
        Log.d("Spanimator","spanimator lifecycle resume");
        if (hyperAnimation!=null && !hyperAnimation.isRunning())hyperAnimation.start();
    }
    static void onPause(){
        Log.d("Spanimator","spanimator lifecycle pause");
        if (hyperAnimation!=null)hyperAnimation.end();

    }
}
