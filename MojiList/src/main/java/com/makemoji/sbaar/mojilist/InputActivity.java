package com.makemoji.sbaar.mojilist;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spanned;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.Toast;

import com.makemoji.mojilib.HyperMojiListener;
import com.makemoji.mojilib.Moji;
import com.makemoji.mojilib.MojiEditText;
import com.makemoji.mojilib.MojiInputLayout;

import java.util.ArrayList;

public class InputActivity extends AppCompatActivity {
    MojiEditText outsideMojiEdit;
    MojiInputLayout mojiInputLayout;
    boolean plainTextConversion = false;

    public static final String TAG = "InputActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mojiInputLayout = (MojiInputLayout)findViewById(R.id.mojiInput);
        final MAdapter mAdapter = new MAdapter(this,new ArrayList<MojiMessage>(),true);
        ListView lv = (ListView) findViewById(R.id.list_view);
        outsideMojiEdit = (MojiEditText) findViewById(R.id.outside_met);
        lv.setAdapter(mAdapter);
        mojiInputLayout.setSendLayoutClickListener(new MojiInputLayout.SendClickListener() {
            @Override
            public boolean onClick(String html, Spanned spanned) {
                MojiMessage mojiMessage = new MojiMessage(html);
                mAdapter.add(mojiMessage);

                if (plainTextConversion) {
                    String plainText = Moji.htmlToPlainText(html);
                    String htmlFromPlain = Moji.plainTextToHtml(plainText);
                    Log.d(TAG, "plain text " + plainText);//must convert to html to show new lines
                    MojiMessage message2 = new MojiMessage(plainText);
                    MojiMessage message3 = new MojiMessage(htmlFromPlain);
                    mAdapter.add(message2);
                    mAdapter.add(message3);
                }
                return true;
            }
        });
        mojiInputLayout.setCameraButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(InputActivity.this,"camera clicked",Toast.LENGTH_SHORT).show();
            }
        });
        mojiInputLayout.setHyperMojiClickListener(new HyperMojiListener() {
            @Override
            public void onClick(String url) {
                Toast.makeText(InputActivity.this,"hypermoji clicked from input activity",Toast.LENGTH_SHORT).show();
            }
        });

        //we want the edit text in the moji input layout to show the keyboard on creation, but not when we come back to the activity.
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {

                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED);
            }
        },1000);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_input, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        if (id ==R.id.action_attach){
            mojiInputLayout.attatchMojiEditText(outsideMojiEdit);
            outsideMojiEdit.setVisibility(View.VISIBLE);
            outsideMojiEdit.requestFocus();
            return true;
        }
        else if (id ==R.id.action_detach) {
            mojiInputLayout.detachMojiEditText();
            outsideMojiEdit.setVisibility(View.GONE);
            return true;
        }
        else if (id == R.id.action_plain_conversion){
            plainTextConversion=!plainTextConversion;
        }

        return super.onOptionsItemSelected(item);
    }
}
