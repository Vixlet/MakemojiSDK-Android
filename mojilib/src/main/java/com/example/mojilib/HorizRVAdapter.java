package com.example.mojilib;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.*;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Scott Baar on 1/5/2016.
 */
public class HorizRVAdapter extends Adapter<HorizRVAdapter.RVHolder>{

    @Override
    public RVHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.mm_horiz_moji_item, parent, false);
        return new RVHolder(v);
    }

    @Override
    public void onBindViewHolder(RVHolder holder, int position) {

    }


    @Override
    public int getItemCount() {
        return 30;
    }

    public static class RVHolder extends ViewHolder
    {
        TextView name;
        ImageView image;
        public RVHolder(View v){
            super(v);

        }
    }
}
