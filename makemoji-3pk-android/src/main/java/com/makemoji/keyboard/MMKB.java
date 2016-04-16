package com.makemoji.keyboard;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.method.MetaKeyKeyListener;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import android.widget.TextView;
import android.widget.Toast;

import com.makemoji.mojilib.BackSpaceDelegate;
import com.makemoji.mojilib.CategoryPopulator;
import com.makemoji.mojilib.Moji;
import com.makemoji.mojilib.MojiGridAdapter;
import com.makemoji.mojilib.MojiInputLayout;
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
import java.util.ArrayList;
import java.util.List;

public class MMKB extends InputMethodService
        implements KeyboardView.OnKeyboardActionListener, TabLayout.OnTabSelectedListener,MojiGridAdapter.ClickAndStyler,
        PagerPopulator.PopulatorObserver,KBCategory.KBTAbListener {
    static final boolean DEBUG = false;

    /**
     * This boolean indicates the optional example code for performing
     * processing of hard keys in addition to regular text generation
     * from on-screen interaction.  It would be used for input methods that
     * perform language translations (such as converting text entered on
     * a QWERTY keyboard to Chinese), but may not be used for input methods
     * that are primarily intended to be used for on-screen text entry.
     */
    static final boolean PROCESS_HARD_KEYS = true;
    private InputMethodManager mInputMethodManager;
    private LatinKeyboardView mInputView;
    private CandidateView mCandidateView;
    private CompletionInfo[] mCompletions;

    private StringBuilder mComposing = new StringBuilder();
    private boolean mPredictionOn;
    private boolean mCompletionOn;
    private int mLastDisplayWidth;
    private boolean mCapsLock;
    private long mLastShiftTime;
    private long mMetaState;

    private LatinKeyboard mSymbolsKeyboard;
    private LatinKeyboard mSymbolsShiftedKeyboard;
    private LatinKeyboard mQwertyKeyboard;

    private LatinKeyboard mCurKeyboard;

    private String mWordSeparators;


    View inputView;
    String packageName;
    TabLayout tabLayout;
    RecyclerView rv;
    RecyclerView.ItemDecoration itemDecoration;
    PagerPopulator<MojiModel> populator;
    int mojisPerPage;
    MojiGridAdapter adapter;
    TextView heading, shareText;
    View pageFrame;
    static CharSequence shareMessage;

    /**
     * Main initialization of the input method component.  Be sure to call
     * to super class.
     */
    @Override public void onCreate() {
        super.onCreate();
        mInputMethodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        mWordSeparators = getResources().getString(R.string.word_separators);
    }

    /**
     * This is the point where you can do all of your UI initialization.  It
     * is called after creation and any configuration change.
     */
    @Override public void onInitializeInterface() {
        if (mQwertyKeyboard != null) {
            // Configuration changes can happen after the keyboard gets recreated,
            // so we need to be able to re-build the keyboards if the available
            // space has changed.
            int displayWidth = getMaxWidth();
            if (displayWidth == mLastDisplayWidth) return;
            mLastDisplayWidth = displayWidth;
        }
        mQwertyKeyboard = new LatinKeyboard(this, R.xml.qwerty);
        mSymbolsKeyboard = new LatinKeyboard(this, R.xml.symbols);
        mSymbolsShiftedKeyboard = new LatinKeyboard(this, R.xml.symbols_shift);
    }


    //if this is not the sample app but is using the sample authority, don't allow it, or else it will stop other apps from installing.
    public void assertAuthorityChanged(){
        if (!"com.makemoji.sbaar.mojilist".equals(getContext().getApplicationInfo().packageName)
                && "com.makemoji.keyboard.fileprovider".equals(getContext().getResources().getString(R.string._mm_provider_authority))){
            throw new IllegalStateException("You must override _mm_provider_authority in strings.xml with a unique package name!! com.your.name.kbfileprovider ");
        }
    }

    /**
     * Called by the framework when your view for creating input needs to
     * be generated.  This will be called the first time your input method
     * is displayed, and every time it needs to be re-created such as due to
     * a configuration change.
     */
    @Override public View onCreateInputView() {
        assertAuthorityChanged();
        inputView =  getLayoutInflater().inflate(
                R.layout.kb_layout, null);
        tabLayout = (TabLayout)inputView.findViewById(R.id.tabs);
        rv = (RecyclerView) inputView.findViewById(R.id.kb_page_grid);
        rv.setLayoutManager(new GridLayoutManager(inputView.getContext(), OneGridPage.ROWS, LinearLayoutManager.HORIZONTAL, false));
        heading = (TextView) inputView.findViewById(R.id.kb_page_heading);
        shareText = (TextView) inputView.findViewById(R.id.share_kb_tv);
        mInputView = (LatinKeyboardView) inputView.findViewById(R.id._mm_kb_latin);
        pageFrame = inputView.findViewById(R.id._mm_kb_pageframe);

        shareMessage = getString(R.string._mm_kb_share_message);
        if (shareMessage!=null && shareMessage.length()>0){
            shareText.setVisibility(View.VISIBLE);
        }
        List<TabLayout.Tab> tabs = KBCategory.getTabs(tabLayout,this);
        onNewTabs(tabs);


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
                if (text.length()>1 && (Character.isSurrogatePair(text.charAt(0),text.charAt(1))|| MojiInputLayout.isVariation(text.charAt(1))))
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

        mInputView.setOnKeyboardActionListener(this);
        setLatinKeyboard(mQwertyKeyboard);

        return inputView;
    }
    private void setLatinKeyboard(LatinKeyboard nextKeyboard) {
        if (Build.VERSION.SDK_INT>18){
            final boolean shouldSupportLanguageSwitchKey =
                    mInputMethodManager.shouldOfferSwitchingToNextInputMethod(getToken());

        nextKeyboard.setLanguageSwitchKeyVisibility(shouldSupportLanguageSwitchKey);
        }
        mInputView.setKeyboard(nextKeyboard);
    }
    /**
     * Called by the framework when your view for showing candidates needs to
     * be generated, like {@link #onCreateInputView}.
     */
    @Override public View onCreateCandidatesView() {
        mCandidateView = new CandidateView(this);
        mCandidateView.setService(this);
        return mCandidateView;
    }
    /**
     * This is the main point where we do our initialization of the input method
     * to begin operating on an application.  At this point we have been
     * bound to the client, and are now receiving all of the detailed information
     * about the target of our edits.
     */
    @Override public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);
        packageName = attribute.packageName;

        // Reset our state.  We want to do this even if restarting, because
        // the underlying state of the text editor could have changed in any way.
        mComposing.setLength(0);
        updateCandidates();

        if (!restarting) {
            // Clear shift states.
            mMetaState = 0;
        }

        mPredictionOn = false;
        mCompletionOn = false;
        mCompletions = null;

        // We are now going to initialize our state based on the type of
        // text being edited.
        switch (attribute.inputType & InputType.TYPE_MASK_CLASS) {
            case InputType.TYPE_CLASS_NUMBER:
            case InputType.TYPE_CLASS_DATETIME:
                // Numbers and dates default to the symbols keyboard, with
                // no extra features.
                mCurKeyboard = mSymbolsKeyboard;
                break;

            case InputType.TYPE_CLASS_PHONE:
                // Phones will also default to the symbols keyboard, though
                // often you will want to have a dedicated phone keyboard.
                mCurKeyboard = mSymbolsKeyboard;
                break;

            case InputType.TYPE_CLASS_TEXT:
                // This is general text editing.  We will default to the
                // normal alphabetic keyboard, and assume that we should
                // be doing predictive text (showing candidates as the
                // user types).
                mCurKeyboard = mQwertyKeyboard;
                mPredictionOn = true;

                // We now look for a few special variations of text that will
                // modify our behavior.
                int variation = attribute.inputType & InputType.TYPE_MASK_VARIATION;
                if (variation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                        variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                    // Do not display predictions / what the user is typing
                    // when they are entering a password.
                    mPredictionOn = false;
                }

                if (variation == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                        || variation == InputType.TYPE_TEXT_VARIATION_URI
                        || variation == InputType.TYPE_TEXT_VARIATION_FILTER) {
                    // Our predictions are not useful for e-mail addresses
                    // or URIs.
                    mPredictionOn = false;
                }

                if ((attribute.inputType & InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE) != 0) {
                    // If this is an auto-complete text view, then our predictions
                    // will not be shown and instead we will allow the editor
                    // to supply their own.  We only show the editor's
                    // candidates when in fullscreen mode, otherwise relying
                    // own it displaying its own UI.
                    mPredictionOn = false;
                    mCompletionOn = isFullscreenMode();
                }

                // We also want to look at the current state of the editor
                // to decide whether our alphabetic keyboard should start out
                // shifted.
                updateShiftKeyState(attribute);
                break;

            default:
                // For all unknown input types, default to the alphabetic
                // keyboard with no special features.
                mCurKeyboard = mQwertyKeyboard;
                updateShiftKeyState(attribute);
        }

        // Update the label on the enter key, depending on what the application
        // says it will do.
        mCurKeyboard.setImeOptions(getResources(), attribute.imeOptions);
    }
    /**
     * This is called when the user is done editing a field.  We can use
     * this to reset our state.
     */
    @Override public void onFinishInput() {
        super.onFinishInput();

        // Clear current composing text and candidates.
        mComposing.setLength(0);
        updateCandidates();

        // We only hide the candidates window when finishing input on
        // a particular editor, to avoid popping the underlying application
        // up and down if the user is entering text into the bottom of
        // its window.
        setCandidatesViewShown(false);

        mCurKeyboard = mQwertyKeyboard;
        if (mInputView != null) {
            mInputView.closing();
        }
    }

    boolean firstStart =true;
    int currentTab = 0;
    @Override public void onStartInputView(EditorInfo attribute, boolean restarting) {
        super.onStartInputView(attribute, restarting);
        // Apply the selected keyboard to the input view.
        if (firstStart)
            inputView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    tabLayout.getTabAt(currentTab).select();
                }
            },20);
        firstStart=false;

        setLatinKeyboard(mCurKeyboard);
        mInputView.closing();
  //      final InputMethodSubtype subtype = mInputMethodManager.getCurrentInputMethodSubtype();
