package com.example.appchat.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.net.URL;

public abstract class TezamessSqlite extends SQLiteOpenHelper {

    //general
    public static final String DATABASE_NAME = "tezamess.sqlite";
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String ADMIN = "adminid";
    public static final String TYPE = "type";
    public static final String ROOM = "room";

    //contact
    public static final String TABLE_CONTACT = "contact";
    public static final String PHONE = "phone";
    public static final String URLAVATAR = "urlavatar";
    public static final String LASTACTIVE = "lastactive";
    public static final String RELATIONSHIP = "relationship";
    public static final String STATUSADDFRIEND = "statusaddfriend";

    //room
    public static final String TABLE_ROOM = "room";
    public static final String CREATOR = "creator";

    //message
    public static final String TABLE_MESSAGE = "message";
    public static final String KEY = "key";
    public static final String SENDER = "sender";
    public static final String CONTENT = "content";
    public static final String TIME = "time";
    public static final String STATUS = "status";

    //status
    public static final String TABLE_STATUS = "status";
    public static final String CREATREDATE = "createdate";
    public static final String BODY = "body";

    //media
    public static final String TABLE_MEDIA = "media";
    public static final String STATUS_ID = "statusid";
    public static final String URL = "url";

    //participation
    public static final String TABLE_PARTICIPATION = "participation";
    public static final String USER = "user";

    public TezamessSqlite(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String tableContact = "CREATE TABLE " + TABLE_CONTACT +
                "(" + ID + " INTEGER PRIMARY KEY" +
                "," + NAME + " TEXT" +
                "," + PHONE + " TEXT" +
                "," + URLAVATAR + " TEXT" +
                "," + ROOM + " INTEGER" +
                "," + RELATIONSHIP + " INTEGER" +
                "," + STATUSADDFRIEND + " INTEGER" +
                "," + LASTACTIVE + " INTEGER" +
                "," + ADMIN + " INTEGER)";

        String tableRoom = "CREATE TABLE " + TABLE_ROOM +
                "(" + ID + " INTEGER PRIMARY KEY" +
                "," + NAME + " TEXT" +
                "," + CREATOR + " INTEGER" +
                "," + TYPE + " TEXT" +
                "," + URLAVATAR + " TEXT" +
                "," + ADMIN + " INTEGER)";

        String tableMessage = "CREATE TABLE " + TABLE_MESSAGE +
                "(" + ID + " INTEGER" +
                "," + SENDER + " INTEGER" +
                "," + ROOM + " INTEGER" +
                "," + CONTENT + " TEXT" +
                "," + TIME + " INTEGER" +
                "," + STATUS + " TEXT" +
                "," + TYPE + " TEXT" +
                "," + ADMIN + " INTEGER" +
                "," + KEY + " INTEGER PRIMARY KEY AUTOINCREMENT)";

        String tablePaticipation = "CREATE TABLE " + TABLE_PARTICIPATION +
                "(" + USER + " INTEGER" +
                "," + ROOM + " INTEGER" +
                "," + ADMIN + " INTEGER" +
                ", PRIMARY KEY(" + USER +
                "," + ROOM + "))";

        String tableStatus = "CREATE TABLE " + TABLE_STATUS +
                "(" + ID + " INTEGER PRIMARY KEY" +
                "," + CREATOR + " INTEGER" +
                "," + CREATREDATE + " INTEGER" +
                "," + BODY + " TEXT" +
                "," + ADMIN + " INTEGER)";

        String tableMedia = "CREATE TABLE " + TABLE_MEDIA +
                "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT" +
                "," + STATUS_ID + " INTEGER" +
                "," + URL + " TEXT" +
                "," + ADMIN + " INTEGER)";

        db.execSQL(tableContact);
        db.execSQL(tableRoom);
        db.execSQL(tableMessage);
        db.execSQL(tablePaticipation);
        db.execSQL(tableStatus);
        db.execSQL(tableMedia);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String dropTableContact = String.format("DROP TABLE IF EXISTS %s", TABLE_CONTACT);
        String dropTableMessage = String.format("DROP TABLE IF EXISTS %s", TABLE_MESSAGE);
        String dropTableRoom = String.format("DROP TABLE IF EXISTS %s", TABLE_ROOM);
        String dropTableParticipation = String.format("DROP TABLE IF EXISTS %s", TABLE_PARTICIPATION);
        db.execSQL(dropTableContact);
        db.execSQL(dropTableMessage);
        db.execSQL(dropTableRoom);
        db.execSQL(dropTableParticipation);
        onCreate(db);
    }

    public void close(){
        getReadableDatabase().close();
    }

}