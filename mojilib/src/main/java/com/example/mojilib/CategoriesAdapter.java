package com.example.mojilib;

import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mojilib.model.Category;
import com.squareup.picasso252.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Scott Baar on 1/10/2016.
 */
public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.Holder>{
    List<Category> categories = new ArrayList<>();
    ICatListener iCatListener;
    @ColorInt int textColor;
    public interface ICatListener{
        void onClick(Category category);
    }

    public CategoriesAdapter(ICatListener iCatListener, @ColorInt int textColor){
        this.iCatListener = iCatListener;
        this.textColor = textColor;

    }
    public void setCategories(List<Category> newCategories){
        categories = newCategories;
        notifyDataSetChanged();

    }
    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.mm_item_category, parent, false);
        //v.getLayoutParams().height = parent.getHeight()/2;
        return new Holder(v,parent);
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        Category category = categories.get(position);
        if (holder.position!=position){
            //Moji.loadImage(holder.image,category.image_url);
            int width = (int)(80 *Moji.density * .9);
            Picasso.with(Moji.context).load(category.image_url).resize(width,width).into(holder.image);
            holder.title.setText(category.name);
            holder.view.setTag(category);
        }

    }

    View.OnClickListener catClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (iCatListener!=null) iCatListener.onClick((Category)v.getTag());
        }
    };
    @Override
    public int getItemCount() {
        return categories.size();
    }

    public class Holder extends RecyclerView.ViewHolder{
        public ImageView image;
        public TextView title;
        public View view;
        public int position =-1;
        public Holder(View itemView, ViewGroup parent) {
            super(itemView);
            view = itemView;
            view.setOnClickListener(catClick);
            image = (ImageView) itemView.findViewById(R.id._mm_item_category_iv);
            title = (TextView) itemView.findViewById(R.id._mm_item_category_tv);
            title.setTextColor(textColor);
            int padding = view.getPaddingLeft()*2 ;
            image.setMinimumWidth((parent.getWidth()/4)-padding);
            image.setMaxWidth((parent.getWidth()/4)-padding);
        }
    }
}
