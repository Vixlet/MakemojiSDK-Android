package com.example.mojilib;
import android.support.annotation.UiThread;

import com.example.mojilib.Spanimator.Spanimation;

/**
 * Created by Scott Baar on 12/18/2015.
 */
public interface Spanimatable {
    /**
     * New animation frame. Called on ui thread for performance.
     * @param spanimation the animation that was subscribed to
     * @param progress the current value of the animation
     * @param min the min value of the animation
     * @param max the max value of the animation
     */
    void onAnimationUpdate(@Spanimation int spanimation, float progress, float min, float max );
    void onAnimationPause();

    /**
     * called when the spanimatable is subscribed to
     */
    void onSubscribed();

    /**
     * called when the spanimatable is unsubscribed from
     */
    void onUnsubscribed();
}
