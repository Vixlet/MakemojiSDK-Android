package com.makemoji.mojilib;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.makemoji.mojilib.model.MojiModel;

import java.util.Collection;
import java.util.List;

/**
 * page that contains one long grid of emojis
 * Created by Scott Baar on 1/19/2016.
 */
public class OneGridPage extends MakeMojiPage implements PagerPopulator.PopulatorObserver {

    PagerPopulator<MojiModel> mPopulator;
    TextView heading;
    int count;
    public int ROWS = DEFAULT_ROWS;
    public static final int DEFAULT_ROWS = 5;
    RecyclerView rv;
    RecyclerView.ItemDecoration itemDecoration;
    boolean gifs;

    private int oldH;
    public OneGridPage(String title, MojiInputLayout mojiInputLayout, PagerPopulator p) {
        super("gifs".equalsIgnoreCase(title)?R.layout.mm_one_grid_page_gif:R.layout.mm_one_grid_page, mojiInputLayout);
        if ("gifs".equalsIgnoreCase(title)) {
            gifs=true;
            ROWS = mojiInputLayout.getResources().getInteger(R.integer._mm_gif_rows);
        }
        mPopulator = p;
        heading = (TextView) mView.findViewById(R.id._mm_page_heading);
        if (!gifs)heading.setTextColor(mMojiInput.getHeaderTextColor());
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
        count = mPopulator.getTotalCount();
        List<MojiModel> mojiModelList = mPopulator.populatePage(count,0);
        if (hasVideo(mojiModelList)){
                ROWS = rv.getResources().getInteger(R.integer._mm_video_rows);
                rv.setLayoutManager(new GridLayoutManager(rv.getContext(), ROWS, LinearLayoutManager.HORIZONTAL, false));
        }
        int h = rv.getHeight();
        int size = h / DEFAULT_ROWS;
        int vSpace = (h - (size * DEFAULT_ROWS)) / DEFAULT_ROWS;
        int hSpace = (mView.getWidth() - (size * 8)) / 16;

        MojiGridAdapter adapter = new MojiGridAdapter(mojiModelList, mMojiInput, false, size);
        if (itemDecoration!=null) rv.removeItemDecoration(itemDecoration);
       // if (!gifs){
            itemDecoration = new SpacesItemDecoration(vSpace, hSpace);
            rv.addItemDecoration(itemDecoration);
        //}
        rv.setAdapter(adapter);

    }
    private boolean hasVideo(Collection<MojiModel> list){
        for (MojiModel m : list)
            if (m.isVideo())return true;
        return false;

    }
    @Override
    public void hide(){
        super.hide();
    }
}


