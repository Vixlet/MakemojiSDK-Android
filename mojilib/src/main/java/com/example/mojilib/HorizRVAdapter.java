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

    String[]names = {"asfdasdf","htrht","88888","pppp","tuteuetr","as","eryyn","Cvbcxzbcx","23424sf","nhnhnf"};
    @Override
    public RVHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.mm_horiz_moji_item, parent, false);
        return new RVHolder(v);
    }

    @Override
    public void onBindViewHolder(RVHolder holder, int position) {
        holder.name.setText(names[position%names.length]);
    }


    @Override
    public int getItemCount() {
        return 60;
    }

    public static class RVHolder extends ViewHolder
    {
        TextView name;
        ImageView image;
        public RVHolder(View v){
            super(v);
            name = (TextView) v.findViewById(R.id.tv);
            image = (ImageView)v.findViewById(R.id.pic);

        }
    }
}
