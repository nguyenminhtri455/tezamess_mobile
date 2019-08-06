package com.example.appchat.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.example.appchat.objectclass.Contact;
import com.example.appchat.objectclass.Member;

import java.util.ArrayList;
import java.util.List;

public class TableParticipation extends TezamessSqlite {

    private static TableParticipation tableParticipation;
    private Context context;

    private TableParticipation(Context context) {
        super(context);
        this.context = context;
    }

    public static TableParticipation getInstance(Context context) {
        if (tableParticipation == null) {
            tableParticipation = new TableParticipation(context);
        }
        return tableParticipation;
    }

    public List<Contact> getContactsInRoom(int roomId) {
        SQLiteDatabase db = getReadableDatabase();
        List<Contact> contacts = new ArrayList<>();
        List<Integer> userIds = new ArrayList<>();
        synchronized (db) {
            Cursor cursor = db.query(TABLE_PARTICIPATION, new String[]{USER}
                    , ROOM + " = ?"
                    , new String[]{String.valueOf(roomId)}
                    , null, null, null);
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                userIds.add(id);
            }
            cursor.close();

            String ids = userIds.toString().replaceAll("[\\[\\]\\s]", "");
            Cursor cursorContact = db.query(TABLE_CONTACT, null
                    , ID + " IN (" + ids + ")"
                    , null
                    , null, null, null);
            while (cursorContact.moveToNext()) {
                int id = cursorContact.getInt(0);
                String name = cursorContact.getString(1);
                String phone = cursorContact.getString(2);
                String urlavatar = cursorContact.getString(3);
                int relationship = cursorContact.getInt(5);
                int statusaddfriend = cursorContact.getInt(6);
                String lastActive = cursorContact.getString(7);

                Contact mContact = new Contact();
                mContact.setId(id);
                mContact.setPhone(phone);
                mContact.setName(name);
                mContact.setUrlavatar(urlavatar);
                mContact.setmRoomId(roomId);
                mContact.setmRelationship(relationship);
                mContact.setmStatusAddFriend(statusaddfriend);
                mContact.setLastactive(Long.parseLong(lastActive));

                contacts.add(mContact);
            }
            cursorContact.close();
        }
        return contacts;
    }

    public void addContacts(List<Contact> contacts, int roomId) {
        SQLiteDatabase db = getReadableDatabase();
        synchronized (db) {
            for (Contact contact : contacts) {
                Cursor cursor = db.query(TABLE_PARTICIPATION, new String[]{USER}
                        , USER + " = ? AND " + ROOM + " = ?"
                        , new String[]{String.valueOf(contact.getId()), String.valueOf(roomId)}
                        , null, null, null);
                if (cursor.getCount() <= 0) {
                    ContentValues values = new ContentValues();
                    values.put(USER, contact.getId());
                    values.put(ROOM, roomId);
                    values.put(ADMIN, String.valueOf(Member.getInstance(context).getId()));
                    db.insert(TABLE_PARTICIPATION, null, values);
                }
                cursor.close();
            }
        }
    }

    public void addContacts(List<Contact> contacts) {
        SQLiteDatabase db = getReadableDatabase();
        synchronized (db) {
            for (Contact contact : contacts) {
                Cursor cursor = db.query(TABLE_PARTICIPATION, new String[]{USER}
                        , USER + " = ? AND " + ROOM + " = ?"
                        , new String[]{String.valueOf(contact.getId()), String.valueOf(contact.getmRoomId())}
                        , null, null, null);
                if (cursor.getCount() <= 0) {
                    ContentValues values = new ContentValues();
                    values.put(USER, contact.getId());
                    values.put(ROOM, contact.getmRoomId());
                    values.put(ADMIN, String.valueOf(Member.getInstance(context).getId()));
                    db.insert(TABLE_PARTICIPATION, null, values);
                }
                cursor.close();
            }
        }
    }

    public void addContact(Contact contact, int roomId) {
        SQLiteDatabase db = getReadableDatabase();
        synchronized (db) {
            Cursor cursor = db.query(TABLE_CONTACT, new String[]{ID}, ID + " = ?"
                    , new String[]{String.valueOf(contact.getId())}
                    , null, null, null);
            if (cursor.getCount() <= 0) {
                ContentValues values = new ContentValues();
                values.put(USER, contact.getId());
                values.put(ROOM, roomId);
                values.put(ADMIN, String.valueOf(Member.getInstance(context).getId()));
                db.insert(TABLE_PARTICIPATION, null, values);
            }
            cursor.close();
        }
    }

    public void removeContacts(List<Contact> contacts) {
        SQLiteDatabase db = getReadableDatabase();
        synchronized (db) {
            for (Contact c : contacts) {
                db.delete(TABLE_PARTICIPATION
                        , USER + " = ? AND " + ROOM + " = ?"
                        , new String[]{String.valueOf(c.getId()), String.valueOf(c.getmRoomId())});
            }
        }
    }

    public void deleteParticipations(int idRoom) {
        SQLiteDatabase db = getReadableDatabase();
        synchronized (db) {
            db.delete(TABLE_PARTICIPATION, ROOM + " = ?", new String[]{String.valueOf(idRoom)});
        }
    }

    public void deleteParticipations() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                SQLiteDatabase db = getReadableDatabase();
                synchronized (db) {
                    db.execSQL("DELETE FROM " + TABLE_PARTICIPATION);
                }
                return null;
            }
        }.execute();
    }
}
