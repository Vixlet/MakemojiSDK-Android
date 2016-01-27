package com.example.mojilib;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.Nullable;
import android.support.v4.text.TextUtilsCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.example.mojilib.model.MojiModel;

import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Scott Baar on 1/4/2016.
 */
public class MojiInputLayout extends LinearLayout implements ViewTreeObserver.OnGlobalLayoutListener{
    ImageButton cameraImageButton;
    EditText editText;
    View sendLayout;
    RecyclerView rv;
    FrameLayout pageContainer;
    CategoriesPage categoriesPage;
    ViewPagerPage trendingPage;
    ViewPagerPage recentPage;

    //just used for measurement
    ResizeableLL topScroller;
    LinearLayout horizontalLayout;

    View trendingButton,flashtagButton,categoriesButton,recentButton,backButton;
    Stack<MakeMojiPage> pages = new Stack<>();
    TrendingPopulator trendingPopulator;
    SearchPopulator searchPopulator;
    HorizRVAdapter adapter;
    ResizeableLL resizeableLL;
    public interface SendClickListener{
        /**
         * The send layout has been clicked. Returns the raw message and the transformed html
         * @param html the parsed html of the edit text
         * @param spanned, the raw spans in the edit text
         * @return true to clear the input, false to keep it.
         */
        boolean onClick(String html, Spanned spanned);
    }

    public MojiInputLayout(Context context) {
        super(context);
        init(null,0);
    }

