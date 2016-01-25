package com.example.mojilib;

import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.mojilib.model.MojiModel;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

/**
 * populates a page based on a given category
 * Created by Scott Baar on 1/22/2016.
 */
public class TrendingPopulator extends PagerPopulator<MojiModel> {
    PopulatorObserver obs;
    MojiApi mojiApi;
    SharedPreferences sp;
    public TrendingPopulator(){
        mojiApi = Moji.mojiApi;
        sp = Moji.context.getSharedPreferences("_mm_trending",0);
    }


    boolean networkResponseServed;
    @Override
    public void setup(PopulatorObserver o) {
        this.obs = o;
        //look for cache
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    final List<MojiModel> cached = MojiModel.fromJSONArray(new JSONArray(sp.getString("trending", "[]")));
                    Moji.handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (!networkResponseServed) {
                                mojiModels = cached;
                                obs.onNewDataAvailable();
                            }
                        }
                    });
                }
                catch (JSONException je){
                    je.printStackTrace();
                    sp.edit().putString("trending",null);//delete cache if bad
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
                mojiModels = response.body();
                saveInBackground(mojiModels);
                networkResponseServed = true;
                obs.onNewDataAvailable();
            }
        });
    }

    private void saveInBackground(final List<MojiModel> models){
        new Thread(new Runnable() {
            @Override
            public void run() {
                sp.edit().putString("trending",MojiModel.toJsonArray(models).toString()).apply();
            }
        }).start();

    }
    @Override
    public List<MojiModel> populatePage(int count, int offset) {
        if (mojiModels.size()<offset)return new ArrayList<>();//return empty
        if (offset+count>mojiModels.size())count = mojiModels.size()-offset;
        return mojiModels.subList(offset,offset+count);
    }

    @Override
    public int getTotalCount() {
        return mojiModels.size();
    }
}
