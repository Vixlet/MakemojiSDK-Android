package com.example.mojilib;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.UiThread;
import android.support.v4.content.SharedPreferencesCompat;

import com.example.mojilib.model.MojiModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Scott Baar on 1/24/2016.
 */
public abstract class PagerPopulator<T> {
    protected List<T> mojiModels = new ArrayList<>();

    public interface PopulatorObserver{
       @UiThread void onNewDataAvailable();
    }
    protected abstract void setup(PopulatorObserver observer);//once done, call the next two
    abstract List<T> populatePage(int count, int offset);
    int getTotalCount(){
        return mojiModels.size();
    };
}
