package com.example.mojilib.pages;

import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;

import com.example.mojilib.CategoriesAdapter;
import com.example.mojilib.MojiApi;
import com.example.mojilib.MojiInputLayout;
import com.example.mojilib.R;
import com.example.mojilib.SmallCB;
import com.example.mojilib.model.Category;

import java.util.List;

import retrofit2.Response;

/**
 * Created by Scott Baar on 1/10/2016.
 */
public class CategoriesPage extends MakeMojiPage {
    RecyclerView rv;
    GridLayoutManager glm;
    MojiApi api;
    CategoriesAdapter adapter;
    public CategoriesPage(ViewStub stub, MojiApi mojiApi, MojiInputLayout mojiInputLayout){
        super(stub,mojiInputLayout);
        api=mojiApi;
        adapter = new CategoriesAdapter();
        api.getCategories().enqueue(new SmallCB<List<Category>>() {
            @Override
            public void done(Response<List<Category>> response, @Nullable Throwable t) {
                if (t!=null){
                    t.printStackTrace();
                    return;
                }
                adapter.setCategories(response.body());
            }
        });

    }
    @Override
    protected void setup(){
        super.setup();
        rv = (RecyclerView)mView.findViewById(R.id._mm_cat_rv);
        glm = new GridLayoutManager(mView.getContext(),2, LinearLayoutManager.HORIZONTAL,false);
        rv.setLayoutManager(glm);
        rv.setAdapter(adapter);

    }
    public void show(){
        super.show();

    }
    public void hide(){
        super.hide();
    }

}
