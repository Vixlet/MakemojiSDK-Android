package com.makemoji.mojilib;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
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
    public int COLS = DEFAULT_COLS;
    public static int DEFAULT_ROWS = 4;
    public static int DEFAULT_COLS = 7;
    RecyclerView rv;
    RecyclerView.ItemDecoration itemDecoration;
    boolean gifs;
    MojiInputLayout mojiInputLayout;
    View footer;
    public static int RNDELAY = 100;

    int oldH;
    int height;
    public OneGridPage(String title, MojiInputLayout mil, PagerPopulator<MojiModel> p) {
        super("gifs".equalsIgnoreCase(title)?R.layout.mm_one_grid_page_gif:R.layout.mm_one_grid_page, mil);
        this.mojiInputLayout = mil;
        ROWS = mojiInputLayout.getResources().getInteger(R.integer._mm_emoji_rows);
        COLS = mojiInputLayout.getResources().getInteger(R.integer._mm_emoji_cols);
        if ("gifs".equalsIgnoreCase(title)) {
            gifs=true;
            ROWS = mojiInputLayout.getResources().getInteger(R.integer._mm_gif_rows);
        }
        mPopulator = p;
        heading = (TextView) mView.findViewById(R.id._mm_page_heading);
        if (!gifs)heading.setTextColor(mMojiInput.getHeaderTextColor());
        heading.setText(title);
        rv = (RecyclerView) mView.findViewById(R.id._mm_page_grid);
        footer = mView.findViewById(R.id._mm_one_grid_footer);
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

        height = (mojiInputLayout.getPageFrame().getHeight() - heading.getHeight() - footer.getHeight());
        if (mojiInputLayout.hasRnListener())
            mojiInputLayout.postDelayed(new Runnable() {
                @Override
                public void run() {
                    rv.invalidate();
                    rv.requestLayout();
                    mojiInputLayout.requestRnUpdate();
                    mojiInputLayout.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            onNewDataAvailable();
                        }
                    },RNDELAY);
                }
            }, RNDELAY);


    }

    //called by the populater once a query is complete.
    @Override
    public void onNewDataAvailable() {
        height = (mojiInputLayout.getPageFrame().getHeight() - heading.getHeight() - footer.getHeight());
        if (height==0 || mPopulator.getTotalCount()==0)return;
        count = mPopulator.getTotalCount();
        List<MojiModel> mojiModelList = mPopulator.populatePage(count,0);
        if (hasVideo(mojiModelList)){
                ROWS = rv.getResources().getInteger(R.integer._mm_video_rows);
                rv.setLayoutManager(new GridLayoutManager(rv.getContext(), ROWS, LinearLayoutManager.HORIZONTAL, false));
        }
        int h = rv.getHeight();
        if (h ==0)
            h = (int) (height*.75f);
        int size = h / DEFAULT_ROWS;
        int vSpace = (h - (size * ROWS)) / ROWS;
        int hSpace = (mojiInputLayout.getWidth() - (size * COLS)) / (COLS*2);
        //heading.setPadding(hSpace,heading.getPaddingTop(),heading.getPaddingRight(),heading.getPaddingBottom());

        MojiGridAdapter adapter = new MojiGridAdapter(mojiModelList, mMojiInput, false, size);
        if (itemDecoration!=null) rv.removeItemDecoration(itemDecoration);
       // if (!gifs){
            itemDecoration = new SpacesItemDecoration(vSpace, hSpace);
            rv.addItemDecoration(itemDecoration);
        //}
        rv.setAdapter(adapter);
        if (mojiInputLayout.hasRnListener()) {
            rv.invalidate();
            rv.requestLayout();
            rv.scrollBy(1,0);
            mojiInputLayout.requestRnUpdate();
        }

    }
    public static boolean hasVideo(Collection<MojiModel> list){
        for (MojiModel m : list)
            if (m.isVideo())return true;
        return false;

    }
    @Override
    public void hide(){
        super.hide();
    }
}


