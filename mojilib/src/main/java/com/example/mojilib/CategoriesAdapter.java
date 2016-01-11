package com.example.mojilib;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mojilib.model.Category;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Scott Baar on 1/10/2016.
 */
public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.Holder>{
    List<Category> categories = new ArrayList<>();

    public CategoriesAdapter(){

    }
    public void setCategories(List<Category> newCategories){
        categories = newCategories;
        notifyDataSetChanged();

    }
    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.mm_item_category, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        Category category = categories.get(position);
        if (holder.position!=position){
            Moji.loadImage(holder.image,category.image_url);
            holder.title.setText(category.name);
        }

    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public class Holder extends RecyclerView.ViewHolder{
        public ImageView image;
        public TextView title;
        public int position =-1;
        public Holder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id._mm_item_category_iv);
            title = (TextView) itemView.findViewById(R.id._mm_item_category_tv);
        }
    }
}
