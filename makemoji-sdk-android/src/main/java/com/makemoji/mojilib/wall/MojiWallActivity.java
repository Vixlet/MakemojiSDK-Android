package com.makemoji.mojilib.wall;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.makemoji.mojilib.IMojiSelected;
import com.makemoji.mojilib.Moji;
import com.makemoji.mojilib.R;
import com.makemoji.mojilib.model.MojiModel;

import org.json.JSONObject;

/**
 * Created by DouglasW on 4/16/2016.
 */
public class MojiWallActivity extends AppCompatActivity implements IMojiSelected{
    boolean selected =false;
    @Override
    public void onCreate(Bundle bundle){
        super.onCreate(bundle);
        setContentView(R.layout.mm_moji_wall_activity);
    }
    @Override
    public void mojiSelected(MojiModel model, @Nullable BitmapDrawable bd) {
        Intent intent = new Intent();
        JSONObject jo = MojiModel.toJson(model);
        if (jo!=null) {
            intent.putExtra(Moji.EXTRA_JSON, jo.toString());
            setResult(RESULT_OK, intent);
            selected = true;
            finish();
        }
        else{
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    @Override
    public void mojiSelectionCanceled() {
        finish();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if (!selected)
            setResult(RESULT_CANCELED);
    }
}
