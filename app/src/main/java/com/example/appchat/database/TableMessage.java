package com.example.appchat.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.example.appchat.objectclass.ChatMessage;
import com.example.appchat.objectclass.Member;
import com.example.appchat.objectclass.Room;
import com.example.appchat.websocket.WebSocket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TableMessage extends TezamessSqlite {

    private static TableMessage tableMessage;
    private Context context;

    private TableMessage(Context context) {
        super(context);
        this.context = context;
    }

    public static TableMessage getInstance(Context context) {
        if (tableMessage == null) {
            tableMessage = new TableMessage(context);
        }
        return tableMessage;
    }


    public List<ChatMessage> getChatMessages(int idRoom, int start, int limit) {
        List<ChatMessage> messages = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        synchronized (db) {
            Cursor cursor = db.query(TABLE_MESSAGE, null,
                    ADMIN + " = ? AND " + ROOM + " = ?"
                    , new String[]{String.valueOf(Member.getInstance(context).getId()), String.valueOf(idRoom)}
                    , null, null, TIME + " DESC", start + "," + limit);

            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                int sender = cursor.getInt(1);
                int room = cursor.getInt(2);
                String content = cursor.getString(3);
                long time = cursor.getLong(4);
                String status = cursor.getString(5);
                String type = cursor.getString(6);
                ChatMessage message = new ChatMessage();
                message.setId(id);
                message.setUser(sender);
                message.setRoom(room);
                message.setBody(content);
                message.setCreatedate(time);
                switch (status) {
                    case WebSocket.SENT:
                        message.setStatus(ChatMessage.StatusMessage.Sent);
                        break;
                    case WebSocket.RECEIVED:
                        message.setStatus(ChatMessage.StatusMessage.Received);
                        break;
                    case WebSocket.SEEN:
                        message.setStatus(ChatMessage.StatusMessage.Seen);
                        break;
                }

                switch (type) {
                    case WebSocket.CHAT:
                        message.setTypeMessage(ChatMessage.TypeMessage.Chat);
                        break;
                    case WebSocket.NOTIFY:
                        message.setTypeMessage(ChatMessage.TypeMessage.Notify);
                        break;
                    case WebSocket.IMAGE:
                        message.setTypeMessage(ChatMessage.TypeMessage.Image);
                        break;
                }
                messages.add(message);
            }
            cursor.close();
        }
        Collections.sort(messages, (o1, o2) -> (int) (o1.getCreatedate() - o2.getCreatedate()));
        return messages;
    }

    public List<ChatMessage> getLastChatMessages() {
        List<ChatMessage> messages = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        synchronized (db) {
            Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_MESSAGE +
                    " WHERE " + KEY + " IN" +
                    " (SELECT MAX(" + KEY + ") FROM " + TABLE_MESSAGE +
                    " GROUP BY " + ROOM +
                    " ORDER BY " + ID + ")", null);
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                int sender = cursor.getInt(1);
                int room = cursor.getInt(2);
                String content = cursor.getString(3);
                long time = cursor.getLong(4);
                String status = cursor.getString(5);
                String type = cursor.getString(6);
                ChatMessage message = new ChatMessage();
                message.setId(id);
                message.setUser(sender);
                message.setRoom(room);
                message.setBody(content);
                message.setCreatedate(time);
                switch (status) {
                    case WebSocket.SENT:
                        message.setStatus(ChatMessage.StatusMessage.Sent);
                        break;
                    case WebSocket.RECEIVED:
                        message.setStatus(ChatMessage.StatusMessage.Received);
                        break;
                    case WebSocket.SEEN:
                        message.setStatus(ChatMessage.StatusMessage.Seen);
                        break;
                }

                switch (type) {
                    case WebSocket.CHAT:
                        message.setTypeMessage(ChatMessage.TypeMessage.Chat);
                        break;
                    case WebSocket.NOTIFY:
                        message.setTypeMessage(ChatMessage.TypeMessage.Notify);
                        break;
                    case WebSocket.IMAGE:
                        message.setTypeMessage(ChatMessage.TypeMessage.Image);
                        break;
                }
                messages.add(message);
            }
            cursor.close();
        }
        return messages;
    }

    public List<ChatMessage> getLastChatMessages(List<Room> rooms) {
        List<Integer> roomIds = new ArrayList<>();
        for (Room room : rooms) {
            roomIds.add(room.getId());
        }
        String ids = roomIds.toString().replaceAll("[\\[\\]\\s]", "");
        List<ChatMessage> messages = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        synchronized (db) {
            Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_MESSAGE +
                    " WHERE " + ROOM + " IN (" + ids + ") AND " + KEY + " IN" +
                    " (SELECT MAX(" + KEY + ") FROM " + TABLE_MESSAGE +
                    " GROUP BY " + ROOM +
                    " ORDER BY " + ID + ")", null);
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                int sender = cursor.getInt(1);
                int room = cursor.getInt(2);
                String content = cursor.getString(3);
                long time = cursor.getLong(4);
                String status = cursor.getString(5);
                String type = cursor.getString(6);
                ChatMessage message = new ChatMessage();
                message.setId(id);
                message.setUser(sender);
                message.setRoom(room);
                message.setBody(content);
                message.setCreatedate(time);
                switch (status) {
                    case WebSocket.SENT:
                        message.setStatus(ChatMessage.StatusMessage.Sent);
                        break;
                    case WebSocket.RECEIVED:
                        message.setStatus(ChatMessage.StatusMessage.Received);
                        break;
                    case WebSocket.SEEN:
                        message.setStatus(ChatMessage.StatusMessage.Seen);
                        break;
                }

                switch (type) {
                    case WebSocket.CHAT:
                        message.setTypeMessage(ChatMessage.TypeMessage.Chat);
                        break;
                    case WebSocket.NOTIFY:
                        message.setTypeMessage(ChatMessage.TypeMessage.Notify);
                        break;
                    case WebSocket.IMAGE:
                        message.setTypeMessage(ChatMessage.TypeMessage.Image);
                        break;
                }
                messages.add(message);
            }
            cursor.close();
        }
        return messages;
    }

    public int getTotalUnreadChatMessages() {
        int count = 0;
        SQLiteDatabase db = getReadableDatabase();
        synchronized (db) {
            Cursor cursor = db.query(TABLE_MESSAGE, new String[]{ID},
                    ADMIN + " = ? AND " + STATUS + " = ? AND " + SENDER + " != ?"
                    , new String[]{String.valueOf(Member.getInstance(context).getId()), "Received", String.valueOf(Member.getInstance(context).getId())}
                    , null, null, null, null);
            Cursor cursor1 = db.query(TABLE_MESSAGE, new String[]{ID},
                    ADMIN + " = ? " +
                            "AND " + STATUS + " = ? " +
                            "AND " + SENDER + " = ? " +
                            "AND " + TYPE + " = ?"
                    , new String[]{String.valueOf(Member.getInstance(context).getId())
                            , "Received"
                            , String.valueOf(Member.getInstance(context).getId())
                            , "Notify"}
                    , null, null, null, null);
            count = cursor.getCount();
            count += cursor1.getCount();
            cursor.close();
            cursor1.close();
        }
        return count;

    }

    public int getUnreadChatMessagesInRoom(int idRoom) {
        int count = 0;
        SQLiteDatabase db = getReadableDatabase();
        synchronized (db) {
            Cursor cursor = db.query(TABLE_MESSAGE, new String[]{ID},
                    ADMIN + " = ? " +
                            "AND " + STATUS + " = ? " +
                            "AND " + ROOM + " = ? " +
                            "AND " + SENDER + " != ?"
                    , new String[]{String.valueOf(Member.getInstance(context).getId())
                            , "Received", String.valueOf(idRoom)
                            , String.valueOf(Member.getInstance(context).getId())}
                    , null, null, null, null);
            Cursor cursor1 = db.query(TABLE_MESSAGE, new String[]{ID},
                    ADMIN + " = ? " +
                            "AND " + STATUS + " = ? " +
                            "AND " + ROOM + " = ? " +
                            "AND " + SENDER + " = ? " +
                            "AND " + TYPE + " = ?"
                    , new String[]{String.valueOf(Member.getInstance(context).getId())
                            , "Received", String.valueOf(idRoom)
                            , String.valueOf(Member.getInstance(context).getId())
                            , "Notify"}
                    , null, null, null, null);
            count = cursor.getCount();
            count += cursor1.getCount();
            cursor.close();
            cursor1.close();
        }
        return count;
    }

    public void addChatMessages(List<ChatMessage> messages) {
        SQLiteDatabase db = getReadableDatabase();
        synchronized (db) {
            for (ChatMessage message : messages) {
                ContentValues values = new ContentValues();
                values.put(ID, message.getId());
                values.put(SENDER, String.valueOf(message.getUser()));
                values.put(ROOM, String.valueOf(message.getRoom()));
                values.put(CONTENT, message.getBody());
                values.put(TIME, String.valueOf(message.getCreatedate()));
                values.put(STATUS, message.getStatus().name());
                values.put(TYPE, message.getTypeMessage().name());
                values.put(ADMIN, String.valueOf(Member.getInstance(context).getId()));
                db.insert(TABLE_MESSAGE, null, values);
            }
        }
    }

    public void addChatMessagesAndCheckExist(List<ChatMessage> messages) {
        SQLiteDatabase db = getReadableDatabase();
        synchronized (db) {
            for (ChatMessage message : messages) {
                Cursor cursor = db.query(TABLE_MESSAGE
                        , new String[]{ID}
                        , ID + " = ? AND " + ADMIN + " = ?"
                        , new String[]{String.valueOf(message.getId()), String.valueOf(Member.getInstance(context).getId())}
                        , null, null, null, null);
                if (cursor.getCount() > 0) {
                    ContentValues values = new ContentValues();
                    values.put(STATUS, message.getStatus().name());
                    db.update(TABLE_MESSAGE, values, ID + " = ? AND " + ADMIN + " = ?"
                            , new String[]{String.valueOf(message.getId()), String.valueOf(Member.getInstance(context).getId())});
                    cursor.close();
                    continue;
                }
                ContentValues values = new ContentValues();
                values.put(ID, message.getId());
                values.put(SENDER, String.valueOf(message.getUser()));
                values.put(ROOM, String.valueOf(message.getRoom()));
                values.put(CONTENT, message.getBody());
                values.put(TIME, String.valueOf(message.getCreatedate()));
                values.put(STATUS, message.getStatus().name());
                values.put(TYPE, message.getTypeMessage().name());
                values.put(ADMIN, String.valueOf(Member.getInstance(context).getId()));
                db.insert(TABLE_MESSAGE, null, values);
                cursor.close();
            }
        }
    }

    public void addChatMessage(ChatMessage message) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                SQLiteDatabase db = getReadableDatabase();
                synchronized (db) {
                    Cursor cursor = db.query(TABLE_MESSAGE
                            , new String[]{ID}
                            , ID + " = ? AND " + ADMIN + " = ?"
                            , new String[]{String.valueOf(message.getId()), String.valueOf(Member.getInstance(context).getId())}
                            , null, null, null, null);
                    if (cursor.getCount() <= 0) {
                        ContentValues values = new ContentValues();
                        values.put(ID, message.getId());
                        values.put(SENDER, String.valueOf(message.getUser()));
                        values.put(ROOM, String.valueOf(message.getRoom()));
                        values.put(CONTENT, message.getBody());
                        values.put(TIME, String.valueOf(message.getCreatedate()));
                        values.put(STATUS, message.getStatus().name());
                        values.put(TYPE, message.getTypeMessage().name());
                        values.put(ADMIN, String.valueOf(Member.getInstance(context).getId()));
                        db.insert(TABLE_MESSAGE, null, values);
                    }
                    cursor.close();
                }
                return null;
            }
        }.execute();
    }

    public void updateChatMessage(ChatMessage message) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                SQLiteDatabase db = getReadableDatabase();
                ContentValues values = new ContentValues();
                values.put(STATUS, message.getStatus().name());
                synchronized (db) {
                    db.update(TABLE_MESSAGE, values, ADMIN + " = ? AND "
                                    + ID + " = ?"
                            , new String[]{String.valueOf(Member.getInstance(context).getId())
                                    , String.valueOf(message.getId())});
                }
                return null;
            }
        }.execute();
    }

    public void updateStatusChatMessages(ChatMessage message) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                SQLiteDatabase db = getReadableDatabase();
                synchronized (db) {
                    Cursor cursor = db.query(TABLE_MESSAGE
                            , new String[]{STATUS}
                            , ID + " = ?"
                            , new String[]{String.valueOf(message.getId())}
                            , null, null, null);

                    if (cursor.getCount() > 0) {
                        cursor.moveToNext();
                        String mStatus = cursor.getString(0);
                        if (!mStatus.equals("Seen")) {
                            ContentValues values = new ContentValues();
                            values.put(STATUS, message.getStatus().name());

//                            db.update(TABLE_MESSAGE, values, ADMIN + " = ?" +
//                                            " AND " + ROOM + " = ?" +
//                                            " AND " + ID + " BETWEEN -1 AND ?"
//                                    , new String[]{String.valueOf(Member.getInstance(context).getId())
//                                            , String.valueOf(message.getRoom())
//                                            , String.valueOf(message.getId())});

//                            String status = null;
//                            switch (message.getStatus()) {
//                                case Received:
//                                    status = ChatMessage.StatusMessage.Sent.name();
//                                    break;
//                                case Seen:
//                                    status = ChatMessage.StatusMessage.Received.name();
//                                    break;
//                            }

                            db.update(TABLE_MESSAGE, values, ADMIN + " = ?" +
                                            " AND " + STATUS + " = ?" +
                                            " AND " + ROOM + " = ?" +
                                            " AND " + ID + " BETWEEN -1 AND ?"
                                    , new String[]{String.valueOf(Member.getInstance(context).getId())
                                            , mStatus, String.valueOf(message.getRoom()), String.valueOf(message.getId())});
                        }
                    }
                    cursor.close();
                }

                return null;
            }
        }.execute();
    }

    public void updateStatusChatMessagesNotify(int idRoom) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                SQLiteDatabase db = getReadableDatabase();
                synchronized (db) {
                    ContentValues values = new ContentValues();
                    values.put(STATUS, ChatMessage.StatusMessage.Seen.name());
                    db.update(TABLE_MESSAGE, values, STATUS + " IN (?,?)" +
                                    " AND " + ROOM + " = ?" +
                                    " AND " + TYPE + " = ?"
                            , new String[]{"Sent", "Received", String.valueOf(idRoom), "Notify"});
                }
                return null;
            }
        }.execute();
    }

    public void deleteChatMessages(int idRoom) {
        SQLiteDatabase db = getReadableDatabase();
        synchronized (db) {
            db.delete(TABLE_MESSAGE, ROOM + " = ? AND " + ADMIN + " = ?"
                    , new String[]{String.valueOf(idRoom), String.valueOf(Member.getInstance(context).getId())});
        }
    }

    public void deleteChatMessages() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                SQLiteDatabase db = getReadableDatabase();
                synchronized (db) {
                    db.execSQL("DELETE FROM " + TABLE_MESSAGE);
                }
                return null;
            }
        }.execute();
    }
}
