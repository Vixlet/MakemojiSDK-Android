package com.makemoji.mojilib;

import android.text.Spanned;

/**
 * We need our own text watcher hook to avoid before/after textchanged loops and maintain selection positioning.
 * Set this on a textview .setTag(R.id._makemoji_moji_text_watcher, ...) and it will be called just before the library changes the text.
 * Created by Scott Baar on 1/28/2017.
 */

public interface IMojiTextWatcher {
    //return a spanned to set, optionally modified if needed
    Spanned textAboutToChange(Spanned spanned);
}
