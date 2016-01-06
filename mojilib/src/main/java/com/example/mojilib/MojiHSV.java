package com.example.mojilib;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

/**
 * Horizontal scroll view witch custom touch intercept behavior to pass onto children
 * Created by Scott Baar on 1/6/2016.
 */
public class MojiHSV extends HorizontalScrollView{
    public MojiHSV(Context context) {
        super(context);
    }

    public MojiHSV(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MojiHSV(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
        //return super.onInterceptTouchEvent(ev);
    }
}
