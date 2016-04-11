package com.makemoji.sbaar.mojilist;

import android.content.Intent;
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

                if (plainTextConversion) {//not needed usually, only to facilitate sharing to 3rd party places legibly
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
        else if (id == R.id.action_kb_activate){
            startActivity(new Intent(this,ActivateActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Handle intents coming from the makemoji keyboard to add them as inline emojis rather than just a picture.
     * If the mojiinputlayout does not handle the intent, the handle it yourself by extracting the image for example.
     * Make sure to have your activity launch mode be singleTop if possible! The incoming intent has to come with the NEW_TASK
     * flag because it is not coming from an activity, but that can be overridden with launchMode="singleTop" in the manifest
     * @param i intent
     */
    @Override
    public void onNewIntent(Intent i){
        super.onNewIntent(i);
        boolean wasMMIntent = mojiInputLayout.handleIntent(i);
    }
}
