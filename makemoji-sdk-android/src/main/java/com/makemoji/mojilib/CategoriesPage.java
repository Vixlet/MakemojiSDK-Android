package com.makemoji.mojilib;

import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ViewStub;
import android.widget.TextView;

import com.makemoji.mojilib.model.Category;

import java.util.List;

import retrofit2.Response;

/**
 * Created by Scott Baar on 1/10/2016.
 */
public class CategoriesPage extends MakeMojiPage implements CategoriesAdapter.ICatListener{
    RecyclerView rv;
    GridLayoutManager glm;
    MojiApi api;
    CategoriesAdapter adapter;
    MojiInputLayout mojiInputLayout;
    public CategoriesPage(ViewStub stub, MojiApi mojiApi, final MojiInputLayout mojiInputLayout){
        super(stub,mojiInputLayout);
        this.mojiInputLayout = mojiInputLayout;
        api=mojiApi;
        adapter = new CategoriesAdapter(this,mojiInputLayout.getHeaderTextColor());
        List<Category> categories = Category.getCategories();
        adapter.setCategories(categories);
        mojiInputLayout.requestRnUpdate();

        if (Moji.enableUpdates)
        api.getCategories().enqueue(new SmallCB<List<Category>>() {
            @Override
            public void done(Response<List<Category>> response, @Nullable Throwable t) {
                if (t!=null){
                    t.printStackTrace();
                    return;
                }
                Category.saveCategories(response.body());
                adapter.setCategories(response.body());
                mojiInputLayout.requestRnUpdate();
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
        rv.setItemAnimator(null);
        ((TextView)mView.findViewById(R.id._mm_page_heading)).setTextColor(mMojiInput.getHeaderTextColor());

    }
    public void show(){
        super.show();
        mojiInputLayout.requestRnUpdate();

    }
    public void hide(){
        super.hide();
    }
    public void refresh(){
        if (adapter!=null) adapter.notifyItemRangeChanged(0,adapter.getItemCount());
        mojiInputLayout.requestRnUpdate();
    }

    @Override
    public void onClick(Category category) {
        if (category.isLocked()&& !MojiUnlock.getUnlockedGroups().contains(category.name)){
            mMojiInput.onLockedCategoryClicked(category.name);
            return;
        }
        MakeMojiPage mmp = new VPPage(category.name,mMojiInput,new CategoryPopulator(category));
        mMojiInput.addPage(mmp);
    }
}
