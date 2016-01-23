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
import android.view.ViewTreeObserver;
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

        ViewConfiguration vc = ViewConfiguration.get(getContext());
        mTouchSlop = vc.getScaledTouchSlop();
        post(new Runnable() {
            @Override
            public void run() {
                maxSize = getWidth() -(int)(50 *Moji.density);
            }
        });

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
        final int action = MotionEventCompat.getActionMasked(ev);

        // Always handle the case of the touch gesture being complete.
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            // Release the scroll.
            mDragging = false;
            return false; // Do not intercept touch event, let the child handle it
        }
        else if (action==MotionEvent.ACTION_DOWN){
            mDragStartX = ev.getRawX();
            mDragStartY = ev.getY();
            mStartWidth = leftView.getMeasuredWidth();
        }
        else if (action==MotionEvent.ACTION_MOVE){
            if (mDragging) return true;
            final int xDiff = Math.abs((int)(mDragStartX - ev.getRawX()));

            boolean rvhandled = rvHandlesMotion(ev);
            if (xDiff > mTouchSlop && !rvhandled) {
                // Start scrolling!
                if (mDragStartX>ev.getRawX()) mDragStartX-=mTouchSlop;//adjust for slop to remove jank on scroll start
                else if (mDragStartX<ev.getRawX()) mDragStartX+=mTouchSlop;
                mDragging = true;
                return true;
            }
        }
        return false;

    }
    //if true, recycler view will handle the motion.
    boolean canScrollLeft,canScrollRight;
    float newX;
    private boolean rvHandlesMotion(MotionEvent ev){
        newX = ev.getRawX();
        int[] l = new int[2];
        recyclerView.getLocationOnScreen(l);
        int x = l[0];
        int w = recyclerView.getWidth();
        if ((ev.getRawX()< x || ev.getRawX()> x + w ))
            return false;
        canScrollLeft = ((SnappyLinearLayoutManager) recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition()!=0;

        Log.d("asfd","canscroll "+canScrollLeft);
        if (newX<mDragStartX)//drag right
            return true;
        if (newX>mDragStartX && canScrollLeft)
            return true;

        return false;

    }

    int mStartWidth;
    @Override
    public boolean onTouch(View view, MotionEvent me) {
        int nw = mStartWidth - (int)(mDragStartX - me.getRawX());
        if (me.getAction() == MotionEvent.ACTION_DOWN) {
            mDraggingStarted = SystemClock.elapsedRealtime();
            mDragStartX = me.getRawX();
            mDragStartY = me.getY();
            mStartWidth = leftView.getMeasuredWidth();
                mPointerOffset = me.getRawX() - leftView.getMeasuredWidth();
            mDragging=true;
            return true;
        }
        else if ((me.getAction() == MotionEvent.ACTION_UP || me.getAction()==MotionEvent.ACTION_CANCEL) && mDragging) {
            mDragging = false;
                snapOpenOrClose(nw);
                return true;

        }
        else if (me.getAction() == MotionEvent.ACTION_MOVE && mDragging) {
            setPrimaryContentWidth(nw);
            }


        return false;
    }

    private boolean setPrimaryContentWidth(int newWidth) {
        newWidth=  Math.max(minSize,newWidth);
        newWidth = Math.min(maxSize,newWidth);// clamp >--(◣_◢)--<
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
