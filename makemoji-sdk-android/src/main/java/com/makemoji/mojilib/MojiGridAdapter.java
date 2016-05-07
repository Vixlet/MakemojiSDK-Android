package com.makemoji.mojilib;

import android.content.Context;
import android.graphics.Color;
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

import com.makemoji.mojilib.gif.GifImageView;
import com.makemoji.mojilib.model.MojiModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Scott Baar on 2/5/2016.
 */
public class MojiGridAdapter extends RecyclerView.Adapter<MojiGridAdapter.Holder>
{
    List<MojiModel> mojiModels = new ArrayList<>();
    boolean vertical;
    int spanSize;
    Drawable phraseBg;
    ClickAndStyler clickAndStyler;
    boolean enablePulse = true;
    boolean imaagesSizedToSpan = true;
    boolean useKbLifecycle;
    public interface ClickAndStyler{
        void addMojiModel(MojiModel model,BitmapDrawable d);
        Context getContext();
        int getPhraseBgColor();
    }
    public void setEnablePulse(boolean enable){
        enablePulse = enable;
    }
    //force gif image views to have the mmkb hash and NOT the open activity's.
    public void useKbLifecycle(){
        useKbLifecycle = true;

    }

    public MojiGridAdapter (List<MojiModel> models, ClickAndStyler clickAndStyler,boolean vertical, int spanSize) {
        mojiModels = models;
        this.clickAndStyler = clickAndStyler;
        this.spanSize = spanSize;
        this.vertical =vertical;
        phraseBg = ContextCompat.getDrawable(clickAndStyler.getContext(),R.drawable.mm_phrase_bg);
        phraseBg.setColorFilter(clickAndStyler.getPhraseBgColor(), PorterDuff.Mode.SRC);
    }

    public void setMojiModels(List<MojiModel> models){
        mojiModels = new ArrayList<>(models);
        notifyDataSetChanged();

    }
    public void setImagesSizedtoSpan(boolean enable){
        imaagesSizedToSpan = enable;
    }
    @Override
    public int getItemCount() {
        return mojiModels.size();
    }


    @Override public int getItemViewType(int position){
        if (mojiModels.get(position).gif==1)return 2;
        return mojiModels.get(position).phrase==1?1:0;
    }
    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        if (viewType==0)
        v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.mm_rv_moji_item, parent, false);
        else if (viewType==2){
            v = LayoutInflater.from(parent.getContext())
                    .inflate(vertical?R.layout.mm_gif_iv_vertical:R.layout.mm_gif_iv,parent,false);
        }
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
            holder.imageView.setPulseEnabled(enablePulse);
            holder.imageView.forceDimen(holder.dimen);
            holder.imageView.sizeImagesToSpanSize(imaagesSizedToSpan);
            holder.imageView.setModel(model);
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
        public void onClick(View v) {
            clickAndStyler.addMojiModel(model, null);
                }
            });
        }
        else if (getItemViewType(position)==2){
            holder.gifImageView.getFromUrl(model.image_url);
            holder.gifImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickAndStyler.addMojiModel(model, null);
                }
            });
        }
        else {
            LinearLayout ll = (LinearLayout) holder.itemView;
            ll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (MojiModel emoji : model.emoji)
                        clickAndStyler.addMojiModel(emoji,null);
                }
            });
            while (holder.mojiImageViews.size()<model.emoji.size()) {
                MojiImageView v = (MojiImageView)LayoutInflater.from(holder.itemView.getContext())
                        .inflate(R.layout.mm_rv_moji_item, ll, false);
                v.setPulseEnabled(enablePulse);
                //v.setPadding(0,(int)(2*Moji.density),(int)(-5*Moji.density),(int)(2*Moji.density));
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
                        //mojiImageView.setBackgroundColor(i%2==1? Color.RED:Color.BLUE);
                    }
                    else mojiImageView.setVisibility(View.GONE);
                }

            }


    }



class Holder extends RecyclerView.ViewHolder {
    MojiImageView imageView;
    int dimen;
    List<MojiImageView> mojiImageViews = new ArrayList<>();
    GifImageView gifImageView;

    public Holder(View v, ViewGroup parent) {
        super(v);
        if (v instanceof MojiImageView)imageView = (MojiImageView) v;
        if (v instanceof GifImageView) {
            gifImageView = (GifImageView) v;
            if (useKbLifecycle) gifImageView.useKbLifecycle = true;
        }
        dimen = spanSize;


    }
}
}