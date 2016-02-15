package com.makemoji.mojilib;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.makemoji.mojilib.model.MojiModel;

import java.util.Stack;

/**
 * To switch between emoji pages that paginate or that are one cotinuous list, change new OneGridPage(...) to ViewPagerPage
 * in toggleTrending, toggleRecent, and in CategoriesPage onclick
 *
 * Customize colors and properties by applying a style in xml inheriting from MojiInputLayoutDefaultStyle. To customize the editText's appearance
 * set a default "android:editTextStyle" in a theme then apply that theme to this view in xml.
 *
 * Created by Scott Baar on 1/4/2016.
 */
public class MojiInputLayout extends LinearLayout implements ViewTreeObserver.OnGlobalLayoutListener{
    ImageButton cameraImageButton;
    EditText editText;
    View sendLayout;
    RecyclerView rv;
    FrameLayout pageContainer;
    CategoriesPage categoriesPage;
    MakeMojiPage trendingPage;
    MakeMojiPage recentPage;

    //just used for measurement
    ResizeableLL topScroller;
    LinearLayout horizontalLayout;

    ImageView trendingButton,flashtagButton,categoriesButton,recentButton,backButton;
    Stack<MakeMojiPage> pages = new Stack<>();
    TrendingPopulator trendingPopulator;
    SearchPopulator searchPopulator;
    HorizRVAdapter adapter;

    @ColorInt int headerTextColor;
    @DrawableRes int backSpaceDrawableRes;
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
        TypedArray a = getContext().getTheme().obtainStyledAttributes(attributeSet,R.styleable.MojiInputLayout,0,R.style.MojiInputLayoutDefaultStyle);
        int cameraDrawableRes = a.getResourceId(R.styleable.MojiInputLayout__mm_cameraButtonDrawable,R.drawable.mm_camera_icon);
        backSpaceDrawableRes = a.getResourceId(R.styleable.MojiInputLayout__mm_backSpaceButtonDrawable,R.drawable.mm_backspace_grey600_24dp);
        int sendLayoutRes = a.getResourceId(R.styleable.MojiInputLayout__mm_sendButtonLayout,R.layout.mm_default_send_layout);
        boolean cameraVisibility = a.getBoolean(R.styleable.MojiInputLayout__mm_cameraButtonVisible,true);
        int buttonColor = a.getColor(R.styleable.MojiInputLayout__mm_leftButtonColor,ContextCompat.getColor(getContext(),R.color._mm_left_button_cf));
        Drawable buttonBg = a.getDrawable(R.styleable.MojiInputLayout__mm_leftButtonBg);
        headerTextColor = a.getColor(R.styleable.MojiInputLayout__mm_headerTextColor,ContextCompat.getColor(getContext(),R.color._mm_header_text_color));
        Drawable leftContainerDrawable = a.getDrawable(R.styleable.MojiInputLayout__mm_leftContainerDrawable);
        @ColorInt int mainBgColor = a.getColor(R.styleable.MojiInputLayout__mm_mainBgColor,ContextCompat.getColor(getContext(),R.color._mm_top_layout_bg));
        a.recycle();

        inflate(getContext(),R.layout.mm_moji_input_layout,this);
        getChildAt(0).setBackgroundColor(mainBgColor);
        horizontalLayout = (LinearLayout) findViewById(R.id._mm_horizontal_ll);
        topScroller = (ResizeableLL)findViewById(R.id._mm_horizontal_top_scroller);
        sendLayout = ((LinearLayout)inflate(getContext(),sendLayoutRes,horizontalLayout)).getChildAt(2);
        findViewById(R.id._mm_left_buttons).setBackgroundDrawable(leftContainerDrawable);

        cameraImageButton = (ImageButton) findViewById(R.id._mm_camera_ib);
        cameraImageButton.setImageResource(cameraDrawableRes);
        if (!cameraVisibility) cameraImageButton.setVisibility(View.GONE);

        editText = (EditText)findViewById(R.id._mm_edit_text);

