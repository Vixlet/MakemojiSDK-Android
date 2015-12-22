package com.example.sbaar.mojilist;

import android.text.Spanned;

import com.example.mojilib.ParsedAttributes;

import org.json.JSONObject;

/**
 * Created by Scott Baar on 12/3/2015.
 */
public class MojiMessage {
    final String from,to, fromImg, toImg, messageRaw, id;
    public ParsedAttributes parsedAttributes;
    public MojiMessage(JSONObject jo){
        from = jo.optString("from_username");
        to = jo.optString("to_username");
        fromImg = jo.optString("from_profile_img");
        toImg = jo.optString("to_profile_img");
        messageRaw = jo.optString("message");
        id = jo.optString("id","");
    }
}
