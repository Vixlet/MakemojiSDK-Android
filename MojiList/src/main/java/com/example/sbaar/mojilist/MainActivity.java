package com.example.sbaar.mojilist;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.squareup.picasso252.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static Resources resources;
    public static Context context;
    public static Picasso picasso;
    MAdapter adapter;
    public boolean simple=true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resources = getResources();
        context =this;
        Picasso.Builder builder = new Picasso.Builder(MainActivity.context);
        picasso = builder.build();
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        adapter = new MAdapter(this,new ArrayList<MojiMessage>(), simple);
        final ListView listView = (ListView)findViewById(R.id.list_view);
try {
    JSONArray ja = new JSONArray(Sample.sample1);
    for (int i = 0; i < ja.length(); i++) {
        adapter.add(new MojiMessage(ja.getJSONObject(i)));
    }
    ja = new JSONArray(Sample.sample2);
    for (int i = 0; i < ja.length(); i++) {
        adapter.add(new MojiMessage(ja.getJSONObject(i)));
    }
    ja = new JSONArray(Sample.sample3);
    for (int i = 0; i < ja.length(); i++) {
        adapter.add(new MojiMessage(ja.getJSONObject(i)));
    }
    InputStream is = getResources().openRawResource(R.raw.newsample);
    BufferedReader streamReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
    StringBuilder responseStrBuilder = new StringBuilder();

    String inputStr;
    while ((inputStr = streamReader.readLine()) != null)
        responseStrBuilder.append(inputStr);
    ja = new JSONArray(responseStrBuilder.toString());
    for (int i = 0; i < ja.length(); i++) {
        adapter.add(new MojiMessage(ja.getJSONObject(i)));
    }
    listView.setAdapter(adapter);
}
catch (Exception e){
    Log.e("Main","json error "+ e.getLocalizedMessage());
    e.printStackTrace();
}
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                listView.invalidate();
            }
        },2000);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        if (id ==R.id.action_size_up){
            adapter.changeTextSize(2f);
            return true;
        }
        else if (id ==R.id.action_size_down){
            adapter.changeTextSize(-2f);
            return true;
        }
        else if (id == R.id.action_toggle_simple){
            simple=!simple;
            adapter.setSimple(simple);
        }

        return super.onOptionsItemSelected(item);
    }
}
