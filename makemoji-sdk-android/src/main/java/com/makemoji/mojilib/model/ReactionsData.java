package com.makemoji.mojilib.model;

import android.support.annotation.Nullable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.makemoji.mojilib.Moji;
import com.makemoji.mojilib.MojiEditText;
import com.makemoji.mojilib.MojiSpan;
import com.makemoji.mojilib.PagerPopulator;
import com.makemoji.mojilib.SmallCB;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import retrofit2.Response;

/**
 * created fields not included
 * Created by s_baa on 7/5/2016.
 */
public class ReactionsData {
    public Content content;
    public List<Reaction> reactions;
    public CurrentUser user;
    private static Type ReactionsListType;
    private boolean inFlight = false;
    PagerPopulator.PopulatorObserver observer;
    public ReactionsData(String id){
        inFlight = true;
        Moji.mojiApi.getReactionData(getHash(id)).enqueue(new SmallCB<JSONObject>() {
            @Override
            public void done(Response<JSONObject> response, @Nullable Throwable t) {
                inFlight = false;
                if (t!=null){
                    t.printStackTrace();
                    return;
                }
                fromJson(ReactionsData.this,response.body());
            }
        });

    }
    public ReactionsData(JSONObject jsonObject){
        fromJson(this,jsonObject);
    }
    public void setObserver(PagerPopulator.PopulatorObserver observer){
        this.observer = observer;
        if (observer!=null && reactions!=null) observer.onNewDataAvailable();
    }
    public void removeObserver(PagerPopulator.PopulatorObserver observerToRemove){
        if (observerToRemove==observer)
            observer =null;
    }
    public List<Reaction> getReactions(){return reactions;}

    public static void fromJson(ReactionsData data, JSONObject jo){
        if (ReactionsListType==null)ReactionsListType = new TypeToken<List<Reaction>>() {}.getType();
        try{
            data.content = MojiModel.gson.fromJson(jo.getJSONObject("content").toString(),Content.class);
            data.reactions = MojiModel.gson.fromJson(jo.getJSONArray("reactions").toString(),ReactionsListType);
            data.user = MojiModel.gson.fromJson(jo.getJSONObject("currentUser").toString(),CurrentUser.class);

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    public class Content{
        public int id;
        public int sdk_id;
        public int content_id;
        public  String title;
    }
    public class Reaction{
        public int total;
        public int emoji_id;
        public String emoji_type;
        public String character;
        public String image_url;
        public Spanned toSpanned(@Nullable TextView tv){
            SpannableStringBuilder ssb = new SpannableStringBuilder();
            if (character!=null && !character.isEmpty()){
                ssb.append(character);
                return ssb;
            }
            MojiModel model = new MojiModel("unknown",image_url);
            model.id = emoji_id;
            MojiSpan span = MojiSpan.fromModel(model,tv,null);
            ssb.append(MojiEditText.replacementChar);
            ssb.setSpan(span,0,1,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            return ssb;
        }
    }
    public class CurrentUser{
        public int id;
        public int sdk_id;
        public int user_id;
        public int content_id;
        public int emoji_id;
        public String emoji_type;
    }

    public static String getHash(String str) {
        MessageDigest digest = null;
        byte[] input = null;

        try {
            digest = MessageDigest.getInstance("SHA-1");
            digest.reset();
            input = digest.digest(str.getBytes("UTF-8"));

        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return convertToHex(input);
    }

    private static String convertToHex(byte[] data) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                if ((0 <= halfbyte) && (halfbyte <= 9))
                    buf.append((char) ('0' + halfbyte));
                else
                    buf.append((char) ('a' + (halfbyte - 10)));
                halfbyte = data[i] & 0x0F;
            } while(two_halfs++ < 1);
        }
        return buf.toString();
    }
}
