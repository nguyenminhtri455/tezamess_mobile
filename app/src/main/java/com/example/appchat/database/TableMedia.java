package com.example.appchat.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.appchat.objectclass.Member;

import java.util.ArrayList;
import java.util.List;

public class TableMedia extends TezamessSqlite {

    private static TableMedia tableMedia;
    private Context context;

    private TableMedia(Context context) {
        super(context);
        this.context = context;
    }

    public static TableMedia getInstance(Context context) {
        if (tableMedia == null) {
            tableMedia = new TableMedia(context);
        }
        return tableMedia;
    }

    public List<String> getMedias(int statusId) {
        Log.e("BBBBB", "dzoooooooooo TableMedia getMedias");
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        synchronized (db) {
            Cursor cursor = db.query(TABLE_STATUS, new String[]{URL},
                    STATUS_ID + " = ?", new String[]{String.valueOf(statusId)}, null,
                    null, null);
            while (cursor.moveToNext()) {
                String url = cursor.getString(0);
                list.add(url);
            }
        }
        return list;
    }

    public void saveMedias(List<String> urls, int statusId) {
        Log.e("BBBBB", "dzoooooooooo TableMedia saveMedias");
        SQLiteDatabase db = getReadableDatabase();
        synchronized (db) {
            for (String s : urls) {
                ContentValues values = new ContentValues();
                values.put(STATUS_ID, statusId);
                values.put(URL, s);
                values.put(ADMIN, String.valueOf(Member.getInstance(context).getId()));
                db.insert(TABLE_MEDIA, null, values);
            }
        }
    }
}

