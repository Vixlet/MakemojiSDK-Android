package com.makemoji.mojilib;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Response;

/**
 * Analytics for emojis, of course
 * Created by Scott Baar on 2/2/2016.
 */
public class Mojilytics {
    private static Map<Integer,Data> viewed = new ConcurrentHashMap<>();
    private static Handler handler = new Handler(Looper.getMainLooper());
    private static boolean runnablePosted;

    private static int MAX_SIZE = 200;
    private static int TRACK_INTERVAL = 30;

    private static DateFormat sdf = SimpleDateFormat.getInstance();

    public static void trackView(int id) {
        Data d = viewed.get(id);
        if (d != null) {
            if (System.currentTimeMillis() > d.lastViewTime + 300) {
                d.lastViewTime = System.currentTimeMillis();
                d.viewCount++;
            }
        } else {
            d = new Data(id);
            viewed.put(id, d);
        }
        if (viewed.size()>MAX_SIZE){
            handler.removeCallbacks(sendRunnable);
            sendRunnable.run();
            runnablePosted=false;
        }
        else if (!runnablePosted) {
            handler.postDelayed(sendRunnable,TRACK_INTERVAL *1000);
            runnablePosted = true;
        }
    }
    static void forceSend(){
        sendRunnable.run();
    }
    private static Runnable sendRunnable = new Runnable() {
        @Override
        public void run() {
            runnablePosted=false;
            if (viewed.isEmpty())return;
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<Integer,Data> e : viewed.entrySet())    {
                Data d = e.getValue();
                sb.append(d.id).append("[emoji_id]=").append(d.id).append("&").append(d.id).append("[views]=").append(d.viewCount).append("&");
            }
            viewed.clear();
            sb.append("data=").append(sdf.format(new Date()));
            Moji.mojiApi.trackViews(RequestBody.create(MediaType.parse("text/plain"),sb.toString())).enqueue(new SmallCB<Void>() {
                @Override
                public void done(Response<Void> response, @Nullable Throwable t) {
                    if (t!=null){
                        t.printStackTrace();
                        return;
                    }
                }
            });
        }
    };

    static class Data{
        int id;
        long lastViewTime;
        int viewCount;
        Data(int id){
            this.id = id;
            viewCount = 1;
            lastViewTime = System.currentTimeMillis();
        }
    }

}
