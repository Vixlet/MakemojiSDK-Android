package com.makemoji.mojilib;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.makemoji.mojilib.model.MojiModel;

/**
 * page that contains one long grid of emojis
 * Created by Scott Baar on 1/19/2016.
 */
public class OneGridPage extends MakeMojiPage implements PagerPopulator.PopulatorObserver {

    PagerPopulator<MojiModel> mPopulator;
    TextView heading;
    int count;
    int mojisPerPage = 10;
    public static final int ROWS = 5;
    RecyclerView rv;
    RecyclerView.ItemDecoration itemDecoration;

    private int oldH;
    public OneGridPage(String title, MojiInputLayout mojiInputLayout, PagerPopulator p) {
        super(R.layout.mm_one_grid_page, mojiInputLayout);
        mPopulator = p;
        heading = (TextView) mView.findViewById(R.id._mm_page_heading);
        heading.setTextColor(mMojiInput.getHeaderTextColor());
        heading.setText(title);
        rv = (RecyclerView) mView.findViewById(R.id._mm_page_grid);
        rv.setLayoutManager(new GridLayoutManager(mojiInputLayout.getContext(), ROWS, LinearLayoutManager.HORIZONTAL, false));
        mPopulator.setup(this);
        mView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (oldH== mView.getHeight())return;
                oldH = mView.getHeight();
                onNewDataAvailable();

            }
        });


    }

    //called by the populater once a query is complete.
    @Override
    public void onNewDataAvailable() {
        if (mView.getHeight()==0 || mPopulator.getTotalCount()==0)return;
        int h = rv.getHeight();
        int size = h / ROWS;
        int vSpace = (h - (size * ROWS)) / ROWS;
        int hSpace = (mView.getWidth() - (size * 8)) / 16;


        mojisPerPage = Math.max(10, 8 * ROWS);
        count = mPopulator.getTotalCount();
        MojiGridAdapter adapter = new MojiGridAdapter(mPopulator.populatePage(mPopulator.getTotalCount(), 0), mMojiInput, ROWS, size);
        if (itemDecoration!=null) rv.removeItemDecoration(itemDecoration);
        itemDecoration = new SpacesItemDecoration(vSpace, hSpace);
        rv.addItemDecoration(itemDecoration);
        rv.setAdapter(adapter);

    }
}


