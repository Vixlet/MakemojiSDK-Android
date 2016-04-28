package com.makemoji.mojilib;

import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.view.View;
import android.widget.ImageView;

import com.makemoji.mojilib.Moji;
import com.makemoji.mojilib.MojiApi;
import com.makemoji.mojilib.SmallCB;
import com.makemoji.mojilib.model.Category;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import retrofit2.Response;

/**
 * Created by Scott Baar on 3/29/2016.
 */
public class KBCategory {
    public interface KBTAbListener{
        void onNewTabs(List<TabLayout.Tab> tabs);
    }
    static Map<String,Integer> defaults = new HashMap<>();
    private static String [] defaultCategories = {"trending","animals","clothing","expression","food",
            "hands","objects","politics","pop culture","sports","keyboard"};
    private static int [] icons = {R.drawable.mm_trending,R.drawable.mm_animals,R.drawable.mm_clothing,R.drawable.mm_expression,R.drawable.mm_food,
            R.drawable.mm_hands,R.drawable.mm_objects,R.drawable.mm_politics,R.drawable.mm_popculture,R.drawable.mm_sports,R.drawable.mm_keyboard};
    static{
        for (int i = 0; i < defaultCategories.length; i++) {
            defaults.put(defaultCategories[i],icons[i]);
        }

    }

    public static List<TabLayout.Tab> getTabs(final TabLayout tabLayout, final KBTAbListener kbtAbListener, @LayoutRes final int layoutRes){
        List<TabLayout.Tab> tabs = new ArrayList<>();
        List<Category> cachedCategories = Category.getCategories();
        Moji.mojiApi.getCategories().enqueue(new SmallCB<List<Category>>() {
            @Override
            public void done(final Response<List<Category>> response, @Nullable Throwable t) {
                if (t!=null){
                    t.printStackTrace();
                    return;
                }
                Category.saveCategories(response.body());
                kbtAbListener.onNewTabs(returnTabs(tabLayout,response.body(),layoutRes));

            }
        });
        if (cachedCategories.isEmpty()) {
            for (int i = 0; i < defaultCategories.length; i++) {
                tabs.add(tabLayout.newTab().setCustomView(layoutRes).
                        setContentDescription(defaultCategories[i]).setIcon(icons[i]));
            }
            return tabs;
        }
        else {
            return returnTabs(tabLayout,cachedCategories,layoutRes);
        }
    }
    private static List<TabLayout.Tab> returnTabs(TabLayout tabLayout, List<Category> categories, @LayoutRes int layoutRes){
        return addTrendingAndKB(createTabs(tabLayout,mergeCategoriesDrawable(categories,true,true),layoutRes),tabLayout,layoutRes);

    }

    public static List<TabLayout.Tab> createTabs(TabLayout tabLayout,List<Category> categories,@LayoutRes int layoutRes){
        List<TabLayout.Tab> tabs = new ArrayList<>();
        for (Category c : categories) {
            if ("phrases".equalsIgnoreCase(c.name))//currently unicode is good, multiple makemojis does not work.
                continue;
            if (c.drawableRes!=0){
                tabs.add(tabLayout.newTab().setCustomView(layoutRes).
                        setContentDescription(c.name).setIcon(c.drawableRes));
                if ("recent".equals(c.name)){
                    View v = tabs.get(tabs.size()-1).getCustomView().findViewWithTag("iv");
                    if ((v!=null) && v instanceof ImageView)
                        ((ImageView) v).setColorFilter(Moji.resources.getColor(R.color._mm_left_button_cf));
                }
            }
            else if (c.image_url!=null){
                TabLayout.Tab tab = tabLayout.newTab().setCustomView(layoutRes).
                        setContentDescription(c.name).setIcon(R.drawable.mm_placeholder);
                ImageView iv =(ImageView) tab.getCustomView().findViewWithTag("iv");
               if (c.image_url!= null && !c.image_url.isEmpty()) Moji.picasso.load(c.image_url).into(iv);
                tabs.add(tab);

            }
        }
        return tabs;

    }
    public static List<Category> mergeCategoriesDrawable(List<Category> oldCategories,boolean keepOs,boolean keepRecent){
        ListIterator<Category> iterator = oldCategories.listIterator();
        while (iterator.hasNext()){
            Category c = iterator.next();
            if (defaults.containsKey(c.name.toLowerCase())){
                c.drawableRes = defaults.get(c.name.toLowerCase());
            }
            if ("osemoji".equalsIgnoreCase(c.name)){
                c.drawableRes=R.drawable.mm_globe;
                if (!keepOs) iterator.remove();
            }
            if ("recent".equalsIgnoreCase(c.name)){
                c.drawableRes=R.drawable.mm_recent;
                if (!keepRecent)iterator.remove();
            }
        }
        return oldCategories;
    }
    @SuppressWarnings("ConstantConditions")
    private static List<TabLayout.Tab> addTrendingAndKB(List<TabLayout.Tab> tabs, TabLayout tabLayout, @LayoutRes int layoutRes){
        tabs.add(0,tabLayout.newTab().setCustomView(layoutRes).
                setContentDescription(defaultCategories[0]).setIcon(icons[0]));
        tabs.add(tabs.size(),tabLayout.newTab().setCustomView(layoutRes).
                setContentDescription(defaultCategories[defaultCategories.length-1]).setIcon(icons[defaultCategories.length-1]));

        View v = tabs.get(0).getCustomView().findViewWithTag("iv");
        if ((v!=null) && v instanceof ImageView)
            ((ImageView) v).setColorFilter(tabLayout.getContext().getResources().getColor(R.color.mmKBIconColor));
         v = tabs.get(tabs.size()-1).getCustomView().findViewWithTag("iv");
        if ((v!=null) && v instanceof ImageView)
            ((ImageView) v).setColorFilter(tabLayout.getContext().getResources().getColor(R.color.mmKBIconColor));

        return tabs;
    }

}
