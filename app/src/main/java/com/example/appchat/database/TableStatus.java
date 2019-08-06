package com.example.appchat.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.appchat.objectclass.Contact;
import com.example.appchat.objectclass.Member;
import com.example.appchat.objectclass.Status;

import java.util.ArrayList;
import java.util.List;

public class TableStatus extends TezamessSqlite {

    private static TableStatus tableStatus;
    private Context context;

    private TableStatus(Context context) {
        super(context);
        this.context = context;
    }

    public static TableStatus getInstance(Context context) {
        if (tableStatus == null) {
            tableStatus = new TableStatus(context);
        }
        return tableStatus;
    }

    public List<Status> getStatuses(int start, int limit) {
        Log.e("BBBBB", "dzoooooooooo TableStatus getStatuses");
        List<Status> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        synchronized (db) {
            Cursor cursor = db.query(TABLE_STATUS, null, null, null, null, null,
                    CREATREDATE + " DESC", start + "," + limit);
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                long createdate = cursor.getLong(2);
                String body = cursor.getString(3);

                Contact contact = new Contact();
                contact.setId(Member.getInstance(context).getId());
                contact.setName(Member.getInstance(context).getName());
                contact.setUrlavatar(Member.getInstance(context).getUrlavatar());

                List<String> medias = TableMedia.getInstance(context).getMedias(id);

                Status status = new Status();
                status.setId(id);
                status.setBody(body);
                status.setCreatedate(createdate);
                status.setUserid(contact);
                status.setUrlImages(medias);
            }
        }
        return list;
    }

    public void saveStatus(Status status) {
        Log.e("BBBBB", "dzoooooooooo TableStatus saveStatus");
        SQLiteDatabase db = getReadableDatabase();
        synchronized (db) {
            ContentValues values = new ContentValues();
            values.put(ID, status.getId());
            values.put(BODY, status.getBody());
            values.put(CREATOR, status.getUserid().getId());
            values.put(CREATREDATE, status.getCreatedate());
            values.put(ADMIN, String.valueOf(Member.getInstance(context).getId()));
            db.insert(TABLE_STATUS, null, values);
            TableMedia.getInstance(context).saveMedias(status.getUrlImages(), status.getId());
        }
    }
}

