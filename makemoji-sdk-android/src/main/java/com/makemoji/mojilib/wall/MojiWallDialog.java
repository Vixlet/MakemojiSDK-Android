package com.makemoji.mojilib.wall;

import android.content.DialogInterface;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialog;

import com.makemoji.mojilib.IMojiSelected;
import com.makemoji.mojilib.R;

/**
 * Created by DouglasW on 4/16/2016.
 */
public class MojiWallDialog {
    public static <A extends AppCompatActivity & IMojiSelected> AppCompatDialog createDialog(final A activity){
        AppCompatDialog dialog = new AppCompatDialog(activity);
        dialog.setContentView(R.layout.mm_moji_wall_activity);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                activity.getSupportFragmentManager().beginTransaction().
                        remove(activity.getSupportFragmentManager().findFragmentById(R.id.mm_moji_wall_frag)).commitAllowingStateLoss();
            }
        });
        return dialog;

    }
}
