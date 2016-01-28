package com.example.mojilib;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.mojilib.model.Category;
import com.example.mojilib.model.MojiModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

/**
 * populates a page based on a given category
 * Created by Scott Baar on 1/22/2016.
 */
public class CategoryPopulator extends PagerPopulator<MojiModel>  {
    Category category;
    PopulatorObserver obs;
    MojiApi mojiApi;
    SharedPreferences sp;
    public CategoryPopulator(Category category){
        this.category = category;
        mojiApi = Moji.mojiApi;
        sp =   Moji.context.getSharedPreferences("_mm_categories_cache",0);
    }


    @Override
    public void setup(PopulatorObserver o) {
        this.obs = o;
        mojiModels = MojiModel.getList(category.name);
        if (!mojiModels.isEmpty())
            obs.onNewDataAvailable();
        else
            mojiApi.getByCategory(category.name.replace(' ','_')).enqueue(new SmallCB<List<MojiModel>>() {
                @Override
                public void done(Response<List<MojiModel>> response, @Nullable Throwable t) {
                    if (t!=null){
                        Log.e("Category populator",t.getLocalizedMessage());
                        return;
                    }
                    mojiModels = response.body();
                    MojiModel.saveList(response.body(),category.name);
                    obs.onNewDataAvailable();
                }
            });
    }

}
