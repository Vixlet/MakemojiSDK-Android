package com.example.mojilib;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.mojilib.model.MojiModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Scott Baar on 1/25/2016.
 */
public class MojiSQLHelper extends SQLiteOpenHelper {


    public static MojiSQLHelper mInstance;
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "makemoji.db";

    private static final String FTS_VIRTUAL_TABLE = "FTS";
    public static final String TABLE_MM = "makemoji";
    public static final String COL_ID = "_ID";
    public static final String COL_ID_INT = "ID";
    public static final  String COL_NAME = "NAME";
    public static final  String COL_IMG_URL = "IMG_URL";
    public static final  String COL_LINK_URL = "LINK_URL";
    public static final  String COL_FLASHTAG = "FLASHTAG";
    public static final  String COL_CHARACTER = "CHARACTER";

    private static final String DATABASE_CREATE = "create table "
            + TABLE_MM + "(" + COL_ID + " integer primary key AUTOINCREMENT, "
            + COL_ID_INT + " INT, "
            + COL_NAME + " TEXT, "
            + COL_IMG_URL+ " TEXT, "
            + COL_LINK_URL + " TEXT, "
            + COL_FLASHTAG + " TEXT, "
            + COL_CHARACTER + " TEXT "
            +", UNIQUE( "+COL_ID_INT+ ","+COL_IMG_URL+ ","+ COL_NAME+") ON CONFLICT REPLACE"
            + ");";

    private MojiSQLHelper(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);

    }
    public static synchronized MojiSQLHelper getInstance(Context context){
        if (mInstance == null)
            mInstance =  new MojiSQLHelper(context.getApplicationContext());
        return mInstance;
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
    private ContentValues addValues(MojiModel model){
        ContentValues values = new ContentValues();
        values.put(COL_ID_INT, model.id);
        values.put(COL_NAME, model.name);
        values.put(COL_IMG_URL, model.image_url);
        values.put(COL_LINK_URL, model.link_url);
        values.put(COL_FLASHTAG, model.flashtag);
        values.put(COL_CHARACTER, model.character);
        return values;
    }
    public synchronized void insert(List<MojiModel> models){
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        for (MojiModel m : models) {
            ContentValues cv = addValues(m);
            long row = db.insert(TABLE_MM, null,cv);
        }
        db.setTransactionSuccessful();
        db.endTransaction();

    }
    public List<MojiModel> search(String query, int limit){
        SQLiteDatabase db = this.getReadableDatabase();
        List<MojiModel> models = new ArrayList<>();
        String raw = "SELECT * FROM "+TABLE_MM + " WHERE "+COL_NAME + " LIKE '" +query + "%' LIMIT "+ limit + " COLLATE NOCASE";
        Cursor c = db.rawQuery(raw,null);
        try{
            while (c.moveToNext()) {
                MojiModel mm = new MojiModel();
                mm.id = c.getInt(1);
                mm.name = c.getString(2);
                mm.image_url = c.getString(3);
                mm.link_url = c.getString(4);
                mm.flashtag = c.getString(5);
                mm.character = c.getString(6);
                models.add(mm);
            }

        }
        catch (Exception e){
            e.printStackTrace();
        }
        try{
            c.close();
        } catch (Exception e){e.printStackTrace();}
        return models;
    }
}
