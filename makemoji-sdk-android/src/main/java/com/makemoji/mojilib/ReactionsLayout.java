package com.makemoji.mojilib;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.makemoji.mojilib.model.ReactionsData;

import java.util.List;

/**
 * Created by s_baa on 7/5/2016.
 */
public class ReactionsLayout extends LinearLayout implements PagerPopulator.PopulatorObserver {
    ReactionsData data;
    List<ReactionsData.Reaction> reactions;
    ReactionsAdapter adapter;
    RecyclerView rv;

    public ReactionsLayout(Context context) {
        super(context);
        init(null,0);
    }

    public ReactionsLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs,0);
    }

    public ReactionsLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs,defStyleAttr);
    }

    public void init(AttributeSet attrs,int defStyle){
        inflate(getContext(),R.layout.mm_reactions_layout,this);
        rv = (RecyclerView)findViewById(R.id.recyclerView);
        rv.setLayoutManager(new LinearLayoutManager(getContext(),HORIZONTAL,false));
        adapter = new ReactionsAdapter();

    }
    public void setReactionsData(ReactionsData newData){
        if (data!=null&& newData!=data) data.removeObserver(this);//remove old observer so we don't recieve updates from it anymore.
        this.data = newData;
        data.setObserver(this);
    }

    @Override
    public void onNewDataAvailable() {
        reactions = data.getReactions();
        adapter.setReactions(reactions);

    }

}
