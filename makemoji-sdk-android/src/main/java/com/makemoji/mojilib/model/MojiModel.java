package com.makemoji.mojilib.model;


import android.content.SharedPreferences;
import android.support.annotation.WorkerThread;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.makemoji.mojilib.Moji;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Scott Baar on 1/9/2016.
 */
public class MojiModel {
    static Gson gson = new Gson();
    public int id;
    public String user_id;
    public String origin_id;
    public  String name;
    public String image_url;
    public String link_url;
    public String legacy;
    public String deleted;
    public String created;
    public String access;
    public String username;
    public String flashtag;
    public int shares;
    public int remoji;
    public int likes;
    public String character;

    @SerializedName("native")
    public int _native;
    public int phrase;
    public List<MojiModel> emoji = new ArrayList<>();

    public MojiModel(){}
    public MojiModel(String name, String image_url){
        this.name = name;
        this.image_url = image_url;
    }

    @Override
    public boolean equals(Object o){
        if (!(o instanceof MojiModel)) return false;
        MojiModel m = (MojiModel) o;
        if (m.character!=null && !m.character.equals(character))return false;
        else if (character!=null && !character.equals(m.character))return false;
        return  (m.image_url).equals(image_url);
    }
    public static JSONObject toJson(MojiModel m){
        if ( m.image_url==null||m.name==null)return null;//invalid object
        try{
            return new JSONObject(gson.toJson(m));
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
        /*
        JSONObject jo = new JSONObject();
        try {
            jo.put("image_url", m.image_url);
            jo.put("link_url", m.link_url);
            jo.put("name", m.name);
            jo.put("id", m.id);
            jo.put("phrase",m.phrase);
            if (m.emoji!=null){
                JSONArray ja = new JSONArray();
                for (MojiModel emoji : m.emoji) ja.put(toJson(emoji));
                jo.put("emoji",ja.toString());
            }
        }
        catch (Exception e){e.printStackTrace();}
        return jo;
        */
    }
    public static MojiModel fromJson(JSONObject jo){
       return gson.fromJson(jo.toString(),MojiModel.class);
        /*MojiModel m= new MojiModel(jo.optString("name"),jo.optString("image_url"));
        m.link_url = jo.optString("link_url",null);
        m.id = jo.optInt("id",-1);
        m.phrase = jo.optBoolean("phrase");
        if (jo.has("emoji")){
            JSONArray ja = jo.optJSONArray("emoji");
            m.emoji = new ArrayList<>();
            for (int i = 0; i < ja.length();i++) m.emoji.add
        }
        return m;*/
    }
    public static JSONArray toJsonArray(Collection<MojiModel> models){
        JSONArray ja = new JSONArray();
        if (models==null ||models.isEmpty())return ja;
        for (MojiModel m : models){
            JSONObject jo = toJson(m);
            ja.put(jo);
        }
        return ja;
    }
    public static List<MojiModel> fromJSONArray(JSONArray ja){
        List<MojiModel> list = new ArrayList<>();
        for (int i = 0; i <ja.length();i++){
            try {
                list.add(fromJson(ja.getJSONObject(i)));
            }
            catch (Exception e){e.printStackTrace();}
        }
        return list;
    }
    @Override
    public String toString(){
        return ""+name;
    }
    @WorkerThread
    public static void saveList(List<MojiModel> list,String name){
        SharedPreferences sp = Moji.context.getSharedPreferences("_mm_cached_lists4",0);
        sp.edit().putString(""+name,toJsonArray(list).toString()).apply();
    }
    @WorkerThread
    public static List<MojiModel> getList(String name){
        SharedPreferences sp = Moji.context.getSharedPreferences("_mm_cached_lists4",0);
        List<MojiModel> models;
        try{
            models = fromJSONArray(new JSONArray(sp.getString(name,"[]")));
        }catch (Exception e){
            models = new ArrayList<>();
            e.printStackTrace();
        }
        return models;
    }
    public boolean isNative(){return _native==1;}
    public boolean isPhrase(){return phrase==1;}
}
