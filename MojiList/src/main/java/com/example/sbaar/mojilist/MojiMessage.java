package com.example.sbaar.mojilist;

import org.json.JSONObject;

/**
 * Created by DouglasW on 12/3/2015.
 */
public class MojiMessage {
    String from,to, fromImg, toImg, messageRaw, id;
    public MojiMessage(JSONObject jo){
        from = jo.optString("from_username");
        to = jo.optString("to_username");
        fromImg = jo.optString("from_profile_img");
        toImg = jo.optString("to_profile_img");
        messageRaw = jo.optString("message");
        id = jo.optString("id","");
    }
}
