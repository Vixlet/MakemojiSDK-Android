package com.makemoji.mojilib.wall;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.makemoji.mojilib.IMojiSelected;
import com.makemoji.mojilib.KBCategory;
import com.makemoji.mojilib.Moji;
import com.makemoji.mojilib.MojiGridAdapter;
import com.makemoji.mojilib.R;
import com.makemoji.mojilib.SmallCB;
import com.makemoji.mojilib.SpacesItemDecoration;
import com.makemoji.mojilib.model.Category;
import com.makemoji.mojilib.model.MojiModel;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import retrofit2.Response;

/**
 * Created by DouglasW on 4/16/2016.
 */
public class MojiWallFragment extends Fragment implements KBCategory.KBTAbListener,MojiGridAdapter.ClickAndStyler {
    View view;
    TabLayout tabLayout;
    ViewPager pager;
    MojiWallAdapter pagerAdapter;
    IMojiSelected mojiSelected;
    List<Category> categories =new ArrayList<>();
    @LayoutRes int  tabRes;
    public static MojiWallFragment newInstance() {

        Bundle args = new Bundle();

        MojiWallFragment fragment = new MojiWallFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

       TypedValue value = new TypedValue();
        getActivity().getTheme().resolveAttribute(R.attr._mm_wall_tab_layout,value,true);
        tabRes = value.resourceId;
        getActivity().getTheme().resolveAttribute(R.attr._mm_wall_header_layout,value,true);
        int headerRes = value.resourceId;
        view = inflater.inflate(R.layout.mm_wall_frag,container,false);
        View header =inflater.inflate(headerRes,(ViewGroup)view,false);
        ((ViewGroup) view).addView(header,0);

        tabLayout =(TabLayout) view.findViewById(R.id.tabs);
        pager = (ViewPager) view.findViewById(R.id.pager);
        pagerAdapter = new MojiWallAdapter(getChildFragmentManager(),categories);
        pager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(pager);

        final SharedPreferences sp = getContext().getSharedPreferences("emojiWall",0);
        try {
            String s = sp.getString("data", null);
            Map<String, List<MojiModel>> data =
                    MojiModel.gson.fromJson(s, new TypeToken<Map<String, List<MojiModel>>>() {
                    }.getType());
            if (data != null)
                handleData(data);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        Moji.mojiApi.getEmojiWallData().enqueue(new SmallCB<Map<String, List<MojiModel>>>() {
            @Override
            public void done(Response<Map<String, List<MojiModel>>> response, @Nullable Throwable t) {
                if (t!=null){
                    t.printStackTrace();
                    return;
                }
                sp.edit().putString("data",MojiModel.gson.toJson(response.body())).apply();
                handleData(response.body());

            }
        });

        return view;
    }
    public synchronized void handleData(Map<String, List<MojiModel>> data){
        categories = new ArrayList<>();
        for (Map.Entry<String,List<MojiModel>> entry :data.entrySet()){
            Category c = new Category(entry.getKey(),null);
            c.models = entry.getValue();
            categories.add(c);
        }
        categories = KBCategory.mergeCategoriesDrawable(categories);
        List<Category> cached = Category.getCategories();
        for (Category cat : categories)//find icon url
            if (cat.drawableRes==0 && cat.image_url==null)
                for (Category old :cached)
                    if (cat.name.equalsIgnoreCase(old.name))
                        cat.image_url=old.image_url;
        ListIterator<Category> it = categories.listIterator();
        while (it.hasNext()){
            Category c = it.next();
            if (c.drawableRes == 0 && (c.image_url==null || c.image_url.isEmpty()))
                it.remove();
        }
        List<TabLayout.Tab> tabs = KBCategory.createTabs(tabLayout,categories,tabRes);
        onNewTabs(tabs);
    }

    @Override
    public void onNewTabs(List<TabLayout.Tab> tabs) {
        int selectedPosition = tabLayout.getSelectedTabPosition();
        tabLayout.removeAllTabs();
        for (TabLayout.Tab tab: tabs) {
            tabLayout.addTab(tab);
        }
        if (selectedPosition!= -1 && selectedPosition<tabs.size()) {
            tabLayout.getTabAt(selectedPosition).select();//setscrollposition doesn't work...
        }
        pagerAdapter = new MojiWallAdapter(getChildFragmentManager(),categories);
        pager.setAdapter(pagerAdapter);
    }

    @Override
    public void addMojiModel(MojiModel model, BitmapDrawable d) {
        if (mojiSelected!=null) mojiSelected.mojiSelected(model,d);
    }

    @Override
    public int getPhraseBgColor() {
        return getResources().getColor(R.color._mm_default_phrase_bg_color);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (!(getActivity() instanceof IMojiSelected) )
        throw new RuntimeException("activity must implement IMojiSelected to use MojiWallFragment");
        mojiSelected = (IMojiSelected)getActivity();
    }
    public static class MojiWallAdapter extends FragmentStatePagerAdapter {
         List<Category> categories;
        public MojiWallAdapter(FragmentManager fm,List<Category> categories) {
            super(fm);
            this.categories = categories;

        }

        @Override
        public int getCount() {
            return categories.size();
        }

        @Override
        public Fragment getItem(int position) {
            return MojiWallPage.newInstance(categories.get(position).name,categories.get(position).models);
        }
    }

    public static class MojiWallPage extends Fragment {
        String category;
        int iconRes;
        String iconUrl;
        public List<MojiModel> models = new ArrayList<>();
        RecyclerView rv;
        MojiGridAdapter mojiGridAdapter;
        int parentWidth;

        /**
         * Create a new instance of CountingFragment, providing "num"
         * as an argument.
         */
        static MojiWallPage newInstance(String category,List<MojiModel> models) {
            MojiWallPage f = new MojiWallPage();
            RecyclerView.ItemDecoration decoration;

            // Supply num input as an argument.
            Bundle args = new Bundle();
            args.putString("category", category);
            f.setArguments(args);
            f.models = models;

            return f;
        }

        /**
         * When creating, retrieve this instance's number from its arguments.
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            category = getArguments() != null ? getArguments().getString("category") : "";
        }

        /**
         * The Fragment's UI is just a simple text view showing its
         * instance number.
         */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            parentWidth = container.getWidth();
            View v = inflater.inflate(R.layout.mm_wall_page, container, false);
            rv = (RecyclerView) v;
            rv.setLayoutManager(new GridLayoutManager(getContext(),5,LinearLayoutManager.VERTICAL,false));
            return v;
        }

        RecyclerView.ItemDecoration itemDecoration;
        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            rv.post(new Runnable() {
                @Override
                public void run() {

            parentWidth = ((View) rv.getParent().getParent()).getWidth();
            int size = (int)(parentWidth-(10*10*Moji.density))/5;
            mojiGridAdapter = new MojiGridAdapter(models,(MojiGridAdapter.ClickAndStyler)getParentFragment(),5,
                    size);
            mojiGridAdapter.setImagesSizedtoSpan(false);
            if (itemDecoration!=null) {
                rv.removeItemDecoration(itemDecoration);
                itemDecoration = null;
            }
            int hspace =(parentWidth-(size*5))/10;
            itemDecoration = new SpacesItemDecoration((int)(15*Moji.density),hspace);
            rv.addItemDecoration(itemDecoration);
            rv.setAdapter(mojiGridAdapter);
            Log.d("wall","size: "+ size + " hspace:"+ hspace);

                }
            });
        }

    }

}
