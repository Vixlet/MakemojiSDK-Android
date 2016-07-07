package com.makemoji.mojilib;

import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.makemoji.mojilib.model.MojiModel;
import com.makemoji.mojilib.model.ReactionsData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Scott Baar on 1/5/2016.
 */
class ReactionsAdapter extends Adapter<ReactionsAdapter.CellHolder>{

    MojiInputLayout mil;
    List<ReactionsData.Reaction> list = new ArrayList<>();
    boolean showNames = false;
    public ReactionsAdapter(){}
    @Override
    public CellHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.mm_horiz_moji_item, parent, false);
        return new CellHolder(v,parent);
    }

    @Override
    public void onBindViewHolder(CellHolder holder, int position) {
        ReactionsData.Reaction r = list.get(position);
        Moji.setText(r.toSpanned(holder.left),holder.left);
        holder.right.setText(""+r.total);

    }


    public void setReactions(List<ReactionsData.Reaction> newList){
        list = newList;
        notifyDataSetChanged();
    }
    @Override
    public int getItemCount() {
       return list.size();
    }

    public class CellHolder extends ViewHolder
    {
        View v;
        TextView left,right;
        public CellHolder(View v,ViewGroup parent){
            super(v);
            this.v = v;

            left = (TextView)v.findViewById(R.id._mm_reaction_left_tv);
            right =(TextView) v.findViewById(R.id._mm_reaction_right_tv);

        }
    }
}
