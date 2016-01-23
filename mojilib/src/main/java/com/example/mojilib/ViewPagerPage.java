package com.example.mojilib;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mojilib.model.MojiModel;
import com.squareup.picasso252.Callback;
import com.squareup.picasso252.Picasso;
import com.viewpagerindicator.CirclePageIndicator;

import java.util.ArrayList;
import java.util.List;

/**
 * contains a viewpager that displays emojis. Populating the page is done by the populator
 * Created by Scott Baar on 1/19/2016.
 */
public class ViewPagerPage extends MakeMojiPage{
    public interface PagerPopulater {
        void setup(ViewPagerPage page);//once done, call the next two
        List<MojiModel> populatePage(int count, int offset);
        int getTotalCount();
    }
    PagerPopulater mPopulator;
    ViewPager vp;
    TextView heading;
    int count;
    VPAdapter vpAdapter;
    int mojisPerPage = 10;
    CirclePageIndicator circlePageIndicator;

    public ViewPagerPage (String title,MojiInputLayout mojiInputLayout,PagerPopulater p){
        super(R.layout.mm_vp_page,mojiInputLayout);
        mPopulator = p;
        vp = (ViewPager) mView.findViewById(R.id._mm_moji_pager);
        circlePageIndicator = (CirclePageIndicator) mView.findViewById(R.id._mm_vp_indicator);
     //   VPAdapter vpAdapter = new VPAdapter();
      //  vp.setAdapter(vpAdapter);
        heading = (TextView) mView.findViewById(R.id._mm_page_heading);
        heading.setText(title);
        mPopulator.setup(this);

    }
    //called by the populater once a query is complete.
    public void onNewDataAvailable(){
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
            GridView gv = (GridView) view.findViewById(R.id._mm_vp_page_gv);
            MojiGridAdapter gridAdapter = new MojiGridAdapter(mMojiInput.getContext(),R.layout.mm_gv_moji_item,new ArrayList<MojiModel>());
            gridAdapter.setMojiModels(mPopulator.populatePage(mojisPerPage,position*mojisPerPage));
            gv.setAdapter(gridAdapter);
            return view;
        }
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

    }
    class MojiGridAdapter extends ArrayAdapter<MojiModel>
    {
        List<MojiModel> mojiModels = new ArrayList<>();

        public MojiGridAdapter(Context context, int resource, List<MojiModel> models) {
            super(context, resource, models);
        }

        public void setMojiModels(List<MojiModel> models){
            mojiModels = models;
            notifyDataSetChanged();

        }
        @Override
        public int getCount() {
            return mojiModels.size();
        }

        @Override
        public MojiModel getItem(int position) {
            return mojiModels.get(position);
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            MojiModel model = getItem(position);
            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                convertView = LayoutInflater.from(mMojiInput.getContext()).inflate(R.layout.mm_gv_moji_item,parent,false);
            }
                imageView = (ImageView) convertView;

            int width = (int)(80 *Moji.density * .9);
            Picasso.with(Moji.context).load(model.image_url).resize(width,width).into(imageView);

            return imageView;
        }
    }

}
