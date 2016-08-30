package com.makemoji.mojilib;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.regex.Pattern;

/**
 * Created by Scott Baar on 1/29/2016.
 */
public class MojiEditText extends EditText implements ISpecialInvalidate {
    public static final int DRAWABLE_LEFT = 0;
    public static final int DRAWABLE_TOP = 1;
    public static final int DRAWABLE_RIGHT = 2;
    public static final int DRAWABLE_BOTTOM = 3;
    public static boolean REACT_NATIVE = false;

    boolean mIsJSSettingFocus =false;

    @Override
    public void specialInvalidate() {
        invalidateReflect();
    }

    public interface IDrawableClick{
        void onClick(int drawablePosition );
    }

    public MojiEditText(Context context) {
        super(context);
        init();
    }

    public MojiEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MojiEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    Field mEditor;
    Object editor;
    Method invalidateTextDisplayList;
    static Object[] emptyObject = new Object[]{};
    public void invalidateReflect(){
    if (invalidateTextDisplayList!=null && mEditor!=null && editor!=null)
        try{
            invalidateTextDisplayList.invoke(editor,emptyObject);
        }
        catch (Exception e){
           // e.printStackTrace();

        }
    }
    private void init(){
        try {
            mEditor = getClass().getSuperclass().getSuperclass().getDeclaredField("mEditor");
            mEditor.setAccessible(true);
            Class c  = mEditor.getType();
                    invalidateTextDisplayList = c.getDeclaredMethod("invalidateTextDisplayList");
            invalidateTextDisplayList.setAccessible(true);
            editor = mEditor.get(this);
        }
        catch (Exception e){
           // e.printStackTrace();
        }
        //If any mojispans span less than three characters, remove them because a backspace has happened.
        setImeOptions(getImeOptions()|EditorInfo.IME_FLAG_NO_EXTRACT_UI|EditorInfo.IME_FLAG_NO_FULLSCREEN);
        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String string = s.toString();
                SpannableStringBuilder ssb = new SpannableStringBuilder(s);
                SpannableStringBuilder builder = new SpannableStringBuilder();

                for (int i = 0; i < string.length();) {
                    MojiSpan spanAtPoint[] = ssb.getSpans(i, i + 1, MojiSpan.class);
                    if (spanAtPoint.length == 0) {//not a moji
                        builder.append(s.charAt(i));
                        i++;
                    }
                    else{
                        MojiSpan span = spanAtPoint[0];
                        int start = ssb.getSpanStart(span);
                        int end = ssb.getSpanEnd(span);
                        int spanLength = end-start;
                        if (spanLength==3) {//valid emoji, add it
                            builder.append(ssb.subSequence(start, end));
                        }
                            i+=spanLength;//invalid emoji, skip
                        }
                    }
                if (ssb.length()>builder.length()){//mojis have been deleted
                    int selection = getSelectionStart()-(ssb.length()-builder.length());
                    setText(builder);
                    setSelection(Math.max(0,Math.min(selection,getText().length())));
                }
                }
            });

        /*
        //This fixes spans that have been stripped of their styling by the keyboard. Probably not neccesary anymore.
        addTextChangedListener(new TextWatcher() {
            MojiSpan spans [];
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                spans = new SpannableStringBuilder(s).getSpans(0,s.length(),MojiSpan.class);
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                String string = s.toString();
                SpannableStringBuilder ssb = new SpannableStringBuilder(s);

                int spanCounter = 0;
                int spanLength = spans.length;
                int replaced = 0;
                for (int i = 0; i <string.length() && spanCounter<spanLength;i++){
                    MojiSpan spanAtPoint[] = ssb.getSpans(i,i+1,MojiSpan.class);
                    if (spanAtPoint.length==0 && replacementChar.equals(string.charAt(i))){
                        ssb.setSpan(spans[spanCounter++],i,i+1,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        Log.v("MojiEditText","replacing missing span "+ i);
                        replaced++;
                    }
                }
                if (replaced>0){
                    setText(ssb);
                }

            }
        });
        */

    }


    public static Character replacementChar = "\uFFFC".charAt(0);

    @Override
    public boolean onTextContextMenuItem(int id) {
        int min = 0;
        int max = length();

        if (isFocused()) {
            final int selStart = getSelectionStart();
            final int selEnd = getSelectionEnd();

            min = Math.max(0, Math.min(selStart, selEnd));
            max = Math.max(0, Math.max(selStart, selEnd));
        }

        //convert from html, paste
        if (id == android.R.id.paste) {
            ClipboardManager clipboard =
                    (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = clipboard.getPrimaryClip();
            if (clip==null || clip.getItemCount()==0)return true;
            String paste = clip.getItemAt(0).coerceToText(getContext()).toString();

            ParsedAttributes pa = Moji.parseHtml(paste,this,true,true);

            SpannableStringBuilder original = new SpannableStringBuilder(getText());
            Spanned newText = new SpannableStringBuilder
                    ( TextUtils.concat(original.subSequence(0,min),
                            pa.spanned,
                            original.subSequence(max,original.length())));
            setText(newText);
            setSelection(Math.min(min+pa.spanned.length(),getText().length()));
            Moji.subSpanimatable(newText,this);
            stopActionMode();
            return true;
        }
        //convert to html, copy
        if (id == android.R.id.copy || id == android.R.id.cut) {
            SpannableStringBuilder text = new SpannableStringBuilder(getText().subSequence(min, max));

            Log.d("met copy","met copy " +text.toString());

            String html = Moji.toHtml(text);
            ClipData clip = ClipData.newPlainText(null, html);
            ClipboardManager clipboard = (ClipboardManager) getContext().
                    getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(clip);

            if (id == android.R.id.cut){
                setText(getText().delete(min,max));
                setSelection(Math.min(min,getText().length()));
            }
            stopActionMode();
            return true;
        }
        return super.onTextContextMenuItem(id);
    }
    private void stopActionMode(){
        try{
            Method m = getClass().getSuperclass().getSuperclass().getDeclaredMethod("stopTextActionMode",null);
            m.setAccessible(true);
            m.invoke(this,null);
        }
        catch (Exception e){
            //e.printStackTrace();
        }

    }

    @Override
    public Parcelable onSaveInstanceState()
    {
        Bundle bundle = new Bundle();
        bundle.putParcelable("superState", super.onSaveInstanceState());
        bundle.putString("html",Moji.toHtml(getText()));
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state)
    {
        String html = null;
        if (state instanceof Bundle)
        {
            Bundle bundle = (Bundle) state;
            html = bundle.getString("html",null);
            state = bundle.getParcelable("superState");
        }
        super.onRestoreInstanceState(state);
        if (html ==null) return;
        final String storedHtml = html;
        post(new Runnable() {//let the ui thread handle it when it's free.
            @Override
            public void run() {
               Moji.setText(storedHtml,MojiEditText.this,true,true);
                setSelection(getText().length());
            }
        });

    }
    protected void onSelectionChanged(int selStart, int selEnd) {
       // Log.d("met","selection " + selStart + " " +(selStart %3));
    }
    //react stuff
    @Override
    public void clearFocus() {
        if (REACT_NATIVE)setFocusableInTouchMode(false);
        super.clearFocus();
    }

    @Override
    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        // Always return true if we are already focused. This is used by android in certain places,
        // such as text selection.
        if (!REACT_NATIVE)return super.requestFocus(direction,previouslyFocusedRect);
        if (isFocused()) {
            return true;
        }
        if (!mIsJSSettingFocus) {
            return false;
        }
        setFocusableInTouchMode(true);
        boolean focused = super.requestFocus(direction, previouslyFocusedRect);
        return focused;
    }
    public void requestFocusFromJS() {
        mIsJSSettingFocus = true;
        requestFocus();
        mIsJSSettingFocus = false;
    }


}
