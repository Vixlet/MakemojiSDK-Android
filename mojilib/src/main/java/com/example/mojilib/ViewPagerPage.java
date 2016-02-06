package com.example.mojilib;

import android.graphics.drawable.BitmapDrawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mojilib.model.MojiModel;
import com.squareup.picasso252.Picasso;
import com.viewpagerindicator.CirclePageIndicator;

import java.util.ArrayList;
import java.util.List;

/**
 * contains a viewpager that displays emojis. Populating the page is done by the populator
 * Created by Scott Baar on 1/19/2016.
 */
public class ViewPagerPage extends MakeMojiPage implements PagerPopulator.PopulatorObserver{

    PagerPopulator<MojiModel> mPopulator;
    ViewPager vp;
    TextView heading;
    int count;
    VPAdapter vpAdapter;
    int mojisPerPage = 10;
    CirclePageIndicator circlePageIndicator;
    public static final int ROWS = 5;

    public ViewPagerPage (String title,MojiInputLayout mojiInputLayout,PagerPopulator p){
        super(R.layout.mm_vp_page,mojiInputLayout);
        mPopulator = p;
        vp = (ViewPager) mView.findViewById(R.id._mm_moji_pager);
        vp.setOffscreenPageLimit(7);
        circlePageIndicator = (CirclePageIndicator) mView.findViewById(R.id._mm_vp_indicator);
     //   VPAdapter vpAdapter = new VPAdapter();
      //  vp.setAdapter(vpAdapter);
        heading = (TextView) mView.findViewById(R.id._mm_page_heading);
        heading.setText(title);
        mPopulator.setup(this);


    }
    int oldh,oldw;
    //called by the populater once a query is complete.
    @Override
    public void onNewDataAvailable(){
        oldh = mView.getHeight();
        oldw = mView.getWidth();
        mojisPerPage =Math.max(10,8 * ROWS);
        count = mPopulator.getTotalCount();
        vpAdapter = new VPAdapter();
        vp.setAdapter(vpAdapter);
        circlePageIndicator.setViewPager(vp);
        //vpAdapter.notifyDataSetChanged();


    }
    class VPAdapter extends PagerAdapter{

        @Override
        public int getCount() {
            int c = count/mojisPerPage + (count%mojisPerPage>0?1:0);
            return c;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = LayoutInflater.from(mMojiInput.getContext()).inflate(R.layout.mm_vp_page_content,container,false);
            container.addView(view);
            RecyclerView rv = (RecyclerView) view;
            MojiGridAdapter gridAdapter = new MojiGridAdapter(new ArrayList<MojiModel>(),mMojiInput,ROWS);
            gridAdapter.setMojiModels(mPopulator.populatePage(mojisPerPage,position*mojisPerPage));
            GridLayoutManager glm = new GridLayoutManager(mMojiInput.getContext(),ROWS,GridLayoutManager.HORIZONTAL,false);
            rv.setLayoutManager(glm);
            rv.setAdapter(gridAdapter);
            return view;
        }
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

    }

}
