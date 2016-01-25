package com.example.mojilib;

import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.*;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mojilib.model.MojiModel;
import com.squareup.picasso252.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Scott Baar on 1/5/2016.
 */
public class HorizRVAdapter extends Adapter<HorizRVAdapter.RVHolder>{

    MojiInputLayout mil;
    List<MojiModel> list = new ArrayList<>();
    boolean showNames = false;
    public HorizRVAdapter(MojiInputLayout mojiInputLayout){
        mil = mojiInputLayout;
    }
    @Override
    public RVHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.mm_horiz_moji_item, parent, false);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RVHolder rvHolder = ((RVHolder)v.getTag());
                MojiModel model = list.get(rvHolder.pos);
                mil.addMojiModel(model,
                        rvHolder.image.getDrawable()!=null && rvHolder.image.getDrawable() instanceof BitmapDrawable?
                                (BitmapDrawable)rvHolder.image.getDrawable(): null);
            }
        });
        return new RVHolder(v,parent);
    }

    @Override
    public void onBindViewHolder(RVHolder holder, int position) {
        MojiModel m = list.get(position);
        holder.name.setText(m.name);
        holder.name.setVisibility(showNames?View.VISIBLE:View.GONE);
        //Moji.loadImage(holder.image,m.image_url);
        Picasso.with(mil.getContext()).load(m.image_url).resize(holder.dimen,holder.dimen).into(holder.image);
        holder.pos = position;
    }


    public void setMojiModels(List<MojiModel> newList){
        list = newList;
        notifyDataSetChanged();
    }
    @Override
    public int getItemCount() {
       return list.size();
    }
    public void showNames(boolean enable){
        if (enable!=showNames){
            showNames= enable;
            notifyDataSetChanged();
        }
    }

    public class RVHolder extends ViewHolder
    {
        TextView name;
        ImageView image;
        View v;
        int dimen;
        int pos;
        public RVHolder(View v,ViewGroup parent){
            super(v);
            this.v = v;
            v.setTag(this);
            name = (TextView) v.findViewById(R.id.tv);
            image = (ImageView)v.findViewById(R.id.pic);
            int h = mil.getDefaultSpanDimension();//use same dimension as span to consolidate img fetches
           // (int)(parent.getHeight() *.8);
            dimen =h;
          //  image.setMinimumWidth(h);
        //    image.setMaxWidth(h);
        //    image.setMinimumHeight(h);
       //     image.setMaxHeight(h);

        }
    }
}
