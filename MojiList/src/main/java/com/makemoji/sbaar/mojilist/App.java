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
import com.makemoji.mojilib.KBCategory;
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
        Moji.initialize(this,"940ced93abf2ca4175a4a865b38f1009d8848a58");
        LeakCanary.install(this);

        KBCategory.categoryDrawables.put("Sports",R.drawable.custom_kb_tab);
        MMKB.setCategoryListener(new MMKB.ICategorySelected() {
            View v;
            @Override
            public void categorySelected(String category,boolean locked, final FrameLayout parent) {
                if (v!=null) {
                    parent.removeView(v);
                    v=null;
                    return;
                }
                if (!"Sports".equalsIgnoreCase(category))return;
                v = new View(context);
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
        //Moji.setEnableUpdates(false);
       // Moji.loadOfflineFromAssets();//call only when new assets are in the app after an update
    }

}
