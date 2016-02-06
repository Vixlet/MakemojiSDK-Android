package com.example.mojilib;

import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mojilib.model.MojiModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Scott Baar on 2/5/2016.
 */
public class MojiGridAdapter extends RecyclerView.Adapter<MojiGridAdapter.Holder>
{
    List<MojiModel> mojiModels = new ArrayList<>();
    MojiInputLayout mojiInputLayout;
    final int ROWS;

    public MojiGridAdapter (List<MojiModel> models, MojiInputLayout mojiInputLayout,int rows) {
        mojiModels = models;
        this.mojiInputLayout = mojiInputLayout;
        ROWS = rows;
    }

    public void setMojiModels(List<MojiModel> models){
        mojiModels = new ArrayList<>(models);
        notifyDataSetChanged();

    }
    @Override
    public int getItemCount() {
        return mojiModels.size();
    }


    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.mm_rv_moji_item, parent, false);
        //v.getLayoutParams().height = parent.getHeight()/2;
        return new Holder(v,parent);
    }

    @Override
    public void onBindViewHolder(final Holder holder, int position) {
        final MojiModel model = mojiModels.get(position);
        holder.imageView.forceDimen(holder.dimen);
        Mojilytics.trackView(model.id);
        holder.imageView.setModel(model);
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BitmapDrawable bm=null;
                if (holder.imageView.getDrawable()!=null && holder.imageView.getDrawable() instanceof BitmapDrawable)
                    bm = (BitmapDrawable) holder.imageView.getDrawable();
                mojiInputLayout.addMojiModel(model,bm);
            }
        });

    }



class Holder extends RecyclerView.ViewHolder {
    MojiImageView imageView;
    int dimen;

    public Holder(View v, ViewGroup parent) {
        super(v);
        dimen = mojiInputLayout.getDefaultSpanDimension();
        imageView = (MojiImageView) v;

        int padding = (int) (2 * Moji.density);
        int oldw = parent.getWidth();
        int oldh = parent.getWidth();
        imageView.setMinimumWidth((oldw - padding) / 8);
        imageView.setMaxWidth((oldw - padding) / 8);
        imageView.setMinimumHeight((oldh - padding) / ROWS);
        imageView.setMaxHeight((oldh - padding) / ROWS);

    }
}
}