    public MojiInputLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs,0);
    }

    public MojiInputLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs,defStyle);
    }
    public void init(AttributeSet attributeSet, int defStyle){
        TypedArray a = getContext().getTheme().obtainStyledAttributes(attributeSet,R.styleable.MojiInputLayout,0,defStyle);
        int cameraDrawableRes = a.getResourceId(R.styleable.MojiInputLayout__mm_cameraButtonDrawableAttr,R.drawable.mm_camera_icon);
        int sendLayoutRes = a.getResourceId(R.styleable.MojiInputLayout__mm_sendButtonLayoutAttr,R.layout.mm_default_send_layout);
        boolean cameraVisiblity = a.getBoolean(R.styleable.MojiInputLayout__mm_sendButtonLayoutAttr,true);
        a.recycle();

        inflate(getContext(),R.layout.mm_moji_input_layout,this);
        horizontalLayout = (LinearLayout) findViewById(R.id._mm_horizontal_ll);
        topScroller = (ResizeableLL)findViewById(R.id._mm_horizontal_top_scroller);
        sendLayout = inflate(getContext(),sendLayoutRes,horizontalLayout);
        cameraImageButton = (ImageButton) findViewById(R.id._mm_camera_ib);
        cameraImageButton.setImageResource(cameraDrawableRes);
        if (!cameraVisiblity) cameraImageButton.setVisibility(View.GONE);
        editText = (EditText)findViewById(R.id._mm_edit_text);
        rv = (RecyclerView) findViewById(R.id._mm_recylcer_view);
        SnappyLinearLayoutManager sllm = new SnappyLinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false);
        rv.setLayoutManager(sllm);
        adapter = new HorizRVAdapter(this,editText.getTextSize());
        rv.setAdapter(adapter);
        pageContainer = (FrameLayout) findViewById(R.id._mm_page_container);
        categoriesPage = new CategoriesPage((ViewStub)findViewById(R.id._mm_stub_cat_page),Moji.mojiApi,this);
        getRootView().getViewTreeObserver().addOnGlobalLayoutListener(this);

        categoriesButton = findViewById(R.id._mm_categories_button);
        flashtagButton = findViewById(R.id._mm_flashtag_button);
        recentButton = findViewById(R.id._mm_recent_button);
        trendingButton = findViewById(R.id._mm_trending_button);
        categoriesButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleCategoryPage();
            }
        });
        trendingButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleTrendingPage();
            }
        });
        backButton = findViewById(R.id._mm_back_button);
        backButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                popPage();
            }
        });

        editText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
               // post(new Runnable() {
                //    @Override
               //     public void run() {
                        layoutRunnable = new Runnable() {
                            @Override
                            public void run() {
                                clearStack();
                            }
                        };
              //      }
          //      });
            }});
        trendingPopulator = new TrendingPopulator();
        trendingPopulator.setup(trendingObserver);
        searchPopulator = new SearchPopulator();
        searchPopulator.setup(searchObserver);
        editText.addTextChangedListener(editTextWatcher);
        flashtagButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setText(TextUtils.concat(editText.getText(),"!"));
                editText.setSelection(editText.length());
                topScroller.snapOpen();
                showKeyboard();
                if (!pages.empty())
                    layoutRunnable = new Runnable() {
                        @Override
                        public void run() {
                            clearStack();
                        }
                    };
            }
        });
    }

    Pattern flashtagPattern = Pattern.compile("(?:.*)!([^\\s])*");
    private TextWatcher editTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            String text = s.toString();
            int selectionEnd = editText.getSelectionEnd();//should probably use this instead of edittext.length()
            if (selectionEnd==-1){
                useTrendingAdapter(true);
                return;
            }
            text = text.substring(0,selectionEnd);//only look at what's before selection
            int lastBang = text.lastIndexOf('!');
            int lastSpace = text.lastIndexOf(' ');
            if (lastSpace==-1) lastSpace = text.lastIndexOf('\n');
            if (lastSpace==-1) lastSpace = text.lastIndexOf('\t');
            if (lastBang==-1 || (lastSpace>0 && lastSpace>lastBang)) {
                useTrendingAdapter(true);//no bang or there's whitespace afterward.
                return;
            }
            String query = text.substring(lastBang+1,selectionEnd);
            if (!query.isEmpty()) {
                useTrendingAdapter(false);
                searchPopulator.search(query);
            }

        }
    };

    boolean usingTrendingAdapter = true;
    void useTrendingAdapter(boolean trending){
        usingTrendingAdapter =trending;
        if (usingTrendingAdapter) {
            adapter.showNames(false);
            adapter.setMojiModels(trendingPopulator.populatePage(200, 0));
        }

    }
    PagerPopulator.PopulatorObserver trendingObserver = new PagerPopulator.PopulatorObserver() {
        @Override
        public void onNewDataAvailable() {
            if (usingTrendingAdapter)adapter.setMojiModels(trendingPopulator.populatePage(200,0));
        }
    };
    PagerPopulator.PopulatorObserver searchObserver = new PagerPopulator.PopulatorObserver() {
        @Override
        public void onNewDataAvailable() {
            if (!usingTrendingAdapter){
                adapter.showNames(true);
                adapter.setMojiModels(searchPopulator.populatePage(50,0));
            }
        }
    };

    void toggleCategoryPage(){
        measureHeight=true;
        hideKeyboard();
        if (categoriesPage.isVisible()) {
            clearStack();
        }
        else
            layoutRunnable = new Runnable() {
                @Override
                public void run() {

                        addPage(categoriesPage);
                }
            };

        if (!keyboardVisible && layoutRunnable!=null){
            layoutRunnable.run();
            layoutRunnable=null;
        }
    }
    void toggleTrendingPage(){
        measureHeight = true;
        hideKeyboard();
        if (trendingPage==null)
            trendingPage = new ViewPagerPage("Trending",this,new TrendingPopulator());

        if (trendingPage.isVisible()){
            clearStack();
        }
        else
            layoutRunnable = new Runnable() {
                @Override
                public void run() {
                        addPage(trendingPage);
                }
            };
        if (!keyboardVisible && layoutRunnable !=null){
            layoutRunnable.run();
            layoutRunnable=null;
        }
    }
    void addPage(MakeMojiPage page){
        if (!pages.isEmpty() && pages.peek()!=null)pages.peek().hide();
        pages.push(page);
        page.show();
        setHeight();
        backButton.setVisibility(pages.size()>1?View.VISIBLE:View.GONE);

    }
    void clearStack(){
        while (!pages.empty()){
            MakeMojiPage page = pages.pop();
            page.hide();
        }
        backButton.setVisibility(View.GONE);
    }
    void popPage(){
        if (pages.size()==0)return;
        MakeMojiPage page = pages.pop();
        MakeMojiPage oldPage = pages.size()>0 ?pages.peek():null;
        page.hide();
        if (oldPage!=null) oldPage.show();
        backButton.setVisibility(pages.size()>1?View.VISIBLE:View.GONE);
    }
    protected ViewGroup getPageFrame(){
        return pageContainer;
    }

    public void hideKeyboard(){
        View view = editText;
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)Moji.getActivity(getContext()).getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    public void showKeyboard(){
        InputMethodManager keyboard = (InputMethodManager)
                Moji.getActivity(getContext()).getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboard.showSoftInput(editText, 0);
    }

    boolean kbVisible=false;
    int newHeight;
    boolean measureHeight;
    Runnable layoutRunnable;
    boolean keyboardVisible;
    @Override
    public void onGlobalLayout() {

        Rect r = new Rect();
        getRootView().getWindowVisibleDisplayFrame(r);

        int screenHeight = getRootView().getHeight();
        int heightDifference = screenHeight - (r.bottom - r.top);
        Log.d("kb","kb h "+ heightDifference + " " + getHeight());
        if (getHeight()!=0 && heightDifference>screenHeight/3) {
            measureHeight=false;
            newHeight = heightDifference -topScroller.getHeight() - horizontalLayout.getHeight();
            Log.d("newh","new h "+ newHeight);
            keyboardVisible=true;
            setHeight();
        }
        else
            keyboardVisible=false;

        if (layoutRunnable!=null)
        {
            layoutRunnable.run();
            layoutRunnable=null;
        }
    }
    void setHeight(){
        if (!pages.empty())pages.peek().setHeight(newHeight);
        //categoriesPage.setHeight(newHeight);
    }

    //remove last search term
    void removeSuggestion(){
        if (usingTrendingAdapter)return;
        int lastBang = editText.getText().toString().lastIndexOf("!");
        if (lastBang==-1)return;
        SpannableStringBuilder ssb = new SpannableStringBuilder(editText.getText());
        editText.setText(ssb.subSequence(0,lastBang));
    }
    void addMojiModel(MojiModel model, @Nullable BitmapDrawable bitmapDrawable){
        SpannableStringBuilder ssb = new SpannableStringBuilder(editText.getText());
        if (model.character!= null && !model.character.isEmpty()){
            ssb.append(model.character);
            editText.setText(ssb);
            editText.setSelection(editText.length());
            return;
        }
        final MojiSpan mojiSpan = MojiSpan.fromModel(model,editText,bitmapDrawable);
        int len = ssb.length();
        ssb.append("\uFFFC");
        ssb.setSpan(mojiSpan, len, ssb.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        if (mojiSpan.getLink()!=null && !mojiSpan.getLink().isEmpty()) {
            if (editText!=null)editText.setHighlightColor(Color.TRANSPARENT);
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    HyperMojiListener hyperMojiListener = (HyperMojiListener) widget.getTag(R.id._makemoji_hypermoji_listener_tag_id);
                    if (hyperMojiListener == null)
                        hyperMojiListener = Moji.getDefaultHyperMojiClickBehavior();
                    hyperMojiListener.onClick(mojiSpan.getLink());
                }
            };
            ssb.setSpan(clickableSpan, len, editText.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (editText!=null)editText.setMovementMethod(LinkMovementMethod.getInstance());
        }
        editText.setText(ssb);
        String html = Moji.toHtml(ssb);
        Moji.subSpanimatable(ssb,editText);
        editText.setSelection(editText.length());
    }
    public int getDefaultSpanDimension(){
        return MojiSpan.getDefaultSpanDimension(editText.getTextSize());
    }
    public void setCameraButtonClickListener(View.OnClickListener onClickListener){
        cameraImageButton.setOnClickListener(onClickListener);
    }
    public void setSendLayoutClickListener(final SendClickListener sendClickListener){
     sendLayout.setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View v) {
             if (sendClickListener!=null){
                 SpannableStringBuilder ssb = new SpannableStringBuilder(editText.getText());
                 String html = Moji.toHtml(ssb);
                 Moji.mojiApi.sendPressed(html);
                 if (sendClickListener.onClick(Moji.toHtml(ssb),ssb))
                     editText.setText("");
             }
         }
     });
    }
    public String getInputAsHtml(){
       return Moji.toHtml(new SpannableStringBuilder(editText.getText()));
    }
    public Spanned getInputAsSpanned(){
        return new SpannableStringBuilder(editText.getText());
    }
}
