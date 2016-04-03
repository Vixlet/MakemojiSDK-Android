package com.makemoji.mojilib;

import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.makemoji.mojilib.model.MojiModel;

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
    int spanSize;
    Drawable phraseBg;

    public MojiGridAdapter (List<MojiModel> models, MojiInputLayout mojiInputLayout,int rows, int spanSize) {
        mojiModels = models;
        this.mojiInputLayout = mojiInputLayout;
        ROWS = rows;
        this.spanSize = spanSize;
        phraseBg = ContextCompat.getDrawable(mojiInputLayout.getContext(),R.drawable.mm_phrase_bg);
        phraseBg.setColorFilter(mojiInputLayout.phraseBgColor, PorterDuff.Mode.SRC);
    }

    public void setMojiModels(List<MojiModel> models){
        mojiModels = new ArrayList<>(models);
        notifyDataSetChanged();

    }
    @Override
    public int getItemCount() {
        return mojiModels.size();
    }


    @Override public int getItemViewType(int position){
        return mojiModels.get(position).phrase==1?1:0;
    }
    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        if (viewType==0)
        v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.mm_rv_moji_item, parent, false);
        else {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.mm_rv_phrase_item, parent, false);
            v.setBackgroundDrawable(phraseBg);
        }

        //v.getLayoutParams().height = parent.getHeight()/2;
        return new Holder(v,parent);
    }

    @Override
    public void onBindViewHolder(final Holder holder, int position) {
        final MojiModel model = mojiModels.get(position);
        Mojilytics.trackView(model.id);
        if (getItemViewType(position)==0) {
            holder.imageView.forceDimen(holder.dimen);
            holder.imageView.setModel(model);
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
        public void onClick(View v) {
            mojiInputLayout.addMojiModel(model, null);
                }
            });
        }
        else {
            LinearLayout ll = (LinearLayout) holder.itemView;
            ll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (MojiModel emoji : model.emoji)
                        mojiInputLayout.addMojiModel(emoji,null);
                }
            });
            while (holder.mojiImageViews.size()<model.emoji.size()) {
                View v = LayoutInflater.from(holder.itemView.getContext())
                        .inflate(R.layout.mm_rv_moji_item, ll, false);
                ll.addView(v);
                holder.mojiImageViews.add((MojiImageView)v);
            }
                for (int i = 0; i < ll.getChildCount(); i++) {
                    MojiImageView mojiImageView = (MojiImageView) ll.getChildAt(i);

                    MojiModel sequence = model.emoji.size()>i?model.emoji.get(i):null;
                    if (sequence!=null) {
                        mojiImageView.forceDimen(holder.dimen);
                        mojiImageView.setModel(sequence);
                        mojiImageView.setVisibility(View.VISIBLE);
                    }
                    else mojiImageView.setVisibility(View.GONE);
                }

            }


    }



class Holder extends RecyclerView.ViewHolder {
    MojiImageView imageView;
    int dimen;
    List<MojiImageView> mojiImageViews = new ArrayList<>();

    public Holder(View v, ViewGroup parent) {
        super(v);
        if (v instanceof MojiImageView)imageView = (MojiImageView) v;
        dimen = spanSize;


    }
}
}