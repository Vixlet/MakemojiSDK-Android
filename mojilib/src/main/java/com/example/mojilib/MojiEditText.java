package com.example.mojilib;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import com.example.mojilib.model.MojiModel;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Scott Baar on 1/29/2016.
 */
public class MojiEditText extends EditText {
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

    /**
     * non-stock keyboards clobber moji spans leaving plain [obj] replacement chars, particularly when typing in the middle
     * of mojispans. Disabling suggestions is usually enough to fix the problem. The text watcher is a redundant fail safe. Either one
     * *should* fix the problem. If problem persists, add code to remove orphaned [obj] chars after restoring spans.
     */
    private void init(){
        //setInputType(getInputType()|InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS|InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD|InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        setImeOptions(getImeOptions()|EditorInfo.IME_FLAG_NO_EXTRACT_UI|EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD|EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE);
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

    }


    static Pattern srcPattern = Pattern.compile("(?:.*)(?:src=\")(.*?)(?:\")(?:.*)");
    static Pattern linkPattern = Pattern.compile("(?:.*)(?:link=\")(.*?)(?:\")(?:.*)");
    static Pattern idPattern = Pattern.compile("(?:.*)(?:id=\")(.*?)(?:\")(?:.*)");
    Character replacementChar = "\uFFFC".charAt(0);

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

        //replace image tags with replacement chars, then insert emojis over them.
        if (id == android.R.id.paste) {
            ClipboardManager clipboard =
                    (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = clipboard.getPrimaryClip();
            if (clip==null || clip.getItemCount()==0)return true;
            String paste = clip.getItemAt(0).coerceToText(getContext()).toString();
            String matchString = "<img style=";

            List<MojiSpan> spansToInsert = new ArrayList<>();

            int index = paste.indexOf(matchString);
            int end = paste.indexOf('>',index);

            while (index!=-1 && end !=-1){
                String subString = paste.substring(index,end);
                String src = null, link =null, idString =null;
                Matcher m = srcPattern.matcher(subString);
                if (m.find()) src = m.group(1);
                m = linkPattern.matcher(subString);
                if (m.find()) link = m.group(1);
                m = idPattern.matcher(subString);
                if (m.find()) idString = m.group(1);
                if (src!=null){
                    MojiModel model = new MojiModel();
                    model.image_url = src;
                    model.link_url = link;
                    try{
                        model.id = Integer.valueOf(idString);
                    }catch (Exception e){/**/}
                    spansToInsert.add(new MojiSpan(Moji.resources.getDrawable(R.drawable.mm_placeholder),
                            src,MojiSpan.DEFAULT_INCOMING_IMG_WH,MojiSpan.DEFAULT_INCOMING_IMG_WH,
                            MojiSpan.DEFAULT_INCOMING_FONT_PT, true,link,this));
                   paste = paste.replaceFirst("<img (.*?)>","\uFFFC");
                }
                else

                index = paste.indexOf(matchString,index);
                end = paste.indexOf('>',index);

            }

            int spanCounter = 0;
            int spanLength = spansToInsert.size();
            SpannableStringBuilder ssb = new SpannableStringBuilder(paste);
            SpannableStringBuilder pastedSpan = new SpannableStringBuilder();
            for (int i = 0; i<ssb.length() && spanCounter<spanLength;i++){
                if (replacementChar.equals((paste.charAt(i)))) {
                    pastedSpan.append(replacementChar);
                    final MojiSpan span = spansToInsert.get(spanCounter++);
                    pastedSpan.setSpan(span, pastedSpan.length()-1, pastedSpan.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    if (span.getLink() != null && !span.getLink().isEmpty()) {
                        ClickableSpan clickableSpan = new ClickableSpan() {
                            @Override
                            public void onClick(View widget) {
                                HyperMojiListener hyperMojiListener = (HyperMojiListener) widget.getTag(R.id._makemoji_hypermoji_listener_tag_id);
                                if (hyperMojiListener == null)
                                    hyperMojiListener = Moji.getDefaultHyperMojiClickBehavior();
                                hyperMojiListener.onClick(span.getLink());
                            }
                        };
                        pastedSpan.setSpan(clickableSpan, pastedSpan.length()-1, pastedSpan.length(),
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
                else{
                    pastedSpan.append(paste.charAt(i));
                }
            }
            SpannableStringBuilder original = new SpannableStringBuilder(getText());
            Spanned newText = new SpannableStringBuilder
                    ( TextUtils.concat(original.subSequence(0,min),pastedSpan,original.subSequence(max,original.length())));
            setText(newText);
            setSelection(Math.min(min+paste.length(),newText.length()));
            Moji.subSpanimatable(newText,this);
            stopActionMode();
            return true;
        }
        if (id == android.R.id.copy) {
            SpannableStringBuilder text = new SpannableStringBuilder(getText().subSequence(min, max));
            StringBuilder sb = new StringBuilder(text.toString());
            MojiSpan[] spans = text.getSpans(0,text.length()    , MojiSpan.class);

            int spanCounter = 0;
            int spanLength = spans.length;

            //replace mojispans with their html representation
            while ((sb.indexOf("\uFFFC") != -1) && spanCounter < spanLength) {
                sb.replace(sb.indexOf("\uFFFC"), sb.indexOf("\uFFFC") + 1, spans[spanCounter++].toHtml());
            }
            ClipData clip = ClipData.newPlainText(null, sb);
            ClipboardManager clipboard = (ClipboardManager) getContext().
                    getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(clip);

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
}
