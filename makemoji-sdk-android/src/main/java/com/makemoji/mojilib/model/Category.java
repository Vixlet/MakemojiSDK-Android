package com.makemoji.mojilib.model;

import com.makemoji.mojilib.Moji;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Scott Baar on 1/9/2016.
 */
public class Category {
    public final String name;
    public final String image_url;


    public Category(String name, String image_url) {
        this.name = name;
        this.image_url = image_url;
    }

    public static void saveCategories(List<Category> categoryList){
        JSONArray ja =new JSONArray();
        try {
            for (Category category : categoryList) {
                JSONObject jo = new JSONObject();
                jo.putOpt("name", category.name);
                jo.putOpt("image_url", category.image_url);
                ja.put(ja.length(), jo);
            }
            Moji.context.getSharedPreferences("_mm_categories",0).edit().putString("categories",ja.toString()).apply();
        } catch (Exception e){e.printStackTrace();}
    }
    public static List<Category> getCategories(){
        List<Category> categories = new ArrayList<>();
        try{
            JSONArray ja = new JSONArray(Moji.context.getSharedPreferences("_mm_categories",0).getString("categories","[]"));
            for (int i = 0; i<ja.length();i++){
                JSONObject jo = ja.getJSONObject(i);
                categories.add(new Category(jo.optString("name"),jo.optString("image_url")));
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return categories;
    }
}