//        mInputView.setSubtypeOnSpaceKey(subtype);
    }
    @Override public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
        firstStart=true;
    }
    @Override
    public void onCurrentInputMethodSubtypeChanged(InputMethodSubtype subtype) {
        mInputView.setSubtypeOnSpaceKey(subtype);
    }
    /**
     * Deal with the editor reporting movement of its cursor.
     */
    @Override public void onUpdateSelection(int oldSelStart, int oldSelEnd,
                                            int newSelStart, int newSelEnd,
                                            int candidatesStart, int candidatesEnd) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd,
                candidatesStart, candidatesEnd);

        // If the current selection in the text view changes, we should
        // clear whatever candidate text we have.
        if (mComposing.length() > 0 && (newSelStart != candidatesEnd
                || newSelEnd != candidatesEnd)) {
            mComposing.setLength(0);
            updateCandidates();
            InputConnection ic = getCurrentInputConnection();
            if (ic != null) {
                ic.finishComposingText();
            }
        }
    }
    /**
     * This tells us about completions that the editor has determined based
     * on the current text in it.  We want to use this in fullscreen mode
     * to show the completions ourself, since the editor can not be seen
     * in that situation.
     */
    @Override public void onDisplayCompletions(CompletionInfo[] completions) {
        if (mCompletionOn) {
            mCompletions = completions;
            if (completions == null) {
                setSuggestions(null, false, false);
                return;
            }

            List<String> stringList = new ArrayList<String>();
            for (int i = 0; i < completions.length; i++) {
                CompletionInfo ci = completions[i];
                if (ci != null) stringList.add(ci.getText().toString());
            }
            setSuggestions(stringList, true, true);
        }
    }

    /**
     * This translates incoming hard key events in to edit operations on an
     * InputConnection.  It is only needed when using the
     * PROCESS_HARD_KEYS option.
     */
    private boolean translateKeyDown(int keyCode, KeyEvent event) {
        mMetaState = MetaKeyKeyListener.handleKeyDown(mMetaState,
                keyCode, event);
        int c = event.getUnicodeChar(MetaKeyKeyListener.getMetaState(mMetaState));
        mMetaState = MetaKeyKeyListener.adjustMetaAfterKeypress(mMetaState);
        InputConnection ic = getCurrentInputConnection();
        if (c == 0 || ic == null) {
            return false;
        }

        boolean dead = false;
        if ((c & KeyCharacterMap.COMBINING_ACCENT) != 0) {
            dead = true;
            c = c & KeyCharacterMap.COMBINING_ACCENT_MASK;
        }

        if (mComposing.length() > 0) {
            char accent = mComposing.charAt(mComposing.length() -1 );
            int composed = KeyEvent.getDeadChar(accent, c);
            if (composed != 0) {
                c = composed;
                mComposing.setLength(mComposing.length()-1);
            }
        }

        onKey(c, null);

        return true;
    }

    /**
     * Use this to monitor key events being delivered to the application.
     * We get first crack at them, and can either resume them or let them
     * continue to the app.
     */
    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                // The InputMethodService already takes care of the back
                // key for us, to dismiss the input method if it is shown.
                // However, our keyboard could be showing a pop-up window
                // that back should dismiss, so we first allow it to do that.
                if (event.getRepeatCount() == 0 && mInputView != null) {
                    if (mInputView.handleBack()) {
                        return true;
                    }
                }
                break;

            case KeyEvent.KEYCODE_DEL:
                // Special handling of the delete key: if we currently are
                // composing text for the user, we want to modify that instead
                // of let the application to the delete itself.
                if (mComposing.length() > 0) {
                    onKey(Keyboard.KEYCODE_DELETE, null);
                    return true;
                }
                break;

            case KeyEvent.KEYCODE_ENTER:
                // Let the underlying text editor always handle these.
                return false;

            default:
                // For all other keys, if we want to do transformations on
                // text being entered with a hard keyboard, we need to process
                // it and do the appropriate action.
                if (PROCESS_HARD_KEYS) {
                    if (keyCode == KeyEvent.KEYCODE_SPACE
                            && (event.getMetaState()&KeyEvent.META_ALT_ON) != 0) {
                        // A silly example: in our input method, Alt+Space
                        // is a shortcut for 'android' in lower case.
                        InputConnection ic = getCurrentInputConnection();
                        if (ic != null) {
                            // First, tell the editor that it is no longer in the
                            // shift state, since we are consuming this.
                            ic.clearMetaKeyStates(KeyEvent.META_ALT_ON);
                            keyDownUp(KeyEvent.KEYCODE_A);
                            keyDownUp(KeyEvent.KEYCODE_N);
                            keyDownUp(KeyEvent.KEYCODE_D);
                            keyDownUp(KeyEvent.KEYCODE_R);
                            keyDownUp(KeyEvent.KEYCODE_O);
                            keyDownUp(KeyEvent.KEYCODE_I);
                            keyDownUp(KeyEvent.KEYCODE_D);
                            // And we consume this event.
                            return true;
                        }
                    }
                    if (mPredictionOn && translateKeyDown(keyCode, event)) {
                        return true;
                    }
                }
        }

        return super.onKeyDown(keyCode, event);
    }
    /**
     * Use this to monitor key events being delivered to the application.
     * We get first crack at them, and can either resume them or let them
     * continue to the app.
     */
    @Override public boolean onKeyUp(int keyCode, KeyEvent event) {
        // If we want to do transformations on text being entered with a hard
        // keyboard, we need to process the up events to update the meta key
        // state we are tracking.
        if (PROCESS_HARD_KEYS) {
            if (mPredictionOn) {
                mMetaState = MetaKeyKeyListener.handleKeyUp(mMetaState,
                        keyCode, event);
            }
        }

        return super.onKeyUp(keyCode, event);
    }
    /**
     * Helper function to commit any text being composed in to the editor.
     */
    private void commitTyped(InputConnection inputConnection) {
        if (mComposing.length() > 0) {
            inputConnection.commitText(mComposing, mComposing.length());
            mComposing.setLength(0);
            updateCandidates();
        }
    }
    /**
     * Helper to update the shift state of our keyboard based on the initial
     * editor state.
     */
    private void updateShiftKeyState(EditorInfo attr) {
        if (attr != null
                && mInputView != null && mQwertyKeyboard == mInputView.getKeyboard()) {
            int caps = 0;
            EditorInfo ei = getCurrentInputEditorInfo();
            if (ei != null && ei.inputType != InputType.TYPE_NULL) {
                caps = getCurrentInputConnection().getCursorCapsMode(attr.inputType);
            }
            mInputView.setShifted(mCapsLock || caps != 0);
        }
    }

    /**
     * Helper to determine if a given character code is alphabetic.
     */
    private boolean isAlphabet(int code) {
        if (Character.isLetter(code)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Helper to send a key down / key up pair to the current editor.
     */
    private void keyDownUp(int keyEventCode) {
        getCurrentInputConnection().sendKeyEvent(
                new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
        getCurrentInputConnection().sendKeyEvent(
                new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
    }

    /**
     * Helper to send a character to the editor as raw key events.
     */
    private void sendKey(int keyCode) {
        switch (keyCode) {
            case '\n':
                keyDownUp(KeyEvent.KEYCODE_ENTER);
                break;
            default:
                if (keyCode >= '0' && keyCode <= '9') {
                    keyDownUp(keyCode - '0' + KeyEvent.KEYCODE_0);
                } else {
                    getCurrentInputConnection().commitText(String.valueOf((char) keyCode), 1);
                }
                break;
        }
    }
    // Implementation of KeyboardViewListener
    public void onKey(int primaryCode, int[] keyCodes) {
        if (isWordSeparator(primaryCode)) {
            // Handle separator
            if (mComposing.length() > 0) {
                commitTyped(getCurrentInputConnection());
            }
            sendKey(primaryCode);
            updateShiftKeyState(getCurrentInputEditorInfo());
        } else if (primaryCode == Keyboard.KEYCODE_DELETE) {
            handleBackspace();
        } else if (primaryCode == Keyboard.KEYCODE_SHIFT) {
            handleShift();
        } else if (primaryCode == Keyboard.KEYCODE_CANCEL) {
            handleClose();
            return;
        } else if (primaryCode == LatinKeyboardView.KEYCODE_LANGUAGE_SWITCH) {
            handleLanguageSwitch();
            return;
        } else if (primaryCode == LatinKeyboardView.KEYCODE_OPTIONS) {
            // Show a menu or somethin'
        } else if (primaryCode == Keyboard.KEYCODE_MODE_CHANGE
                && mInputView != null) {
            Keyboard current = mInputView.getKeyboard();
            if (current == mSymbolsKeyboard || current == mSymbolsShiftedKeyboard) {
                setLatinKeyboard(mQwertyKeyboard);
            } else {
                setLatinKeyboard(mSymbolsKeyboard);
                mSymbolsKeyboard.setShifted(false);
            }
        } else {
            handleCharacter(primaryCode, keyCodes);
        }
    }
    public void onText(CharSequence text) {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;
        ic.beginBatchEdit();
        if (mComposing.length() > 0) {
            commitTyped(ic);
        }
        ic.commitText(text, 0);
        ic.endBatchEdit();
        updateShiftKeyState(getCurrentInputEditorInfo());
    }
    /**
     * Update the list of available candidates from the current composing
     * text.  This will need to be filled in by however you are determining
     * candidates.
     */
    private void updateCandidates() {
        if (!mCompletionOn) {
            if (mComposing.length() > 0) {
                ArrayList<String> list = new ArrayList<String>();
                list.add(mComposing.toString());
                setSuggestions(list, true, true);
            } else {
                setSuggestions(null, false, false);
            }
        }
    }

    public void setSuggestions(List<String> suggestions, boolean completions,
                               boolean typedWordValid) {

        setCandidatesViewShown(false);
        /*
        if (suggestions != null && suggestions.size() > 0) {
            setCandidatesViewShown(true);
        } else if (isExtractViewShown()) {
            setCandidatesViewShown(true);
        }
        if (mCandidateView != null) {
            mCandidateView.setSuggestions(suggestions, completions, typedWordValid);
        }
        */
    }

    private void handleBackspace() {
        final int length = mComposing.length();
        if (length > 1) {
            CharSequence text = getCurrentInputConnection().getTextBeforeCursor(2, InputConnection.GET_TEXT_WITH_STYLES);
            int deleteLength =1;
            if (text.length()>1 && Character.isSurrogatePair(text.charAt(0),text.charAt(1)))
                deleteLength =2;
            mComposing.delete(length - deleteLength, length);
            getCurrentInputConnection().setComposingText(mComposing, deleteLength);
            updateCandidates();
        } else if (length > 0) {
            mComposing.setLength(0);
            getCurrentInputConnection().commitText("", 0);
            updateCandidates();
        } else {
            CharSequence text = getCurrentInputConnection().getTextBeforeCursor(2, InputConnection.GET_TEXT_WITH_STYLES);
            int deleteLength =1;
            if (text.length()>1 && (Character.isSurrogatePair(text.charAt(0),text.charAt(1))|| MojiInputLayout.isVariation(text.charAt(1))))
                deleteLength =2;
            getCurrentInputConnection().deleteSurroundingText(deleteLength,0);
            //keyDownUp(KeyEvent.KEYCODE_DEL);
        }
        updateShiftKeyState(getCurrentInputEditorInfo());
    }
    private void handleShift() {
        if (mInputView == null) {
            return;
        }

        Keyboard currentKeyboard = mInputView.getKeyboard();
        if (mQwertyKeyboard == currentKeyboard) {
            // Alphabet keyboard
            checkToggleCapsLock();
            mInputView.setShifted(mCapsLock || !mInputView.isShifted());
        } else if (currentKeyboard == mSymbolsKeyboard) {
            mSymbolsKeyboard.setShifted(true);
            setLatinKeyboard(mSymbolsShiftedKeyboard);
            mSymbolsShiftedKeyboard.setShifted(true);
        } else if (currentKeyboard == mSymbolsShiftedKeyboard) {
            mSymbolsShiftedKeyboard.setShifted(false);
            setLatinKeyboard(mSymbolsKeyboard);
            mSymbolsKeyboard.setShifted(false);
        }
    }

    private void handleCharacter(int primaryCode, int[] keyCodes) {
        if (isInputViewShown()) {
            if (mInputView.isShifted()) {
                primaryCode = Character.toUpperCase(primaryCode);
            }
        }
        if (isAlphabet(primaryCode) && mPredictionOn) {
            mComposing.append((char) primaryCode);
            getCurrentInputConnection().setComposingText(mComposing, 1);
            updateShiftKeyState(getCurrentInputEditorInfo());
            updateCandidates();
        } else {
            getCurrentInputConnection().commitText(
                    String.valueOf((char) primaryCode), 1);
        }
    }
    private void handleClose() {
        commitTyped(getCurrentInputConnection());
        requestHideSelf(0);
        mInputView.closing();
    }
    private IBinder getToken() {
        final Dialog dialog = getWindow();
        if (dialog == null) {
            return null;
        }
        final Window window = dialog.getWindow();
        if (window == null) {
            return null;
        }
        return window.getAttributes().token;
    }
    private void handleLanguageSwitch() {
        if (Build.VERSION.SDK_INT>15)
            mInputMethodManager.switchToNextInputMethod(getToken(), false /* onlyCurrentIme */);
    }
    private void checkToggleCapsLock() {
        long now = System.currentTimeMillis();
        if (mLastShiftTime + 800 > now) {
            mCapsLock = !mCapsLock;
            mLastShiftTime = 0;
        } else {
            mLastShiftTime = now;
        }
    }

    private String getWordSeparators() {
        return mWordSeparators;
    }

    public boolean isWordSeparator(int code) {
        String separators = getWordSeparators();
        return separators.contains(String.valueOf((char)code));
    }
    public void pickDefaultCandidate() {
        pickSuggestionManually(0);
    }

    public void pickSuggestionManually(int index) {
        if (mCompletionOn && mCompletions != null && index >= 0
                && index < mCompletions.length) {
            CompletionInfo ci = mCompletions[index];
            getCurrentInputConnection().commitCompletion(ci);
            if (mCandidateView != null) {
                mCandidateView.clear();
            }
            updateShiftKeyState(getCurrentInputEditorInfo());
        } else if (mComposing.length() > 0) {
            // If we were generating candidate suggestions for the current
            // text, we would commit one of them here.  But for this sample,
            // we will just commit the current text.
            commitTyped(getCurrentInputConnection());
        }
    }

    public void swipeRight() {
        if (mCompletionOn) {
            pickDefaultCandidate();
        }
    }

    public void swipeLeft() {
        handleBackspace();
    }
    public void swipeDown() {
        handleClose();
    }
    public void swipeUp() {
    }

    public void onPress(int primaryCode) {
    }

    public void onRelease(int primaryCode) {
    }



    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        currentTab = tab.getPosition();
        heading.setText(tab.getContentDescription());
        if (populator!=null)populator.teardown();
        if ("keyboard".equals(tab.getContentDescription())){
            mInputView.setVisibility(View.VISIBLE);
            pageFrame.setVisibility(View.GONE);
            return;
        }
        else
        {
            mInputView.setVisibility(View.GONE);
            pageFrame.setVisibility(View.VISIBLE);
        }

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
        onTabSelected(tab);
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
                    Uri uri = FileProvider.getUriForFile(getContext(),getContext().getString(R.string._mm_provider_authority),cacheFile);
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
        if (model.character!=null && !model.character.isEmpty()){
            getCurrentInputConnection().setComposingText(model.character, 1);
            getCurrentInputConnection().finishComposingText();
            return;
        }
        if (model.image_url!=null && !model.image_url.isEmpty())Moji.picasso.load(model.image_url).resize(size,size).into(t);
    }


    @Override
    public Context getContext() {
        return Moji.context;
    }

    @Override
    public int getPhraseBgColor() {
        return getResources().getColor(R.color._mm_default_phrase_bg_color);
    }

    @Override
    public void onNewTabs(List<TabLayout.Tab> tabs) {
        int selectedPosition = tabLayout.getSelectedTabPosition();
        tabLayout.removeAllTabs();
        for (TabLayout.Tab tab: tabs) tabLayout.addTab(tab);
        tabLayout.setOnTabSelectedListener(this);
        if (selectedPosition!= -1 && selectedPosition<tabs.size()) {
            tabLayout.getTabAt(selectedPosition).select();//setscrollposition doesn't work...
        }
    }
}