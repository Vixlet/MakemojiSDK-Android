package com.example.mojilib;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

/**
 * Created by Scott Baar on 1/4/2016.
 */
public class MojiInputLayout extends LinearLayout {
    ImageButton cameraImageButton;
    EditText editText;
    View sendLayout;
    ViewPager topViewPager;
    TopPagerAdapter topAdaper;



    public MojiInputLayout(Context context) {
        super(context);
        init(null,0);
    }

    public MojiInputLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs,0);
    }

    public MojiInputLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs,defStyle);
    }
    public void init(AttributeSet attributeSet, int defStyle){
        TypedArray a = getContext().getTheme().obtainStyledAttributes(attributeSet,R.styleable.MojiInputLayout,0,defStyle);
        int cameraDrawableRes = a.getResourceId(R.styleable.MojiInputLayout__mm_cameraButtonDrawableAttr,R.drawable.mm_unknown_image);
        int sendLayoutRes = a.getResourceId(R.styleable.MojiInputLayout__mm_sendButtonLayoutAttr,R.layout.mm_default_send_layout);
        boolean cameraVisiblity = a.getBoolean(R.styleable.MojiInputLayout__mm_sendButtonLayoutAttr,true);
        a.recycle();

        inflate(getContext(),R.layout.mm_moji_input_layout,this);
        LinearLayout ll = (LinearLayout) findViewById(R.id._mm_horizontal_ll);
        sendLayout = inflate(getContext(),sendLayoutRes,ll);
        cameraImageButton = (ImageButton) findViewById(R.id._mm_camera_ib);
        cameraImageButton.setImageResource(cameraDrawableRes);
        if (!cameraVisiblity) cameraImageButton.setVisibility(View.GONE);
        editText = (EditText)findViewById(R.id._mm_edit_text);
        topViewPager = (ViewPager)findViewById(R.id._mm_top_viewpager);
        topAdaper = new TopPagerAdapter();
        topViewPager.setAdapter(topAdaper);

    }

    @Override
    public boolean isInEditMode(){
        return true;
    }
    class TopPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view==object;
        }
        @Override
        public Object instantiateItem(ViewGroup container, int position){

                View v = LayoutInflater.from(getContext()).inflate(R.layout.mm_top_left_page,container,false);
                container.addView(v);
                return v;
        }
    }


}
