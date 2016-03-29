package com.makemoji.keyboard;

import android.support.design.widget.TabLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DouglasW on 3/29/2016.
 */
public class KBCategory {
    private static String [] categories = {"animals","objects","clothing","sports",
            "popculture","politics","hands","food","expression"};
    private static int [] icons = {R.drawable.animals,R.drawable.objects,R.drawable.clothing,R.drawable.sports,
            R.drawable.popculture,R.drawable.politics,R.drawable.hands,R.drawable.food,R.drawable.expression};
    public static List<TabLayout.Tab> getTabs(TabLayout tabLayout){
        List<TabLayout.Tab> tabs = new ArrayList<>();
        for (int i = 0; i<categories.length;i++){
            tabs.add(tabLayout.newTab().setIcon(icons[i])/*.setText(categories[i])*/);
        }
        return tabs;
    }

}
