package com.makemoji.keyboard;

import android.support.design.widget.TabLayout;

import java.util.ArrayList;
import java.util.List;
/**
 * Created by Scott Baar on 3/29/2016.
 */
public class KBCategory {
    private static String [] categories = {"trending","animals","clothing","expression","food",
            "hands","objects","politics","pop culture","sports","keyboard"};
    private static int [] icons = {R.drawable.mm_trending,R.drawable.mm_animals,R.drawable.mm_clothing,R.drawable.mm_expression,R.drawable.mm_food,
            R.drawable.mm_hands,R.drawable.mm_objects,R.drawable.mm_politics,R.drawable.mm_popculture,R.drawable.mm_sports,R.drawable.mm_keyboard};
    public static List<TabLayout.Tab> getTabs(TabLayout tabLayout){
        List<TabLayout.Tab> tabs = new ArrayList<>();
        for (int i = 0; i<categories.length;i++){
            tabs.add(tabLayout.newTab().setCustomView(R.layout.kb_tab).
                    setContentDescription(categories[i]).setIcon(icons[i])/*.setText(categories[i])*/);
        }
        return tabs;
    }

}
