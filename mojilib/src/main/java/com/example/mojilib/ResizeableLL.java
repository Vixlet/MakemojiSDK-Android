package com.example.mojilib;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.os.SystemClock;
import android.support.annotation.IntDef;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Scott Baar on 1/19/2016.
 */
public class ResizeableLL  extends LinearLayout implements View.OnTouchListener{
    public ResizeableLL(Context context) {
        this(context, null);
    }

    public ResizeableLL(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }




    View leftView;
    SnappyRecyclerView recyclerView;
    int minSize, maxSize;
    int expandSizeThreshold =(int) (100 * Moji.density);//bigger than this  after drag -> expand
    boolean lastStateOpened = false; //affinity for snap behavior open or close

    int mTouchSlop;
    @Override
    public void onFinishInflate(){
        super.onFinishInflate();
        leftView = findViewById(R.id._mm_left_buttons);
        recyclerView = (SnappyRecyclerView) findViewById(R.id._mm_recylcer_view);
        //leftView.setOnTouchListener(this);
        setOnTouchListener(this);

        mLastPrimaryContentSize = leftView.getMeasuredHeight();
        minSize = (int)(50 *Moji.density);
        maxSize = getMeasuredWidth();

        ViewConfiguration vc = ViewConfiguration.get(getContext());
        mTouchSlop = vc.getScaledTouchSlop();

    }

    private boolean mDragging;
    private long mDraggingStarted;
    private float mDragStartX;
    private float mDragStartY;
    private float mPointerOffset;

    private int mLastPrimaryContentSize;
    boolean isClosed,isOpened;

    final static private int MAXIMIZED_VIEW_TOLERANCE_DIP = 30;
    final static private int TAP_DRIFT_TOLERANCE = 3;
    final static private int SINGLE_TAP_MAX_TIME = 175;

    float mDownX = 0;
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    /*@Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);
        // Always handle the case of the touch gesture being complete.
        if (action == MotionEvent.ACTION_DOWN){
            mDownX = ev.getRawX();
            return false;
        }
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            // Release the scroll.
            mDragging = false;
            return false; // Do not intercept touch event, let the child handle it
        }
        if (action == MotionEvent.ACTION_MOVE) {
            if (mDragging) {
                // We're currently scrolling, so yes, intercept the
                // touch event!
                return true;
            }

            // If the user has dragged her finger horizontally more than
            // the touch slop, start the scroll

            final int xDiff = (int) Math.abs(ev.getRawX() - mDownX);

            Rect rvBounds = new Rect(recyclerView.getLeft(),recyclerView.getTop(),recyclerView.getRight(),recyclerView.getBottom());
            if ((xDiff > mTouchSlop)
                    && rvBounds.contains(recyclerView.getLeft()+(int)ev.getX(),recyclerView.getTop() +(int) ev.getX())
                    && recyclerView.canScrollHorizontally(1) ) {
                Log.d("asf","disable");
                return false;
            }

            if (xDiff > mTouchSlop) {
                // Start scrolling!
                mDragging = true;
                return true;
            }
        }
            return false;
    }*/
    int mStartWidth;
    @Override
    public boolean onTouch(View view, MotionEvent me) {
        maxSize = getMeasuredWidth();
        if (me.getAction() == MotionEvent.ACTION_DOWN) {
            mDraggingStarted = SystemClock.elapsedRealtime();
            mDragStartX = me.getRawX();
            mDragStartY = me.getY();
            mStartWidth = leftView.getMeasuredWidth();
                mPointerOffset = me.getRawX() - leftView.getMeasuredWidth();
            mDragging=true;
            return true;
        }
        else if (me.getAction() == MotionEvent.ACTION_UP && mDragging) {
            mDragging = false;
                snapOpenOrClose((int)(me.getRawX() - mPointerOffset));
                return true;

        }
        else if (me.getAction() == MotionEvent.ACTION_MOVE && mDragging) {
            int nw = mStartWidth - (int)(mDragStartX - me.getRawX());
            setPrimaryContentWidth(nw);
               // setPrimaryContentWidth((int) (me.getRawX() - mPointerOffset));
            }
        else if (me.getAction() == MotionEvent.ACTION_CANCEL){
            mDragging=false;
            snapOpenOrClose((int)(me.getRawX() - mPointerOffset));
            return false;
        }


        return false;
    }

    public boolean isPrimaryContentMaximized(){
        return false;
    }
    private boolean setPrimaryContentWidth(int newWidth) {
        newWidth=  Math.max(minSize,newWidth);
        newWidth = Math.min(maxSize,newWidth);// clamp >--(◣_◢) --<
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) leftView.getLayoutParams();
        lp.width=newWidth;
        leftView.setLayoutParams(lp);

        //change snap affinity when the open or close is done.
        if (maxSize == newWidth)
            lastStateOpened = true;
        if (minSize == newWidth)
            lastStateOpened = false;


        return true;
    }

    ValueAnimator animator;
    private void snapOpenOrClose(int currentWidth){
        maxSize = getMeasuredWidth() - (int)( 50 * Moji.density);
        int goalWidth = maxSize;
        if ((lastStateOpened && maxSize-currentWidth>expandSizeThreshold)//closed enough to snap close
                || (!lastStateOpened && minSize+expandSizeThreshold>currentWidth))//not open enough to snap open.
            goalWidth = minSize;


        animator = ValueAnimator.ofInt(currentWidth,goalWidth);
        animator.setDuration(200);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setPrimaryContentWidth((int)animation.getAnimatedValue());
            }
        });
        animator.start();

    }


}
