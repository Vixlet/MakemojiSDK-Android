package com.makemoji.mojilib;

import android.support.annotation.Nullable;

import org.json.JSONObject;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import retrofit2.Response;

/**
 * Created by Scott Baar on 5/18/2016.
 */
public class MojiUnlock {
    private static Set<String> unlocked;
    public interface ICategoryUnlock{
        void unlockDone(String name, @Nullable Throwable throwable);
    }
    public interface ILockedCategoryClicked{
        void onClick(String name);
    }
    public static Set<String> getUnlockedGroups(){
        if (unlocked == null)
           unlocked= Moji.context.getSharedPreferences("mm_unlock",0).getStringSet("groupUnlocks",new HashSet<String>());
        return unlocked;
    }
    private static void addGroup(String name){
        getUnlockedGroups().add(name);
        Moji.context.getSharedPreferences("mm_unlock",0).edit().putStringSet("groupUnlocks",getUnlockedGroups()).apply();

    }
    public static void unlockCategory(final String name, final ICategoryUnlock unlockListener){
        Moji.mojiApi.unlockGroup(name).enqueue(new SmallCB<JSONObject>() {
            @Override
            public void done(Response<JSONObject> response, @Nullable Throwable t) {
                if (t!=null){
                    t.printStackTrace();
                    unlockListener.unlockDone(name,t);
                    return;
                }
                if (!response.body().optBoolean("success")){
                    String message = "No message";
                    try {
                        message = response.body().getJSONObject("response").getJSONObject("message").getString("error");
                    }
                    catch (Exception e){
                        e.printStackTrace();
                        unlockListener.unlockDone(name,new Throwable(e.getMessage()));
                    }
                }
                addGroup(name);
                unlockListener.unlockDone(name,null);
            }
        });
    }

}
