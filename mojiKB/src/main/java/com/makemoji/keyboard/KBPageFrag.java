package com.makemoji.keyboard;

import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.makemoji.mojilib.CategoryPopulator;
import com.makemoji.mojilib.MojiGridAdapter;
import com.makemoji.mojilib.OneGridPage;
import com.makemoji.mojilib.PagerPopulator;
import com.makemoji.mojilib.SpacesItemDecoration;
import com.makemoji.mojilib.model.Category;
import com.makemoji.mojilib.model.MojiModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DouglasW on 4/3/2016.
 */
public class KBPageFrag extends Fragment implements PagerPopulator.PopulatorObserver, MojiGridAdapter.ClickAndStyler {

    String name;
    CategoryPopulator populator;
    MojiGridAdapter adapter;
    RecyclerView rv;
    View v;
    int mojisPerPage;
    RecyclerView.ItemDecoration itemDecoration;
    static KBPageFrag newInstance(String categoryName) {
        KBPageFrag f = new KBPageFrag();
        Bundle args = new Bundle();
        args.putString("name", categoryName);
        f.setArguments(args);

        return f;
    }

    /**
     * When creating, retrieve this instance's number from its arguments.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments()!=null){
            name = getArguments().getString("name");
        }
        populator = new CategoryPopulator(new Category(name,null));
    }

    /**
     * The Fragment's UI is just a simple text view showing its
     * instance number.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.kb_page, container, false);
        TextView heading =(TextView) v.findViewById(R.id.kb_page_heading);
        heading.setText(name);
        rv = (RecyclerView)v.findViewById(R.id.kb_page_grid);
        return v;
    }
    @Override
    public void onViewCreated(View view, Bundle saved){
        populator.setup(this);
    }

    @Override
    public void onNewDataAvailable() {

        int h = rv.getHeight();
        int size = h / OneGridPage.ROWS;
        int vSpace = (h - (size * OneGridPage.ROWS)) / OneGridPage.ROWS;
        int hSpace = (v.getWidth() - (size * 8)) / 16;


        mojisPerPage = Math.max(10, 8 * OneGridPage.ROWS);
        List<MojiModel> models =populator.populatePage(populator.getTotalCount(),0);
        adapter = new MojiGridAdapter(models,this,OneGridPage.ROWS,size);
        if (itemDecoration!=null) rv.removeItemDecoration(itemDecoration);
        itemDecoration = new SpacesItemDecoration(vSpace, hSpace);
        rv.addItemDecoration(itemDecoration);
        rv.setAdapter(adapter);

    }

    @Override
    public void addMojiModel(MojiModel model, BitmapDrawable d) {

    }

    @Override
    public int getPhraseBgColor() {
        return getResources().getColor(R.color._mm_default_phrase_bg_color);
    }
}
