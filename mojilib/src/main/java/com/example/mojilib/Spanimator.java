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
    public static final float HYPER_PULSE_MIN = .15f;


    private static Map<Spanimatable,Boolean> subscribers = new WeakHashMap<>();
    private static ValueAnimator hyperAnimation;
    private static Handler mainHandler = new Handler(Looper.getMainLooper());
    private static boolean mPaused =false;


    @Retention(RetentionPolicy.SOURCE)
    @IntDef({HYPER_PULSE})
    public @interface Spanimation {}

    /**
     * Adds a spanimatable to the list of subscribers to be updated on each animation frame
     * @param spanimation the animation to subscribe to
     * @param spanimatable the subscriber to add.
     */
    public static synchronized void subscribe(@Spanimation int spanimation, Spanimatable spanimatable){
        subscribers.put(spanimatable,true);
        spanimatable.onSubscribed();
        setupStartAnimation(spanimation);
    }

    /**
     * Removes a spanimatable from the list of subscribers to be updated on each animation frame
     * @param spanimation
     * @param spanimatable
     */
    public static synchronized void unsubscribe(@Spanimation int spanimation, Spanimatable spanimatable ){
        subscribers.remove(spanimatable);
        spanimatable.onUnsubscribed();
        if (subscribers.isEmpty() && hyperAnimation!=null){
            hyperAnimation.end();
        }
    }
    private static synchronized void setupStartAnimation(@Spanimation int spanimation){
        if (mPaused)return;
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
                float progress = (float) animation.getAnimatedValue();
                Set<Spanimatable> set = subscribers.keySet();
                Log.d("Spanimator","spanimator subscruber size "+ set.size());
                if (set.size()==0 && animation.getAnimatedFraction()!=0f)
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            hyperAnimation.end();
                        }
                    });
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
            if (hyperAnimation!=null && !mPaused)    hyperAnimation.start();
            }
        });


    }
    static void onResume(){
        mPaused=false;
        Log.d("Spanimator","spanimator lifecycle resume");
        if (hyperAnimation!=null && !hyperAnimation.isRunning())hyperAnimation.start();
    }
    static void onPause(){
        mPaused=true;
        Log.d("Spanimator","spanimator lifecycle pause");
        if (hyperAnimation!=null)hyperAnimation.end();
        System.gc();

    }
}
