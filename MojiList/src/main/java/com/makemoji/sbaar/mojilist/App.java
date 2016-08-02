package com.makemoji.sbaar.mojilist;

import android.app.Application;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.makemoji.keyboard.MMKB;
import com.makemoji.mojilib.Moji;
import com.squareup.leakcanary.LeakCanary;

/**
 * Created by Scott Baar on 12/14/2015.
 */
public class App extends Application {
    public static Context context;
    @Override
    public void onCreate(){
        super.onCreate();
        context=this;
        Moji.initialize(this,"bfd3eea60abad87d378f87939ef3a116e8b23a35");
        LeakCanary.install(this);

//bow moji 6b503d6ba664bdb2565a6421f0a8fda1791b3e49
        //Yt bfd3eea60abad87d378f87939ef3a116e8b23a35
        //sdk 940ced93abf2ca4175a4a865b38f1009d8848a58
        //a custom listener that will display a view (ie to prompt a purchase), when a locked category is clicked
        MMKB.setLockedListener(new MMKB.ILockedCategorySelected() {
            @Override
            public void categorySelected(String category, final FrameLayout parent) {
                View v = new View(context);
                v.setAlpha(.5f);
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.setVisibility(View.GONE);
                        parent.removeView(v);
                    }
                });
                v.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int)(210*Moji.density), Gravity.FILL));
                v.setBackgroundColor(ContextCompat.getColor(context,R.color.colorPrimary));
                parent.addView(v);
            }
        });
    }
}
