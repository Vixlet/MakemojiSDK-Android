package com.makemoji.sbaar.mojilist;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spanned;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
import android.widget.Toast;

import com.makemoji.keyboard.MMKB;
import com.makemoji.mojilib.HyperMojiListener;
import com.makemoji.mojilib.IMojiSelected;
import com.makemoji.mojilib.Moji;
import com.makemoji.mojilib.MojiEditText;
import com.makemoji.mojilib.MojiInputLayout;
import com.makemoji.mojilib.MojiUnlock;
import com.makemoji.mojilib.model.MojiModel;
import com.makemoji.mojilib.wall.MojiWallActivity;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;

public class InputActivity extends AppCompatActivity{
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
        //mojiInputLayout.grabFocusShowKb(); //show keyboard on demand; launch behavior defined in mojiInputLayout style xml


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
        mojiInputLayout.setLockedCategoryClicked(new MojiUnlock.ILockedCategoryClicked() {
            @Override
            public void onClick(String name) {
                lockedCategoryClick(name);
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
            mojiInputLayout.manualSaveInputToRecentsAndBackend();//send text for analytics, add emojis to recent.
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
        else if (id == R.id.action_emoji_wall_activity){
            Intent intent = new Intent(this, MojiWallActivity.class);
            //intent.putExtra(MojiWallActivity.EXTRA_THEME,R.style.MojiWallDefaultStyle_Light); //to theme it
            intent.putExtra(MojiWallActivity.EXTRA_SHOWRECENT,true);
            intent.putExtra(MojiWallActivity.EXTRA_SHOWUNICODE,true);
            startActivityForResult(intent,IMojiSelected.REQUEST_MOJI_MODEL);
        }
        else if (id == R.id.action_clear_unlocks){
            MojiUnlock.clearGroups();
            mojiInputLayout.refreshCategories();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Handle intents coming from the makemoji mm_keyboard to add them as inline emojis rather than just a picture.
     * If the mojiinputlayout does not handle the intent, the handle it yourself by extracting the image for example.
     * Make sure to have your activity launch mode be singleTop if possible! The incoming intent has to come with the NEW_TASK
     * flag because it is not coming from an activity, but that can be overridden with launchMode="singleTop" in the manifest
     * @param i intent
     */
    @Override
    public void onNewIntent(Intent i){
        super.onNewIntent(i);
        if (mojiInputLayout.handleIntent(i))
            return;
        if (Moji.ACTION_LOCKED_CATEGORY_CLICK.equals(i.getAction())){
            lockedCategoryClick(i.getStringExtra(Moji.EXTRA_CATEGORY_NAME));
        }
    }

    public void lockedCategoryClick(String name){
        MojiUnlock.unlockCategory(name);
        mojiInputLayout.refreshCategories();

    }

    //get the result from emoji wall activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        //mojiInputLayout.handleIntent(data);
        //OR
        if (requestCode == IMojiSelected.REQUEST_MOJI_MODEL && resultCode== RESULT_OK){
            try{
                String json = data.getStringExtra(Moji.EXTRA_JSON);
                MojiModel model = MojiModel.fromJson(new JSONObject(json));
                mojiInputLayout.addMojiModel(model,null);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed(){
        if (mojiInputLayout.canHandleBack()){
            mojiInputLayout.onBackPressed();
            return;
        }
        super.onBackPressed();
    }


}
