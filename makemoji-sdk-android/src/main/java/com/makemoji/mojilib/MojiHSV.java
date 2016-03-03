package com.makemoji.mojilib;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

/**
 * Horizontal scroll view witch custom touch intercept behavior to pass onto children
 * Created by Scott Baar on 1/6/2016.
 */
public class MojiHSV extends HorizontalScrollView{
    IAllowTouchIntercept mAllowTouchInterceptCalculator;
    public MojiHSV(Context context) {
        super(context);
    }

    public MojiHSV(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MojiHSV(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public interface IAllowTouchIntercept{
        boolean calculateAllow();
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mAllowTouchInterceptCalculator!=null && !mAllowTouchInterceptCalculator.calculateAllow()) return false;
        return super.onInterceptTouchEvent(ev);
    }
    public void setAllowTouchInterceptCalculator(IAllowTouchIntercept interceptCalculator){
        mAllowTouchInterceptCalculator=interceptCalculator;
    }
}
