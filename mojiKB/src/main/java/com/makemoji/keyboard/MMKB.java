package com.makemoji.keyboard;


import android.inputmethodservice.InputMethodService;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import java.util.List;


/**
 * Created by DouglasW on 3/29/2016.
 */
public class MMKB extends InputMethodService{

    View inputView;
    String packageName;
    TabLayout tabLayout;
    ViewPager pager;
    @Override public View onCreateInputView() {
        inputView =  getLayoutInflater().inflate(
                R.layout.kb_layout, null);
        tabLayout = (TabLayout)inputView.findViewById(R.id.tabs);
        pager = (ViewPager)inputView.findViewById(R.id.pager);
        List<TabLayout.Tab> tabs = KBCategory.getTabs(tabLayout);
        for (TabLayout.Tab tab: tabs) tabLayout.addTab(tab);
        return inputView;
    }


    @Override public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);
        packageName = attribute.packageName;

    }
}
