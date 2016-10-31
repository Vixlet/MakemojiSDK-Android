package com.makemoji.mojilib;

import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.util.Log;

import com.makemoji.mojilib.model.MojiModel;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

import retrofit2.Response;

/**
 * populates a page based cached trending emojis. Serve network response if no cache.
 * Created by Scott Baar on 1/22/2016.
 */
public class TrendingPopulator extends PagerPopulator<MojiModel> {

    MojiApi mojiApi;
    SharedPreferences sp;
    boolean cachedResponseServed;
    public TrendingPopulator(){
        mojiApi = Moji.mojiApi;
        sp = Moji.context.getSharedPreferences("_mm_categories",0);
    }

    @Override
    public void setup(PopulatorObserver o) {
        super.setup(o);
        //look for cache
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final List<MojiModel> cached = MojiModel.fromJSONArray(new JSONArray(sp.getString("trending", "[]")));
                    if (!cached.isEmpty()) {
                        cachedResponseServed = true;
                        Moji.handler.post(new Runnable() {
                            @Override
                            public void run() {
                                mojiModels = cached;
                                if (obs!=null) obs.onNewDataAvailable();
                            }
                        });
                    }
                }
                catch (JSONException je){
                    je.printStackTrace();
                    sp.edit().putString("trending",null).apply();//delete cache if bad
                }

            }
        }).start();

        mojiApi.getTrending().enqueue(new SmallCB<List<MojiModel>>() {
            @Override
            public void done(Response<List<MojiModel>> response, @Nullable Throwable t) {
                if (t!=null){
                    Log.e("trending populator",t.getLocalizedMessage());
                    return;
                }
                List<MojiModel>networkModels = response.body();
                saveInBackground(networkModels);
                if (!cachedResponseServed) {
                    mojiModels = networkModels;
                    if (obs!=null) obs.onNewDataAvailable();
                }

            }
        });
    }

    void saveInBackground(final List<MojiModel> models){
        new Thread(new Runnable() {
            @Override
            public void run() {
                sp.edit().putString("trending",MojiModel.toJsonArray(models).toString()).apply();
            }
        }).start();

    }

    @Override
    public int getTotalCount() {
        return mojiModels.size();
    }
}
