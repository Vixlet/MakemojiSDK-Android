package com.makemoji.keyboard;


import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.InputMethodService;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.StringRes;
import android.support.design.widget.TabLayout;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import android.widget.TextView;
import android.widget.Toast;

import com.makemoji.mojilib.BackSpaceDelegate;
import com.makemoji.mojilib.CategoryPopulator;
import com.makemoji.mojilib.Moji;
import com.makemoji.mojilib.MojiGridAdapter;
import com.makemoji.mojilib.MojiSpan;
import com.makemoji.mojilib.OneGridPage;
import com.makemoji.mojilib.PagerPopulator;
import com.makemoji.mojilib.SpacesItemDecoration;
import com.makemoji.mojilib.Spanimator;
import com.makemoji.mojilib.TrendingPopulator;
import com.makemoji.mojilib.model.Category;
import com.makemoji.mojilib.model.MojiModel;
import com.squareup.picasso252.Picasso;
import com.squareup.picasso252.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;


/**
 * Created by Scott Baar on 3/29/2016.
 */
public class MMKB extends InputMethodService implements TabLayout.OnTabSelectedListener,MojiGridAdapter.ClickAndStyler,
        PagerPopulator.PopulatorObserver{

    View inputView;
    String packageName;
    TabLayout tabLayout;
    RecyclerView rv;
    RecyclerView.ItemDecoration itemDecoration;
    PagerPopulator<MojiModel> populator;
    int mojisPerPage;
    MojiGridAdapter adapter;
    TextView heading, shareText;
    static CharSequence shareMessage;
    public static void setShareMessage( CharSequence message) {
        shareMessage = message;
    }




    @Override public View onCreateInputView() {
        inputView =  getLayoutInflater().inflate(
                R.layout.kb_layout, null);
        tabLayout = (TabLayout)inputView.findViewById(R.id.tabs);
        rv = (RecyclerView) inputView.findViewById(R.id.kb_page_grid);
        rv.setLayoutManager(new GridLayoutManager(inputView.getContext(), OneGridPage.ROWS, LinearLayoutManager.HORIZONTAL, false));
        heading = (TextView) inputView.findViewById(R.id.kb_page_heading);
        shareText = (TextView) inputView.findViewById(R.id.share_kb_tv);
        if (shareMessage!=null){
            shareText.setVisibility(View.VISIBLE);
        }
        List<TabLayout.Tab> tabs = KBCategory.getTabs(tabLayout);
        for (TabLayout.Tab tab: tabs) tabLayout.addTab(tab);
        tabLayout.setOnTabSelectedListener(this);

        Runnable backSpaceRunnable = new Runnable() {
            @Override
            public void run() {
                CharSequence selected = getCurrentInputConnection().getSelectedText(InputConnection.GET_TEXT_WITH_STYLES);
                if (selected!=null){
                    getCurrentInputConnection().commitText("",1);
                    return;
                }
                CharSequence text = getCurrentInputConnection().getTextBeforeCursor(2, InputConnection.GET_TEXT_WITH_STYLES);
                int deleteLength =1;
                if (text.length()>1 && Character.isSurrogatePair(text.charAt(0),text.charAt(1)))
                    deleteLength =2;
                getCurrentInputConnection().deleteSurroundingText(deleteLength,0);

            }
        };

        new BackSpaceDelegate(inputView.findViewById(R.id.kb_backspace_button),backSpaceRunnable);
        inputView.findViewById(R.id.kb_abc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showInputMethodPicker();
            }
        });
        inputView.findViewById(R.id.share_kb_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (shareMessage!=null) {
                    getCurrentInputConnection().setComposingText(shareMessage, 1);
                    getCurrentInputConnection().finishComposingText();
                }
            }
        });
        return inputView;
    }


    @Override public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);
        packageName = attribute.packageName;
    }
    boolean firstStart =true;
    @Override
    public void onStartInputView(EditorInfo info, boolean restarting){
        if (firstStart)
            inputView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    onTabSelected(tabLayout.getTabAt(0));
                }
            },10);
        firstStart=false;

    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        heading.setText(tab.getContentDescription());
        if (populator!=null)populator.teardown();
        if ("trending".equals(tab.getContentDescription()))
            populator = new TrendingPopulator();
        else
            populator = new CategoryPopulator(new Category(tab.getContentDescription().toString(),null));
        populator.setup(this);
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }


    @Override
    public void onNewDataAvailable() {

        int h = rv.getHeight();
        int size = h / OneGridPage.ROWS;
        int vSpace = (h - (size * OneGridPage.ROWS)) / OneGridPage.ROWS;
        int hSpace = (rv.getWidth() - (size * 8)) / 16;


        mojisPerPage = Math.max(10, 8 * OneGridPage.ROWS);
        List<MojiModel> models =populator.populatePage(populator.getTotalCount(),0);
        adapter = new MojiGridAdapter(models,this,OneGridPage.ROWS,size);
        adapter.setEnablePulse(false);
        if (itemDecoration!=null) rv.removeItemDecoration(itemDecoration);
        itemDecoration = new SpacesItemDecoration(vSpace, hSpace);
        rv.addItemDecoration(itemDecoration);
        rv.setAdapter(adapter);

        Spanimator.onResume();

    }

    public Target getTarget(final MojiModel model) {
        return new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                FileOutputStream out = null;
                File path = new File(getFilesDir(),"images");
                path.mkdir();
                File cacheFile = new File(path,"share.png");
                try {
                    out = new FileOutputStream(cacheFile.getPath());
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Load failed", Toast.LENGTH_SHORT).show();
                    return;
                } finally {
                    try {
                        if (out != null) {
                            out.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Load failed", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Uri uri = FileProvider.getUriForFile(getContext(),"com.makemoji.keyboard.fileprovider",cacheFile);
                    PackageManager pm = getPackageManager();
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setPackage(packageName);
                    i.putExtra(Moji.EXTRA_MM, true);
                    i.putExtra(Intent.EXTRA_STREAM,uri);
                    i.setData(uri);
                    i.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    i.putExtra(Moji.EXTRA_JSON, MojiModel.toJson(model).toString());
                    i.setType("image/*");
                    List<ResolveInfo> bcs = pm.queryBroadcastReceivers(i,0);
                    List<ResolveInfo> ris = pm.queryIntentActivities(i, PackageManager.MATCH_DEFAULT_ONLY);
                    if (ris.isEmpty()) {
                        Toast.makeText(getContext(), "App does not support sharing images. URL copied to clip board", Toast.LENGTH_LONG).show();
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("MakeMoji emoji", model.image_url);
                        clipboard.setPrimaryClip(clip);
                        return;
                    }
                    i.setPackage(ris.get(0).activityInfo.packageName);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK|Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(i);

                   /* Doesn't work
                   Intent i2 = new Intent(getContext(),BlankActivity.class);
                    i2.putExtra("uri",uri);
                    i2.putExtra("package",ris.get(0).activityInfo.packageName);
                    i2.putExtra(Moji.EXTRA_JSON,MojiModel.toJson(model).toString());
                    i2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i2);*/

                }
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

                Toast.makeText(getContext(), "Load failed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };
    }
    Target t;
    @Override
    public void addMojiModel(MojiModel model, BitmapDrawable d) {
        t = getTarget(model);
        int size = MojiSpan.getDefaultSpanDimension(MojiSpan.BASE_TEXT_PX_SCALED);
        Moji.picasso.load(model.image_url).resize(size,size).into(t);
    }


    @Override
    public Context getContext() {
        return Moji.context;
    }

    @Override
    public int getPhraseBgColor() {
        return getResources().getColor(R.color._mm_default_phrase_bg_color);
    }
}
