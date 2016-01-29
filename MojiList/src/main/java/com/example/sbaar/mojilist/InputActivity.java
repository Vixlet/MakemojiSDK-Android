package com.example.sbaar.mojilist;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spanned;
import android.view.View;
import android.widget.ListView;

import com.example.mojilib.MojiInputLayout;

import java.util.ArrayList;

public class InputActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        MojiInputLayout mojiInputLayout = (MojiInputLayout)findViewById(R.id.mojiInput);
        final MAdapter mAdapter = new MAdapter(this,new ArrayList<MojiMessage>(),true);
        ListView lv = (ListView) findViewById(R.id.list_view);
        lv.setAdapter(mAdapter);
        mojiInputLayout.setSendLayoutClickListener(new MojiInputLayout.SendClickListener() {
            @Override
            public boolean onClick(String html, Spanned spanned) {
                MojiMessage mojiMessage = new MojiMessage(html);
                mAdapter.add(mojiMessage);
                return true;
            }
        });


    }

}
