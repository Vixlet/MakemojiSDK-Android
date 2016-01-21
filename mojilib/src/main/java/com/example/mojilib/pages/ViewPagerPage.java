package com.example.mojilib.pages;

import android.view.View;

/**
 * contains a viewpager that displays emojis. Populating the page is done by the populator
 * Created by Scott Baar on 1/19/2016.
 */
public class ViewPagerPage extends MakeMojiPage{
    public interface PagerPopulater {
        void populatePage(int count, int offset);
        int getTotalCount();//-1 if unknown
    }
    PagerPopulater mPopulator;
    public ViewPagerPage (View v,PagerPopulater p){
        super(v);
        mPopulator = p;

    }

}
