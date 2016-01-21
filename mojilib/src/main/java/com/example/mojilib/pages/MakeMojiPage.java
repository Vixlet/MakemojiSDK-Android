package com.example.mojilib.pages;

import android.support.annotation.CallSuper;
import android.view.View;
import android.view.ViewStub;
import android.widget.FrameLayout;

/**
 * A page selected with one of the three control buttons on the left.
 * Created by Scott Baar on 1/10/2016.
 */
public class MakeMojiPage {

    boolean mIsVisible;
    boolean mIsSetup;
    View mView;
    ViewStub mViewStub;

    protected MakeMojiPage(ViewStub stub){
        mViewStub = stub;
    }
    protected MakeMojiPage(View v){ mView = v;}
    @CallSuper
    public void show(){
        mIsVisible=true;
        if (mView==null) mView = mViewStub.inflate();
        if (!mIsSetup)
            setup();
        mView.setVisibility(View.VISIBLE);
    }
    @CallSuper
    public void hide(){
        mIsVisible=false;
        mView.setVisibility(View.GONE);

    }

    /**
     * called the first time the view is shown
     */
    @CallSuper
    protected void setup(){
        mIsSetup=true;

    }
    public boolean isVisible(){
        return mIsVisible;
    }

    public void setHeight (int height){
        if (mView==null)return;
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mView.getLayoutParams();
        if (params.height!=height) {
            params.height = height;
            mView.setLayoutParams(params);
        }
        mView.invalidate();
    }

}
