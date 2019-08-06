package com.example.appchat.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.example.appchat.objectclass.ChatMessage;
import com.example.appchat.objectclass.Contact;
import com.example.appchat.objectclass.Member;
import com.example.appchat.objectclass.Room;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TableRoom extends TezamessSqlite {

    private static TableRoom tableRoom;
    private Context context;

    private TableRoom(Context context) {
        super(context);
        this.context = context;
    }

    public static TableRoom getInstance(Context context) {
        if (tableRoom == null) {
            tableRoom = new TableRoom(context);
        }
        return tableRoom;
    }

    public List<Room> getRooms() {
        List<Room> rooms = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        synchronized (db) {
            Cursor cursor = db.query(TABLE_ROOM, null,
                    ADMIN + " = ?", new String[]{String.valueOf(Member.getInstance(context).getId())}, null, null, null);
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                int creator = cursor.getInt(2);
                String type = cursor.getString(3);
                String urlavatar = cursor.getString(4);
                Room room = new Room();
                room.setId(id);
                room.setName(name);
                room.setCreator(creator);
                room.setType(type);
                room.setUrlAvatar(urlavatar);
                rooms.add(room);
            }
            cursor.close();
        }
        return rooms;
    }

    public List<Room> getDoubleRoomsNotContact() {
        List<Room> rooms = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        synchronized (db) {
            Cursor cursor = db.query(TABLE_ROOM, null,
                    TYPE + " = ?"
                    , new String[]{"D"}
                    , null, null, null);
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                int creator = cursor.getInt(2);
                String type = cursor.getString(3);
                String urlavatar = cursor.getString(4);
                Room room = new Room();
                room.setId(id);
                room.setName(name);
                room.setCreator(creator);
                room.setType(type);
                room.setUrlAvatar(urlavatar);
                rooms.add(room);
            }
            cursor.close();
        }
        return rooms;
    }

    public List<Room> getRoomsAndContacts() {
        List<Room> rooms = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        synchronized (db) {
            Cursor cursor = db.query(TABLE_ROOM, null,
                    null, null, null, null, null);
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                int creator = cursor.getInt(2);
                String type = cursor.getString(3);
                String urlavatar = cursor.getString(4);
                Room room = new Room();
                room.setId(id);
                room.setName(name);
                room.setCreator(creator);
                room.setType(type);
                room.setUrlAvatar(urlavatar);
                rooms.add(room);

                switch (room.getType()) {
                    case "D":
                        List<Contact> contactsInRoom = TableContact.getInstance(context).getContactsInRoom(room.getId());
                        room.setContacts(contactsInRoom);
                        room.setMembers(contactsInRoom.size());
                        break;
                    case "G":
                        List<Contact> contactsInManyRoom = TableParticipation.getInstance(context).getContactsInRoom(room.getId());
                        room.setContacts(contactsInManyRoom);
                        room.setMembers(contactsInManyRoom.size());
                        break;
                }

            }
            cursor.close();
        }
        return rooms;
    }

    public List<Room> getManyRoomsAndContacts() {
        List<Room> rooms = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        synchronized (db) {
            Cursor cursor = db.query(TABLE_ROOM, null,
                    TYPE + " = ?"
                    , new String[]{"G"}
                    , null, null, null);
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                int creator = cursor.getInt(2);
                String type = cursor.getString(3);
                String urlavatar = cursor.getString(4);
                Room room = new Room();
                room.setId(id);
                room.setName(name);
                room.setCreator(creator);
                room.setType(type);
                room.setUrlAvatar(urlavatar);
                rooms.add(room);
                List<Contact> contactsInManyRoom = TableParticipation.getInstance(context).getContactsInRoom(room.getId());
                room.setContacts(contactsInManyRoom);
                room.setMembers(contactsInManyRoom.size());
            }
        }
        return rooms;
    }

    public Room getRoom(int idRoom) {
        Room r = new Room();
        SQLiteDatabase db = getReadableDatabase();
        synchronized (db) {
            Cursor cursor = db.query(TABLE_ROOM, null,
                    ID + " = ? AND " + ADMIN + " = ?", new String[]{String.valueOf(idRoom), String.valueOf(Member.getInstance(context).getId())}, null, null, null);
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                int creator = cursor.getInt(2);
                String type = cursor.getString(3);
                String urlavatar = cursor.getString(4);
                r.setId(id);
                r.setName(name);
                r.setCreator(creator);
                r.setType(type);
                r.setUrlAvatar(urlavatar);
            }
            cursor.close();
        }
        return r;
    }

    public Room getRoomAndContacts(int idRoom) {
        Room r = new Room();
        SQLiteDatabase db = getReadableDatabase();
        synchronized (db) {
            Cursor cursor = db.query(TABLE_ROOM, null,
                    ID + " = ? AND " + ADMIN + " = ?", new String[]{String.valueOf(idRoom)
                            , String.valueOf(Member.getInstance(context).getId())}, null, null, null);
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                int creator = cursor.getInt(2);
                String type = cursor.getString(3);
                String urlavatar = cursor.getString(4);
                r.setId(id);
                r.setName(name);
                r.setCreator(creator);
                r.setType(type);
                r.setUrlAvatar(urlavatar);

                List<Contact> contactsInRoom = null;
                switch (r.getType()) {
                    case "D":
                        contactsInRoom = TableContact.getInstance(context).getContactsInRoom(r.getId());
                        break;
                    case "G":
                        contactsInRoom = TableParticipation.getInstance(context).getContactsInRoom(r.getId());
                        break;
                }
                r.setContacts(contactsInRoom);
                r.setMembers(contactsInRoom.size());
            }
            cursor.close();
        }
        return r;
    }

    public void addRooms(List<Room> rooms) {
        Log.e("BBBBB","dzoooooooooo TableRoom 1");
        SQLiteDatabase db = getReadableDatabase();
        synchronized (db) {
            for (Room room : rooms) {
                Cursor cursor = db.query(TABLE_ROOM, new String[]{ID}
                        , ID + " = ?", new String[]{String.valueOf(room.getId())}, null, null, null);
                if (cursor.getCount() <= 0) {
                    ContentValues values = new ContentValues();
                    values.put(ID, room.getId());
                    values.put(NAME, room.getName());
                    values.put(CREATOR, String.valueOf(room.getCreator()));
                    values.put(TYPE, room.getType());
                    values.put(URLAVATAR, room.getUrlAvatar());
                    values.put(ADMIN, String.valueOf(Member.getInstance(context).getId()));
                    db.insert(TABLE_ROOM, null, values);
                }
            }
        }
    }


    public void addRoomsAndContact(List<Room> rooms) {
        Log.e("BBBBB","dzoooooooooo TableRoom addRoomsAndContact");
        SQLiteDatabase db = getReadableDatabase();
        synchronized (db) {
            for (Room room : rooms) {
                Cursor cursor = db.query(TABLE_ROOM, new String[]{ID}
                        , ID + " = ?", new String[]{String.valueOf(room.getId())}, null, null, null);
                if (cursor.getCount() <= 0) {
                    ContentValues values = new ContentValues();
                    values.put(ID, room.getId());
                    values.put(NAME, room.getName());
                    values.put(CREATOR, String.valueOf(room.getCreator()));
                    values.put(TYPE, room.getType());
                    values.put(URLAVATAR, room.getUrlAvatar());
                    values.put(ADMIN, String.valueOf(Member.getInstance(context).getId()));
                    db.insert(TABLE_ROOM, null, values);
                }

                TableContact.getInstance(context).saveOrUpdateContact(room.getContacts());

            }
        }
    }

    public boolean checkRoomExists(int idRoom) {
        Log.e("BBBBB","dzoooooooooo TableRoom checkRoomExists");
        boolean mReslut = false;
        SQLiteDatabase db = getReadableDatabase();
        synchronized (db) {
            Cursor query = db.query(TABLE_ROOM, new String[]{ID}
                    , ID + " = ?", new String[]{String.valueOf(idRoom)}, null, null, null);
            if (query.getCount() > 0) {
                query.close();
                mReslut = true;
            }
            query.close();
        }
        return mReslut;
    }

    public void addRoom(Room room) {
        Log.e("BBBBB","dzoooooooooo TableRoom addRoom");
        SQLiteDatabase db = getReadableDatabase();
        synchronized (db) {
            ContentValues values = new ContentValues();
            values.put(ID, room.getId());
            values.put(NAME, room.getName());
            values.put(CREATOR, String.valueOf(room.getCreator()));
            values.put(TYPE, room.getType());
            values.put(URLAVATAR, room.getUrlAvatar());
            values.put(ADMIN, String.valueOf(Member.getInstance(context).getId()));
            db.insert(TABLE_ROOM, null, values);
        }
    }

    public void updateRoom(Room room) {
        Log.e("BBBBB","dzoooooooooo TableRoom updateRoom");
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                SQLiteDatabase db = getReadableDatabase();
                ContentValues values = new ContentValues();
                values.put(NAME, room.getName());
                values.put(CREATOR, String.valueOf(room.getCreator()));
                values.put(TYPE, room.getType());
                values.put(URLAVATAR, room.getUrlAvatar());
                synchronized (db) {
                    db.update(TABLE_ROOM, values, ID + " = ? AND " + ADMIN + " = ?"
                            , new String[]{String.valueOf(room.getId()), String.valueOf(Member.getInstance(context).getId())});
                }
                return null;
            }
        }.execute();
    }

    public void deleteRoom(int id) {
        Log.e("BBBBB","dzoooooooooo TableRoom 6");
        SQLiteDatabase db = getReadableDatabase();
        synchronized (db) {
            db.delete(TABLE_ROOM, ID + " = ? AND " + ADMIN + " = ?"
                    , new String[]{String.valueOf(id), String.valueOf(Member.getInstance(context).getId())});
        }
    }

    public void deleteRooms() {
        Log.e("BBBBB","dzoooooooooo TableRoom deleteRooms");
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                SQLiteDatabase db = getReadableDatabase();
                synchronized (db) {
                    db.execSQL("DELETE FROM " + TABLE_ROOM);
                }
                return null;
            }
        }.execute();
    }

    public List<Room> getConversation() {
        Log.e("BBBBB","dzoooooooooo TableRoom getConversation");
        TableMessage tableMessage = TableMessage.getInstance(context);
        List<Room> rooms = new ArrayList<>();
        List<ChatMessage> lastChatMessages = tableMessage.getLastChatMessages();
        for (ChatMessage chatMessage : lastChatMessages) {
            Room room = getRoomAndContacts(chatMessage.getRoom());
            int quantity = tableMessage.getUnreadChatMessagesInRoom(chatMessage.getRoom());
            room.setLastChatMessage(chatMessage);
            room.setQuantityUnreadMessage(quantity);
            rooms.add(room);
        }
        Collections.sort(rooms, (o1, o2) -> (int) (o2.getLastChatMessage().getCreatedate() - o1.getLastChatMessage().getCreatedate()));
        return rooms;
    }

    public List<Room> getManyRoom() {
        Log.e("BBBBB","dzoooooooooo TableRoom getManyRoom");
        TableMessage tableMessage = TableMessage.getInstance(context);
        List<Room> manyRoomsAndContacts = getManyRoomsAndContacts();
        List<ChatMessage> lastChatMessages = tableMessage.getLastChatMessages(manyRoomsAndContacts);
        for (ChatMessage chatMessage : lastChatMessages) {
            Room room = getRoomAndContacts(chatMessage.getRoom());
            int quantity = tableMessage.getUnreadChatMessagesInRoom(chatMessage.getRoom());
            room.setLastChatMessage(chatMessage);
            room.setQuantityUnreadMessage(quantity);
            manyRoomsAndContacts.remove(room);
            manyRoomsAndContacts.add(0, room);
        }
        List<Room> listNull = new ArrayList<>();
        for (Room room : manyRoomsAndContacts) {
            if (room.getLastChatMessage() == null)
                listNull.add(room);
        }
        manyRoomsAndContacts.removeAll(listNull);
        Collections.sort(manyRoomsAndContacts, (o1, o2) -> (int) (o2.getLastChatMessage().getCreatedate() - o1.getLastChatMessage().getCreatedate()));
        manyRoomsAndContacts.addAll(listNull);
        return manyRoomsAndContacts;
    }
}