        rv = (RecyclerView) findViewById(R.id._mm_recylcer_view);
        rv.setBackgroundColor(mainBgColor);
        LinearLayoutManager llm = new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false);
        rv.setLayoutManager(llm);
        adapter = new HorizRVAdapter(this);
        rv.setAdapter(adapter);
        pageContainer = (FrameLayout) findViewById(R.id._mm_page_container);
        categoriesPage = new CategoriesPage((ViewStub)findViewById(R.id._mm_stub_cat_page),Moji.mojiApi,this);
        getRootView().getViewTreeObserver().addOnGlobalLayoutListener(this);

        categoriesButton =(ImageView) findViewById(R.id._mm_categories_button);
        flashtagButton =(ImageView) findViewById(R.id._mm_flashtag_button);
        recentButton = (ImageView)findViewById(R.id._mm_recent_button);
        trendingButton = (ImageView)findViewById(R.id._mm_trending_button);
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
        recentButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleRecentPage();
            }
        });
        backButton =(ImageView) findViewById(R.id._mm_back_button);
        backButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                popPage();
            }
        });

        //don't show kb if pages are showing.
        editText.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (pages.empty()) return editText.onTouchEvent(event);
                else
                {
                    int pos = editText.getOffsetForPosition(event.getX(),event.getY());
                    editText.setSelection(pos);
                    /*if (event.getAction()== MotionEvent.ACTION_UP){
                        ClickableSpan spans[] =editText.getText().getSpans(pos,pos,ClickableSpan.class);
                        if (spans.length>0)spans[0].onClick(editText);
                    }*/
                    return true;
                }
            }
        });


        trendingPopulator = new TrendingPopulator();
        trendingPopulator.setup(trendingObserver);
        searchPopulator = new SearchPopulator();
        searchPopulator.setup(searchObserver);
        editText.addTextChangedListener(editTextWatcher);
        flashtagButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                deactiveButtons();
                editText.getText().insert(Math.max(0,editText.getSelectionStart()),"!");
                topScroller.snapOpen();
                showKeyboard();
                    layoutRunnable = new Runnable() {
                        @Override
                        public void run() {
                            clearStack();
                        }
                    };
            }
        });
        flashtagButton.setBackgroundDrawable(buttonBg.getConstantState().newDrawable());
        recentButton.setBackgroundDrawable(buttonBg.getConstantState().newDrawable());
        trendingButton.setBackgroundDrawable(buttonBg.getConstantState().newDrawable());
        categoriesButton.setBackgroundDrawable(buttonBg.getConstantState().newDrawable());

        flashtagButton.setColorFilter(buttonColor);
        recentButton.setColorFilter(buttonColor);
        trendingButton.setColorFilter(buttonColor);
        categoriesButton.setColorFilter(buttonColor);
    }
    @ColorInt int getHeaderTextColor(){
        return headerTextColor;
    }

    private TextWatcher editTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            final String t = s.toString();
            sendLayout.setEnabled(t.length()>0);

            int selectionEnd = editText.getSelectionEnd();//should probably use this instead of edittext.length()
            if (selectionEnd==-1){
                useTrendingAdapter(true);
                return;
            }
            String text = t.substring(0,selectionEnd);//only look at what's before selection
            int lastBang = text.lastIndexOf('!');
            int lastSpace = text.lastIndexOf(' ');
            if (lastSpace==-1) lastSpace = text.lastIndexOf('\n');
            if (lastSpace==-1) lastSpace = text.lastIndexOf('\t');
            if (lastBang==-1 || (lastSpace>0 && lastSpace>lastBang)) {
                useTrendingAdapter(true);//no bang or there's whitespace afterward.
                return;
            }
            String query = text.substring(lastBang+1,selectionEnd);
                useTrendingAdapter(false);
                searchPopulator.search(query);


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

    OnClickListener abcClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            layoutRunnable = new Runnable() {
                @Override
                public void run() {
                    clearStack();
                }
            };
            //postDelayed(layoutRunnable,65);
            //layoutRunnable.run();
            //layoutRunnable=null;
            showKeyboard();
        }
    };
    OnClickListener backspaceClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            SpannableStringBuilder ssb = new SpannableStringBuilder(editText.getText());
            int selectionStart = Math.max(editText.getSelectionStart()-1,0);
            int selectionEnd = Math.min(selectionStart+1,editText.length());
            if (selectionEnd==0)return;
            ssb.delete(selectionStart,selectionEnd);
            editText.setText(ssb);
            editText.setSelection(Math.max(0,selectionStart));
        }
    };

    OnTouchListener backspaceTouchListener = new OnTouchListener() {
        boolean isPressed;
        private Runnable backSpaceRunnable = new Runnable() {
            @Override
            public void run() {
                if (isPressed){
                    backspaceClick.onClick(null);
                    postDelayed(this,110);
                }
            }
        };
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    isPressed = true;
                    postDelayed(backSpaceRunnable,200);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    isPressed = false;

            }
            return false;
        }
    };

    void deactiveButtons(){
        categoriesButton.setActivated(false);
        recentButton.setActivated(false);
        trendingButton.setActivated(false);
    }

    void toggleCategoryPage(){
        measureHeight=true;
        hideKeyboard();
        deactiveButtons();
        if (categoriesPage.isVisible()) {
            onLeftClosed();
        }
        else
            layoutRunnable = new Runnable() {
                @Override
                public void run() {
                        categoriesButton.setActivated(true);
                        clearStack();
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
        deactiveButtons();
        if (trendingPage==null)
            trendingPage = new OneGridPage("Trending",this,new TrendingPopulator());

        if (trendingPage.isVisible()){
            onLeftClosed();
        }
        else
            layoutRunnable = new Runnable() {
                @Override
                public void run() {
                    trendingButton.setActivated(true);
                    clearStack();
                        addPage(trendingPage);
                }
            };
        if (!keyboardVisible && layoutRunnable !=null){
            layoutRunnable.run();
            layoutRunnable=null;
        }
    }
    void toggleRecentPage(){
        measureHeight = true;
        hideKeyboard();
        deactiveButtons();
        if (recentPage==null)
            recentPage = new OneGridPage("Recent",this,new RecentPopulator());

        if (recentPage.isVisible()){
            onLeftClosed();
        }
        else
            layoutRunnable = new Runnable() {
                @Override
                public void run() {
                    clearStack();
                    recentButton.setActivated(true);
                    addPage(recentPage);
                    recentPage.onNewDataAvailable();
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
        getPageFrame().setVisibility(View.GONE);
    }
    void popPage(){
        if (pages.size()==0)return;
        MakeMojiPage page = pages.pop();
        MakeMojiPage oldPage = pages.size()>0 ?pages.peek():null;
        page.hide();
        if (oldPage!=null) oldPage.show();
        backButton.setVisibility(pages.size()>1?View.VISIBLE:View.GONE);
        if (pages.empty())getPageFrame().setVisibility(View.GONE);
    }
    protected ViewGroup getPageFrame(){
        return pageContainer;
    }

    void hideKeyboard(){
        View view = editText;
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)Moji.getActivity(getContext()).getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    void showKeyboard(){
        InputMethodManager keyboard = (InputMethodManager)
                Moji.getActivity(getContext()).getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboard.showSoftInput(editText, 0);
    }

    boolean kbVisible=false;
    boolean measureHeight;
    Runnable layoutRunnable;
    boolean keyboardVisible;
    int oldh,oldtop,oldDiff;
    int maxTopScrollherH;
    @Override
    public void onGlobalLayout() {

        Rect r = new Rect();
        getRootView().getWindowVisibleDisplayFrame(r);

        int screenHeight = getRootView().getHeight();
        int heightDifference = screenHeight - (r.bottom - r.top);
        //Log.d("kb","kb h "+ heightDifference + " " + getHeight());
        if (getHeight()!=0 && heightDifference>screenHeight/3) {
            measureHeight=false;
            //newHeight = heightDifference - topScroller.getHeight();// -topScroller.getHeight() - horizontalLayout.getHeight();
           // Log.d("newh","new h "+ newHeight);
            keyboardVisible=true;
            oldtop=getTop()-r.bottom;
            maxTopScrollherH = Math.max(topScroller.getHeight(),maxTopScrollherH);
            int parentHeight = ((ViewGroup)getParent()).getHeight()
                    - editText.getPaddingBottom()-editText.getPaddingTop() -(int) (20 * Moji.density);
            editText.setMaxHeight(parentHeight -maxTopScrollherH);
        }
        else {
            keyboardVisible = false;
           // Log.d("newh", "kb not visible " + heightDifference );
        }

        if (layoutRunnable!=null && oldDiff!=heightDifference)
        {
            layoutRunnable.run();
            layoutRunnable=null;

        }

        oldDiff = heightDifference;
    }
    void setHeight() {
        getPageFrame().setVisibility(View.VISIBLE);
       /* if (!pages.empty())pages.peek().setHeight(newHeight);
        else
        {
           // topScroller.getLayoutParams().height=newHeight;
        }
        */
    }

    //remove last search term
    void removeSuggestion(){
        if (usingTrendingAdapter|| editText.getSelectionStart()==-1)return;
        int selectionStart = editText.getSelectionStart();
        int lastBang = editText.getText().toString().substring(0,editText.getSelectionStart()).lastIndexOf("!");
        if (lastBang==-1)return;
        SpannableStringBuilder ssb = new SpannableStringBuilder(editText.getText());
        ssb.delete(lastBang,selectionStart);
        editText.setText(ssb);
        editText.setSelection(Math.min(lastBang,ssb.length()));
    }
    void addMojiModel(MojiModel model, @Nullable BitmapDrawable bitmapDrawable){
        SpannableStringBuilder ssb = new SpannableStringBuilder(editText.getText());
        int selectionStart = editText.getSelectionStart();
        if (selectionStart==-1)selectionStart = editText.length();
        if (model.character!= null && !model.character.isEmpty()){
            ssb.insert(selectionStart,model.character);
            editText.setText(ssb);
            editText.setSelection(Math.min(selectionStart+model.character.length(),ssb.length()));
            return;
        }
        final MojiSpan mojiSpan = MojiSpan.fromModel(model,editText,bitmapDrawable);
        ssb.insert(selectionStart,"\uFFFC");
        ssb.setSpan(mojiSpan, selectionStart,selectionStart+1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        if (mojiSpan.getLink()!=null && !mojiSpan.getLink().isEmpty()) {
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    HyperMojiListener hyperMojiListener = (HyperMojiListener) widget.getTag(R.id._makemoji_hypermoji_listener_tag_id);
                    if (hyperMojiListener == null)
                        hyperMojiListener = Moji.getDefaultHyperMojiClickBehavior();
                    hyperMojiListener.onClick(mojiSpan.getLink());
                }
            };
            ssb.setSpan(clickableSpan, selectionStart, selectionStart+1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            editText.setMovementMethod(LinkMovementMethod.getInstance());
        }
        editText.setText(ssb);
        Moji.subSpanimatable(ssb,editText);
        editText.setSelection(selectionStart+1);
    }

    void onLeftClosed(){
        showKeyboard();
        deactiveButtons();
        layoutRunnable = new Runnable() {
            @Override
            public void run() {

                clearStack();
            }
        };

    }
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Mojilytics.forceSend();
    }

    public String getInputAsHtml(){
       return Moji.toHtml(new SpannableStringBuilder(editText.getText()));
    }
    public Spanned getInputAsSpanned(){
        return new SpannableStringBuilder(editText.getText());
    }

    public void setInputText(CharSequence cs){
        editText.setText(cs);
    }

    /**
     * the action when a hypermoji is clicked in the edit text.
     * @param hml
     */
    public void setHyperMojiClickListener(HyperMojiListener hml){
        editText.setTag(R.id._makemoji_hypermoji_listener_tag_id,hml);
    }

    public void setCameraButtonClickListener(View.OnClickListener onClickListener){
        cameraImageButton.setOnClickListener(onClickListener);
    }
    public void setCameraVisibility(boolean visible){
        cameraImageButton.setVisibility(visible?View.VISIBLE:View.GONE);
    }
    public void setSendLayoutClickListener(final SendClickListener sendClickListener){
        sendLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sendClickListener!=null){
                    SpannableStringBuilder ssb = new SpannableStringBuilder(editText.getText());
                    MojiSpan [] spans = ssb.getSpans(0,ssb.length(),MojiSpan.class);
                    for (int i = 0; i< spans.length; i++){
                        MojiModel model = new MojiModel(spans[i].name,spans[i].getSource());
                        model.link_url = spans[i].getLink();
                        RecentPopulator.addRecent(model);
                    }
                    String html = Moji.toHtml(ssb);
                    Moji.mojiApi.sendPressed(html);
                    if (sendClickListener.onClick(Moji.toHtml(ssb),ssb))
                        editText.setText("");
                }
            }
        });
    }
}
