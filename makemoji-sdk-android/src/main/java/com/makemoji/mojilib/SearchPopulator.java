package com.makemoji.mojilib;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.makemoji.mojilib.model.MojiModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

/**
 * Created by Scott Baar on 1/25/2016.
 */
public class SearchPopulator extends PagerPopulator<MojiModel> {
    MojiSQLHelper mojiSQLHelper;
    String currentQuery = "";
    @Override
    public void setup(PopulatorObserver observer) {
        super.setup(observer);
        mojiSQLHelper = MojiSQLHelper.getInstance(Moji.context);
        Moji.mojiApi.getFlashtags().enqueue(new SmallCB<List<MojiModel>>() {
            @Override
            public void done(final Response<List<MojiModel>> response, @Nullable Throwable t) {
                if (t!=null){
                    t.printStackTrace();
                    return;
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        mojiSQLHelper.insert(response.body());
                    }
                });
            }
        });
    }

    @Override
    public List<MojiModel> populatePage(int count, int offset) {
        if (mojiModels.size()<offset)return new ArrayList<>();
        if (offset+count>mojiModels.size())count = mojiModels.size()-offset;
        return mojiModels.subList(offset,offset+count);
    }
    //search off thread. If query is still relevant then return results.
    public void search(@NonNull String query){
        final String runQuery = query;
        currentQuery = query;

        if (query.isEmpty()){
            Moji.mojiApi.getTrendingFlashtags().enqueue(new SmallCB<List<MojiModel>>() {
                @Override
                public void done(Response<List<MojiModel>> response, @Nullable Throwable t) {
                    if (t!=null){
                        t.printStackTrace();
                        return;
                    }
                    if (runQuery.equals(currentQuery)){
                        mojiModels = response.body();
                        if (obs!=null)obs.onNewDataAvailable();

                    }
                }
            });
        }
        else
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final List<MojiModel> models = mojiSQLHelper.search(runQuery,50);
                    Moji.handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (runQuery.equals(currentQuery)){
                                mojiModels = models;
                                if (obs!=null) obs.onNewDataAvailable();
                            }
                        }
                    });

                }
            }).start();

    }
}
