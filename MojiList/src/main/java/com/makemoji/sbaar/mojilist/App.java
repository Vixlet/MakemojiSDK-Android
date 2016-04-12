package com.makemoji.sbaar.mojilist;

import android.app.Application;
import android.content.Context;

import com.makemoji.keyboard.MMKB;
import com.makemoji.mojilib.Moji;

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
        //LeakCanary.install(this);
    }
}
