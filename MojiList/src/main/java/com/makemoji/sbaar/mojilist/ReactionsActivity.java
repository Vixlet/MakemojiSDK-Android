package com.makemoji.sbaar.mojilist;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import org.json.JSONArray;

import java.util.ArrayList;

public class ReactionsActivity extends AppCompatActivity {

    ListView lv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reactions);
        lv = (ListView)findViewById(R.id.list_view);
        ReactionsAdapter adapter = new ReactionsAdapter(this,new ArrayList<MojiMessage>());
        try {
            JSONArray ja = new JSONArray(Sample.sample1);
            for (int i = 0; i < ja.length(); i++) {
                adapter.add(new MojiMessage(ja.getJSONObject(i)));
            }
        }
        catch (Exception e){}
        lv.setAdapter(adapter);
    }
}
