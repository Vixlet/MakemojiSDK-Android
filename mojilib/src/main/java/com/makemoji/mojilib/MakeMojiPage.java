package com.makemoji.mojilib;

import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * A page selected with one of the three control buttons on the left.
 * Created by Scott Baar on 1/10/2016.
 */
public class MakeMojiPage implements PagerPopulator.PopulatorObserver{

    boolean mIsVisible;
    boolean mIsSetup;
    View mView;
    ViewStub mViewStub;
    protected MojiInputLayout mMojiInput;

    protected MakeMojiPage(@LayoutRes int layoutRes, MojiInputLayout mojiInputLayout){
        this.mMojiInput = mojiInputLayout;
        mView = LayoutInflater.from(mMojiInput.getContext()).inflate(layoutRes,mMojiInput.getPageFrame(),false);
    }
    protected MakeMojiPage(ViewStub stub, MojiInputLayout mojiInputLayout){
        mViewStub = stub;
        mMojiInput = mojiInputLayout;
    }
    protected MakeMojiPage(View v,MojiInputLayout mojiInputLayout){ mView = v;
        mMojiInput = mojiInputLayout;}
    @CallSuper
    public void show(){
        mIsVisible=true;
        if (mView==null) mView = mViewStub.inflate();
        if (!mIsSetup)
            setup();
        if (mView.getParent()==null)mMojiInput.getPageFrame().addView(mView);
        mView.setVisibility(View.VISIBLE);
    }
    @CallSuper
    public void hide(){
        mIsVisible=false;
        mView.setVisibility(View.GONE);

    }
    public void detatch(){
        mMojiInput.getPageFrame().removeView(mView);
    }

    /**
     * called the first time the view is shown
     */
    @CallSuper
    protected void setup(){
        TextView abc = (TextView)mView.findViewById(R.id._mm_abc_tv);
        ImageView backSpace = (ImageView) mView.findViewById(R.id._mm_backspace_button);
        if (abc!=null){
            abc.setTextColor(mMojiInput.getHeaderTextColor());
            abc.setOnClickListener(mMojiInput.abcClick);
        }
        if (backSpace!=null) {
            backSpace.setOnClickListener(mMojiInput.backspaceClick);
            backSpace.setOnTouchListener(mMojiInput.backspaceTouchListener);
            backSpace.setImageResource(mMojiInput.backSpaceDrawableRes);
        }
        mIsSetup=true;

    }

    public boolean isSetup(){
        return mIsSetup;
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

//should probably override except for category page
    @Override
    public void onNewDataAvailable() {

    }
}
