package com.example.appchat.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.example.appchat.objectclass.Contact;
import com.example.appchat.objectclass.Member;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TableContact extends TezamessSqlite {
    private static TableContact tableContact;
    private Context context;

    private TableContact(Context context) {
        super(context);
        this.context = context;
    }

    public static TableContact getInstance(Context context) {
        if (tableContact == null) {
            tableContact = new TableContact(context);
        }
        return tableContact;
    }

    public List<Contact> getContacts() {
        List<Contact> contacts = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        synchronized (db) {
            Cursor cursor = db.query(TABLE_CONTACT, null,
                    ADMIN + " = ?", new String[]{String.valueOf(Member.getInstance(context).getId())}, null, null, null);
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                String phone = cursor.getString(2);
                String urlavatar = cursor.getString(3);
                int roomId = cursor.getInt(4);
                int relationship = cursor.getInt(5);
                int statusAddFriend = cursor.getInt(6);
                long lastActive = cursor.getLong(7);
                Contact contact = new Contact();
                contact.setId(id);
                contact.setPhone(phone);
                contact.setName(name);
                contact.setUrlavatar(urlavatar);
                contact.setmRoomId(roomId);
                contact.setLastactive(lastActive);
                contact.setmRelationship(relationship);
                contact.setmStatusAddFriend(statusAddFriend);
                contacts.add(contact);
            }
            cursor.close();
        }
        return contacts;
    }

    public List<Contact> getContactsFriend() {
        List<Contact> contacts = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        synchronized (db) {
            Cursor cursor = db.query(TABLE_CONTACT, null,
                    RELATIONSHIP + " = ? AND " + ADMIN + " = ?", new String[]{String.valueOf(1)
                            , String.valueOf(Member.getInstance(context).getId())}
                    , null, null, null);
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                String phone = cursor.getString(2);
                String urlavatar = cursor.getString(3);
                int roomId = cursor.getInt(4);
                int relationship = cursor.getInt(5);
                int statusAddFriend = cursor.getInt(6);
                long lastActive = cursor.getLong(7);
                Contact contact = new Contact();
                contact.setId(id);
                contact.setPhone(phone);
                contact.setName(name);
                contact.setUrlavatar(urlavatar);
                contact.setLastactive(lastActive);
                contact.setmStatusAddFriend(statusAddFriend);
                contact.setmRoomId(roomId);
                contact.setmRelationship(relationship);
                contacts.add(contact);
            }
            cursor.close();
        }
        Collections.sort(contacts,(t1, t2) -> Character.compare(Character.toLowerCase(t1.getName().charAt(0)),
                Character.toLowerCase(t2.getName().charAt(0))));
        return contacts;
    }

    public List<Contact> getContactsNotFriend() {
        List<Contact> contacts = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        synchronized (db) {
            Cursor cursor = db.query(TABLE_CONTACT, null,
                    RELATIONSHIP + " = ?" +
                            " AND " + ADMIN + " = ?" +
                            " AND " + STATUSADDFRIEND + " IN (?,?)" +
                            " AND " + ID + " != ?"
                    , new String[]{String.valueOf(-1)
                            , String.valueOf(Member.getInstance(context).getId())
                            , String.valueOf(1)
                            , String.valueOf(2)
                            , String.valueOf(Member.getInstance(context).getId())}
                    , null, null, null);
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                String phone = cursor.getString(2);
                String urlavatar = cursor.getString(3);
                int roomId = cursor.getInt(4);
                int relationship = cursor.getInt(5);
                int statusAddFriend = cursor.getInt(6);
                long lastActive = cursor.getLong(7);
                Contact contact = new Contact();
                contact.setId(id);
                contact.setPhone(phone);
                contact.setName(name);
                contact.setUrlavatar(urlavatar);
                contact.setmRoomId(roomId);
                contact.setLastactive(lastActive);
                contact.setmRelationship(relationship);
                contact.setmStatusAddFriend(statusAddFriend);
                contacts.add(contact);
            }
            cursor.close();
        }
        return contacts;
    }

    public List<Contact> getContactsRequest() {
        Log.e("BBBBB", "dzoooooooooo TableContact getContactsRequest");
        List<Contact> contacts = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        synchronized (db) {
            Cursor cursor = db.query(TABLE_CONTACT, null,
                    STATUSADDFRIEND + " = ? AND " + ADMIN + " = ?"
                    , new String[]{String.valueOf(3), String.valueOf(Member.getInstance(context).getId())}
                    , null, null, null);
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                String phone = cursor.getString(2);
                String urlavatar = cursor.getString(3);
                int roomId = cursor.getInt(4);
                int relationship = cursor.getInt(5);
                int statusAddFriend = cursor.getInt(6);
                long lastActive = cursor.getLong(7);
                Contact contact = new Contact();
                contact.setId(id);
                contact.setPhone(phone);
                contact.setName(name);
                contact.setUrlavatar(urlavatar);
                contact.setmRoomId(roomId);
                contact.setLastactive(lastActive);
                contact.setmRelationship(relationship);
                contact.setmStatusAddFriend(statusAddFriend);
                contacts.add(contact);
            }
            cursor.close();
        }
        return contacts;
    }

    public List<Contact> getContactsInRoom(int idRoom) {
        List<Contact> contacts = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        synchronized (db) {
            Cursor cursor = db.query(TABLE_CONTACT, null,
                    ROOM + " = ? AND " + ADMIN + " = ?"
                    , new String[]{String.valueOf(idRoom), String.valueOf(Member.getInstance(context).getId())}
                    , null, null, null);
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                String phone = cursor.getString(2);
                String urlavatar = cursor.getString(3);
                int roomId = cursor.getInt(4);
                int relationship = cursor.getInt(5);
                int statusAddFriend = cursor.getInt(6);
                long lastActive = cursor.getLong(7);
                Contact contact = new Contact();
                contact.setId(id);
                contact.setPhone(phone);
                contact.setName(name);
                contact.setUrlavatar(urlavatar);
                contact.setmRoomId(roomId);
                contact.setLastactive(lastActive);
                contact.setmRelationship(relationship);
                contact.setmStatusAddFriend(statusAddFriend);
                contacts.add(contact);
            }
            cursor.close();
        }
        return contacts;
    }

    public Contact getContact(int idContact) {
        SQLiteDatabase db = getReadableDatabase();
        Contact mContact = new Contact();
        synchronized (db) {
            Cursor cursor = db.query(TABLE_CONTACT, null,
                    ID + " = ? AND " + ADMIN + " = ?", new String[]{String.valueOf(idContact), String.valueOf(Member.getInstance(context).getId())}, null, null, null);
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                String phone = cursor.getString(2);
                String urlavatar = cursor.getString(3);
                int roomId = cursor.getInt(4);
                int relationship = cursor.getInt(5);
                int statusaddfriend = cursor.getInt(6);
                long lastActive = cursor.getLong(7);
                mContact.setId(id);
                mContact.setPhone(phone);
                mContact.setName(name);
                mContact.setUrlavatar(urlavatar);
                mContact.setmRoomId(roomId);
                mContact.setmRelationship(relationship);
                mContact.setmStatusAddFriend(statusaddfriend);
                mContact.setLastactive(lastActive);
            }
            cursor.close();
        }
        return mContact;
    }

    public void addContacts(List<Contact> contacts) {
        Log.e("BBBBB", "dzoooooooooo TableContact addContacts");
        SQLiteDatabase db = getReadableDatabase();
        synchronized (db) {
            for (Contact contact : contacts) {
                Cursor cursor = db.query(TABLE_CONTACT, new String[]{ID}, ID + " = ?"
                        , new String[]{String.valueOf(contact.getId())}
                        , null, null, null);
                if (cursor.getCount() <= 0) {
                    ContentValues values = new ContentValues();
                    values.put(ID, contact.getId());
                    values.put(NAME, contact.getName());
                    values.put(PHONE, contact.getPhone());
                    values.put(URLAVATAR, String.valueOf(contact.getUrlavatar()));
                    values.put(ROOM, String.valueOf(contact.getmRoomId()));
                    values.put(RELATIONSHIP, contact.getmRelationship());
                    values.put(STATUSADDFRIEND, contact.getmStatusAddFriend());
                    values.put(LASTACTIVE, String.valueOf(contact.getLastactive()));
                    values.put(ADMIN, String.valueOf(Member.getInstance(context).getId()));
                    db.insert(TABLE_CONTACT, null, values);
                }
                cursor.close();
            }
        }
    }

    public void addContact(Contact contact) {
        Log.e("BBBBB", "dzoooooooooo TableContact 2");
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                SQLiteDatabase db = getReadableDatabase();
                synchronized (db) {
                    Cursor cursor = db.query(TABLE_CONTACT, new String[]{ID}, ID + " = ?"
                            , new String[]{String.valueOf(contact.getId())}
                            , null, null, null);
                    if (cursor.getCount() <= 0) {
                        ContentValues values = new ContentValues();
                        values.put(ID, contact.getId());
                        values.put(NAME, contact.getName());
                        values.put(PHONE, contact.getPhone());
                        values.put(URLAVATAR, String.valueOf(contact.getUrlavatar()));
                        values.put(LASTACTIVE, String.valueOf(contact.getLastactive()));
                        values.put(STATUSADDFRIEND, contact.getmStatusAddFriend());
                        values.put(ROOM, String.valueOf(contact.getmRoomId()));
                        values.put(RELATIONSHIP, contact.getmRelationship());
                        values.put(ADMIN, String.valueOf(Member.getInstance(context).getId()));
                        db.insert(TABLE_CONTACT, null, values);
                    }
                }
                return null;
            }
        }.execute();


    }

    public void updateContacts(List<Contact> contacts) {
        Log.e("BBBBB", "dzoooooooooo TableContact 3");
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                SQLiteDatabase db = getReadableDatabase();
                for (Contact contact : contacts) {
                    ContentValues values = new ContentValues();
                    values.put(ID, contact.getId());
                    values.put(NAME, contact.getName());
                    values.put(PHONE, contact.getPhone());
                    values.put(URLAVATAR, String.valueOf(contact.getUrlavatar()));
                    values.put(LASTACTIVE, String.valueOf(contact.getLastactive()));
                    if (contact.getmStatusAddFriend() != 1) {
                        values.put(STATUSADDFRIEND, contact.getmStatusAddFriend());
                    }

                    if (contact.getmRoomId() != -1) {
                        values.put(ROOM, String.valueOf(contact.getmRoomId()));
                    }
                    if (contact.getmRelationship() != -1) {
                        values.put(RELATIONSHIP, contact.getmRelationship());
                    }

                    synchronized (db) {
                        db.update(TABLE_CONTACT, values, ID + " = ?", new String[]{String.valueOf(contact.getId())});
                    }
                }
                return null;
            }
        }.execute();
    }

    public void updateContact(Contact contact) {
        Log.e("BBBBB", "dzoooooooooo TableContact 4");
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                SQLiteDatabase db = getReadableDatabase();
                Cursor cursor = db.query(TABLE_CONTACT, new String[]{ID}
                        , ID + " = ? AND " + ADMIN + " = ? "
                        , new String[]{String.valueOf(contact.getId()), String.valueOf(Member.getInstance(context).getId())}
                        , null, null, null);

                if (cursor.getCount() > 0) {
                    ContentValues values = new ContentValues();
                    values.put(ID, contact.getId());
                    values.put(NAME, contact.getName());
                    values.put(PHONE, contact.getPhone());
                    values.put(URLAVATAR, String.valueOf(contact.getUrlavatar()));
                    values.put(LASTACTIVE, String.valueOf(contact.getLastactive()));
                    if (contact.getmStatusAddFriend() != 1) {
                        values.put(STATUSADDFRIEND, contact.getmStatusAddFriend());
                    }

                    if (contact.getmRoomId() != -1) {
                        values.put(ROOM, String.valueOf(contact.getmRoomId()));
                    }
                    if (contact.getmRelationship() != -1) {
                        values.put(RELATIONSHIP, contact.getmRelationship());
                    }

                    synchronized (db) {
                        db.update(TABLE_CONTACT, values, ID + " = ?", new String[]{String.valueOf(contact.getId())});
                    }
                }
                return null;
            }
        }.execute();
    }

    public int saveOrUpdateContact(Contact contact) {
        Log.e("BBBBB", "dzoooooooooo TableContact saveOrUpdateContact 5");
        //tra ve trang thai ban be(relationship) cua contact
        int mResult = -1;
        SQLiteDatabase db = getReadableDatabase();
        synchronized (db) {
            Cursor cursor = db.query(TABLE_CONTACT, new String[]{ID}
                    , ID + " = ? AND " + ADMIN + " = ? "
                    , new String[]{String.valueOf(contact.getId()), String.valueOf(Member.getInstance(context).getId())}
                    , null, null, null);

            ContentValues values = new ContentValues();
            values.put(ID, contact.getId());
            values.put(NAME, contact.getName());
            values.put(PHONE, contact.getPhone());
            values.put(URLAVATAR, String.valueOf(contact.getUrlavatar()));
            values.put(LASTACTIVE, String.valueOf(contact.getLastactive()));

            if (cursor.getCount() > 0) {
                if (contact.getmRoomId() != -1) {
                    values.put(ROOM, String.valueOf(contact.getmRoomId()));
                }
                if (contact.getmRelationship() != -1) {
                    values.put(RELATIONSHIP, contact.getmRelationship());
                }
                if (contact.getmStatusAddFriend() != 1) {
                    values.put(STATUSADDFRIEND, contact.getmStatusAddFriend());
                }

                db.update(TABLE_CONTACT, values, ID + " = ?", new String[]{String.valueOf(contact.getId())});
            } else {
                values.put(STATUSADDFRIEND, contact.getmStatusAddFriend());
                values.put(ROOM, String.valueOf(contact.getmRoomId()));
                values.put(RELATIONSHIP, contact.getmRelationship());
                values.put(ADMIN, Member.getInstance(context).getId());
                db.insert(TABLE_CONTACT, null, values);
                return contact.getmRelationship();
            }
            cursor.close();

            //kiem tra relationship
            Cursor cursor1 = db.query(TABLE_CONTACT
                    , new String[]{RELATIONSHIP}
                    , ID + " = ? AND " + ADMIN + " = ?"
                    , new String[]{String.valueOf(contact.getId()), String.valueOf(Member.getInstance(context).getId())}
                    , null, null, null);
            if (cursor1.getCount() > 0) {
                cursor1.moveToNext();

                String relationship = cursor1.getString(0);
                switch (relationship) {
                    case "1":
                        //tra ve 1 neu la ban be
                        mResult = 1;
                        break;
                }
            }
            cursor1.close();
        }
        return mResult;
    }

    public void saveOrUpdateContact(List<Contact> contacts) {
        Log.e("BBBBB", "dzoooooooooo TableContact saveOrUpdateContact 6");
        SQLiteDatabase db = getReadableDatabase();
        synchronized (db) {
            for (Contact contact : contacts) {
                Cursor cursor = db.query(TABLE_CONTACT, new String[]{ID}
                        , ID + " = ?"
                        , new String[]{String.valueOf(contact.getId())}
                        , null, null, null);

                ContentValues values = new ContentValues();
                values.put(ID, contact.getId());
                values.put(NAME, contact.getName());
                values.put(PHONE, contact.getPhone());
                values.put(URLAVATAR, String.valueOf(contact.getUrlavatar()));
                values.put(LASTACTIVE, String.valueOf(contact.getLastactive()));

                if (cursor.getCount() > 0) {
                    if (contact.getmStatusAddFriend() != 1) {
                        values.put(STATUSADDFRIEND, contact.getmStatusAddFriend());
                    }
                    if (contact.getmRoomId() != -1) {
                        values.put(ROOM, String.valueOf(contact.getmRoomId()));
                    }
                    if (contact.getmRelationship() != -1) {
                        values.put(RELATIONSHIP, contact.getmRelationship());
                    }
                    db.update(TABLE_CONTACT, values, ID + " = ?", new String[]{String.valueOf(contact.getId())});
                } else {
                    values.put(STATUSADDFRIEND, contact.getmStatusAddFriend());
                    values.put(ROOM, String.valueOf(contact.getmRoomId()));
                    values.put(RELATIONSHIP, contact.getmRelationship());
                    values.put(ADMIN, Member.getInstance(context).getId());
                    db.insert(TABLE_CONTACT, null, values);
                }
                cursor.close();
            }
        }
    }

    public void updateLastActiveContact(int idContact, long time) {
        Log.e("BBBBB", "dzoooooooooo TableContact 7");
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                SQLiteDatabase db = getReadableDatabase();
                ContentValues values = new ContentValues();
                values.put(LASTACTIVE, String.valueOf(time));
                synchronized (db) {
                    db.update(TABLE_CONTACT, values, ID + " = ?", new String[]{String.valueOf(idContact)});
                }
                return null;
            }
        }.execute();
    }

    public Contact updateRelationship(int idContact, int mRelationship, int statusFriend) {
        Log.e("BBBBB", "dzoooooooooo TableContact 8");
        SQLiteDatabase db = getReadableDatabase();
        Contact mContact = new Contact();
        synchronized (db) {
            ContentValues values = new ContentValues();
            values.put(RELATIONSHIP, mRelationship);
            values.put(STATUSADDFRIEND, statusFriend);
//            if (mRelationship == 1) {
//                values.put(STATUSADDFRIEND, 0);
//            }
            db.update(TABLE_CONTACT, values, ID + " = ?", new String[]{String.valueOf(idContact)});

            Cursor cursor = db.query(TABLE_CONTACT, null,
                    ID + " = ? AND " + ADMIN + " = ?", new String[]{String.valueOf(idContact), String.valueOf(Member.getInstance(context).getId())}, null, null, null);
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                String phone = cursor.getString(2);
                String urlavatar = cursor.getString(3);
                int roomId = cursor.getInt(4);
                int relationship = cursor.getInt(5);
                int statusaddfriend = cursor.getInt(6);
                long lastActive = cursor.getLong(7);
                mContact.setId(id);
                mContact.setPhone(phone);
                mContact.setName(name);
                mContact.setUrlavatar(urlavatar);
                mContact.setLastactive(lastActive);
                mContact.setmStatusAddFriend(statusaddfriend);
                mContact.setmRoomId(roomId);
                mContact.setmRelationship(relationship);
            }
            cursor.close();
        }
        return mContact;
    }

    public Contact updateStatusAddfriend(int idContact, int mStatusAddfriend) {
        Log.e("BBBBB", "dzoooooooooo TableContact 9");
        SQLiteDatabase db = getReadableDatabase();
        Contact mContact = new Contact();
        synchronized (db) {
            ContentValues values = new ContentValues();
            values.put(STATUSADDFRIEND, mStatusAddfriend);
            db.update(TABLE_CONTACT, values, ID + " = ?", new String[]{String.valueOf(idContact)});
            Cursor cursor = db.query(TABLE_CONTACT, null,
                    ID + " = ? AND " + ADMIN + " = ?", new String[]{String.valueOf(idContact), String.valueOf(Member.getInstance(context).getId())}, null, null, null);
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                String phone = cursor.getString(2);
                String urlavatar = cursor.getString(3);
                int roomId = cursor.getInt(4);
                int relationship = cursor.getInt(5);
                int statusaddfriend = cursor.getInt(6);
                long lastActive = cursor.getLong(7);
                mContact.setId(id);
                mContact.setPhone(phone);
                mContact.setName(name);
                mContact.setUrlavatar(urlavatar);
                mContact.setLastactive(lastActive);
                mContact.setmStatusAddFriend(statusaddfriend);
                mContact.setmRoomId(roomId);
                mContact.setmRelationship(relationship);
            }
            cursor.close();
        }
        return mContact;
    }

    public void deleteContact(int id) {
        Log.e("BBBBB", "dzoooooooooo TableContact 10");
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                SQLiteDatabase db = getReadableDatabase();
                synchronized (db) {
                    db.delete(TABLE_CONTACT, ID + " = ? AND " + ADMIN + " = ?"
                            , new String[]{String.valueOf(id), String.valueOf(Member.getInstance(context).getId())});
                }
                return null;
            }
        }.execute();
    }

    public void updateRelationship(List<Contact> contacts) {
        Log.e("BBBBB", "dzoooooooooo TableContact 11");
        SQLiteDatabase db = getReadableDatabase();
        synchronized (db) {
            for (Contact c : contacts) {
                ContentValues values = new ContentValues();
                values.put(RELATIONSHIP, c.getmRelationship());
                if (c.getmRelationship() == 1) {
                    values.put(STATUSADDFRIEND, 0);
                } else {
                    values.put(STATUSADDFRIEND, c.getmStatusAddFriend());
                }
                db.update(TABLE_CONTACT, values, ID + " = ?", new String[]{String.valueOf(c.getId())});
            }
        }
    }

    public void deleteContacts(List<Contact> contacts) {
        Log.e("BBBBB", "dzoooooooooo TableContact 12");
        SQLiteDatabase db = getReadableDatabase();
        synchronized (db) {
            for (Contact c : contacts) {
                db.delete(TABLE_CONTACT
                        , ID + " = ?"
                        , new String[]{String.valueOf(c.getId())});
            }
        }
    }

    public void deleteContacts() {
        Log.e("BBBBB", "dzoooooooooo TableContact 13");
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                SQLiteDatabase db = getReadableDatabase();
                synchronized (db) {
                    db.execSQL("DELETE FROM " + TABLE_CONTACT);
                }
                return null;
            }
        }.execute();
    }

    public int checkRelationship(int idContact) {
        Log.e("BBBBB", "dzoooooooooo TableContact 14");
        int mResult = -1;
        SQLiteDatabase db = getReadableDatabase();
        synchronized (db) {
            Cursor cursor = db.query(TABLE_CONTACT
                    , new String[]{RELATIONSHIP}
                    , ID + " = ? AND " + ADMIN + " = ?"
                    , new String[]{String.valueOf(idContact), String.valueOf(Member.getInstance(context).getId())}
                    , null, null, null);
            if (cursor.getCount() > 0) {
                cursor.moveToNext();

                String relationship = cursor.getString(cursor.getColumnIndex(RELATIONSHIP));
                switch (relationship) {
                    case "0":
                        //tra ve 0 neu co trong danh ba va khong la ban be
                        mResult = 0;
                        break;
                    case "1":
                        //tra ve 1 neu la ban be
                        mResult = 1;
                        break;
                }
            }
        }
        return mResult;
    }
}
