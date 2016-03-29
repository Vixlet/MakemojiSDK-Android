package com.makemoji.keyboard;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;

import com.makemoji.mojilib.Moji;

import static android.content.pm.ApplicationInfo.FLAG_LARGE_HEAP;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.HONEYCOMB;

/**
 * Created by DouglasW on 3/29/2016.
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Moji.initialize(this,"940ced93abf2ca4175a4a865b38f1009d8848a58",calculateMemoryCacheSize(this));
    }
    private static int calculateMemoryCacheSize(Context context) {
        ActivityManager am =(ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        boolean largeHeap = (context.getApplicationInfo().flags & FLAG_LARGE_HEAP) != 0;
        int memoryClass = am.getMemoryClass();
        // Target ~33% of the available heap.
        return 1024 * 1024 * memoryClass / 3;
    }
}
