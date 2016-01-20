package com.example.mojilib;

import android.content.Context;
import android.os.SystemClock;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

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
    @Override
    public void onFinishInflate(){
        super.onFinishInflate();
        leftView = findViewById(R.id._mm_left_buttons);
        recyclerView = (SnappyRecyclerView) findViewById(R.id._mm_recylcer_view);
        leftView.setOnTouchListener(this);

        mLastPrimaryContentSize = leftView.getMeasuredHeight();

    }

    private boolean mDragging;
    private long mDraggingStarted;
    private float mDragStartX;
    private float mDragStartY;
    private float mPointerOffset;

    private int mLastPrimaryContentSize;

    final static private int MAXIMIZED_VIEW_TOLERANCE_DIP = 30;
    final static private int TAP_DRIFT_TOLERANCE = 3;
    final static private int SINGLE_TAP_MAX_TIME = 175;

    @Override
    public boolean onTouch(View view, MotionEvent me) {
        if (me.getAction() == MotionEvent.ACTION_DOWN) {
            mDragging = true;
            mDraggingStarted = SystemClock.elapsedRealtime();
            mDragStartX = me.getX();
            mDragStartY = me.getY();
                mPointerOffset = me.getRawX() - leftView.getMeasuredWidth();
            return true;
        }
        else if (me.getAction() == MotionEvent.ACTION_UP) {
            mDragging = false;
            if (
                    mDragStartX < (me.getX() + TAP_DRIFT_TOLERANCE) &&
                            mDragStartX > (me.getX() - TAP_DRIFT_TOLERANCE) &&
                            mDragStartY < (me.getY() + TAP_DRIFT_TOLERANCE) &&
                            mDragStartY > (me.getY() - TAP_DRIFT_TOLERANCE) &&
                            ((SystemClock.elapsedRealtime() - mDraggingStarted) < SINGLE_TAP_MAX_TIME)) {
                if (isPrimaryContentMaximized()) {
                }
                return true;
            }
        }
        else if (me.getAction() == MotionEvent.ACTION_MOVE) {
                setPrimaryContentWidth((int) (me.getRawX() - mPointerOffset));
            }


        return true;
    }

    public boolean isPrimaryContentMaximized(){
        return false;
    }
    private boolean setPrimaryContentWidth(int newWidth) {
        newWidth=  Math.max(0,newWidth);
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) leftView.getLayoutParams();
        lp.width=newWidth;
        leftView.setLayoutParams(lp);
        return true;
    }


}
