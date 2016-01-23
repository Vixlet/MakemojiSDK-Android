package com.example.mojilib;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import java.util.Stack;

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

    //just used for measurement
    View topScroller;
    LinearLayout horizontalLayout;

    View trendingButton,flashtagButton,categoriesButton,recentButton,backButton;
    Stack<MakeMojiPage> pages = new Stack<>();

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
        int cameraDrawableRes = a.getResourceId(R.styleable.MojiInputLayout__mm_cameraButtonDrawableAttr,R.drawable.mm_unknown_image);
        int sendLayoutRes = a.getResourceId(R.styleable.MojiInputLayout__mm_sendButtonLayoutAttr,R.layout.mm_default_send_layout);
        boolean cameraVisiblity = a.getBoolean(R.styleable.MojiInputLayout__mm_sendButtonLayoutAttr,true);
        a.recycle();

        inflate(getContext(),R.layout.mm_moji_input_layout,this);
        horizontalLayout = (LinearLayout) findViewById(R.id._mm_horizontal_ll);
        topScroller = findViewById(R.id._mm_horizontal_top_scroller);
        sendLayout = inflate(getContext(),sendLayoutRes,horizontalLayout);
        cameraImageButton = (ImageButton) findViewById(R.id._mm_camera_ib);
        cameraImageButton.setImageResource(cameraDrawableRes);
        if (!cameraVisiblity) cameraImageButton.setVisibility(View.GONE);
        editText = (EditText)findViewById(R.id._mm_edit_text);
        rv = (RecyclerView) findViewById(R.id._mm_recylcer_view);
        SnappyLinearLayoutManager sllm = new SnappyLinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false);
        rv.setLayoutManager(sllm);
        rv.setAdapter(new HorizRVAdapter());
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
        backButton = findViewById(R.id._mm_back_button);
        backButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                popPage();
            }
        });



    }
    void toggleCategoryPage(){
        measureHeight=true;
        hideKeyboard();
        if (categoriesPage.isVisible()) {
            categoriesPage.hide();
        }
        else{
            addPage(categoriesPage);
        }

    }
    void addPage(MakeMojiPage page){
        if (!pages.isEmpty() && pages.peek()!=null)pages.peek().hide();
        pages.push(page);
        page.show();
        setHeight();
        backButton.setVisibility(pages.size()>1?View.VISIBLE:View.GONE);

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

    boolean kbVisible=false;
    int newHeight;
    boolean measureHeight;
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
            setHeight();
        }
    }
    void setHeight(){
        if (!pages.empty())pages.peek().setHeight(newHeight);
        //categoriesPage.setHeight(newHeight);
    }
    /*@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (newHeight!=0)heightMeasureSpec = MeasureSpec.makeMeasureSpec(exactHeight,MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }*/
}
