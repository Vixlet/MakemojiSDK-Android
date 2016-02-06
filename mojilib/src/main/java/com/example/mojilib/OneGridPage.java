package com.example.mojilib;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mojilib.model.MojiModel;
import com.viewpagerindicator.CirclePageIndicator;

import java.util.ArrayList;

/**
 * page that contains one long grid of emojis
 * Created by Scott Baar on 1/19/2016.
 */
public class OneGridPage extends MakeMojiPage implements PagerPopulator.PopulatorObserver{

    PagerPopulator<MojiModel> mPopulator;
    ViewPager vp;
    TextView heading;
    int count;
    int mojisPerPage = 10;
    public static final int ROWS = 5;
    RecyclerView rv;

    public OneGridPage(String title, MojiInputLayout mojiInputLayout, PagerPopulator p){
        super(R.layout.mm_one_grid_page,mojiInputLayout);
        mPopulator = p;
        heading = (TextView) mView.findViewById(R.id._mm_page_heading);
        heading.setText(title);
        rv = (RecyclerView) mView.findViewById(R.id._mm_page_grid);
        rv.setLayoutManager(new GridLayoutManager(mojiInputLayout.getContext(),ROWS, LinearLayoutManager.HORIZONTAL,false));
        mPopulator.setup(this);


    }
    //called by the populater once a query is complete.
    @Override
    public void onNewDataAvailable(){
        mojisPerPage =Math.max(10,8 * ROWS);
        count = mPopulator.getTotalCount();
        MojiGridAdapter adapter = new MojiGridAdapter(mPopulator.populatePage(mPopulator.getTotalCount(),0),mMojiInput,ROWS);
        rv.setAdapter(adapter);

    }

}
