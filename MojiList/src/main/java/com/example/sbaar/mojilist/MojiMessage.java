package com.example.sbaar.mojilist;

import android.text.Spanned;

import com.example.mojilib.ParsedAttributes;

import org.json.JSONObject;

import java.util.Random;

/**
 * Created by Scott Baar on 12/3/2015.
 */
public class MojiMessage {
    public String from,to, fromImg, toImg, messageRaw, id;
    public ParsedAttributes parsedAttributes;
    public MojiMessage(JSONObject jo){
        from = jo.optString("from_username");
        to = jo.optString("to_username");
        fromImg = jo.optString("from_profile_img");
        toImg = jo.optString("to_profile_img");
        messageRaw = jo.optString("message");
        id = jo.optString("id","");
    }
    public MojiMessage(String html){
        messageRaw = html;
        id = String.valueOf((new Random().nextFloat()));

    }
}
