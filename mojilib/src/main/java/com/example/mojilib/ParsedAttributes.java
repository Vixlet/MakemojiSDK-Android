package com.example.mojilib;

import android.text.Spanned;

/**
 * Spanned and attributes parsed from html.
 * Created by Scott Baar on 12/15/2015.
 */
public class ParsedAttributes {
    public Spanned spanned;
    public int marginBottom;
    public int marginTop;
    public int marginLeft;
    public int marginRight;
    public String fontFamily;
    public int fontSizePt;//unscaled font size in pt
    public int color;//color parsed from hex
}
