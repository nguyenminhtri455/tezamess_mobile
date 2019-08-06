package com.example.appchat.objectclass;

import java.io.Serializable;

public class ChatMessage implements Serializable {
    private int id;
    private long createdate;
    private Object file;
    private String body;
    private int room;
    private int user;
    private StatusMessage status;
    private TypeMessage typeMessage;

    public enum StatusMessage {
        Sending(0), Sent(1), Received(2), Seen(3), Error(4), Online(5), Offline(6);

        private int status;

        private StatusMessage(int status) {
            this.status = status;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }
    }

    public enum TypeMessage {
        Chat(0), Notify(1), Response(2), Image(3), File(4);

        private int status;

        private TypeMessage(int status) {
            this.status = status;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }
    }

    public ChatMessage() {
    }

    public ChatMessage(int id, long createdate, String body, int room, int user, StatusMessage status, TypeMessage type) {
        this.id = id;
        this.createdate = createdate;
        this.body = body;
        this.room = room;
        this.user = user;
        this.status = status;
        this.typeMessage = type;
    }

    public ChatMessage(long createdate, String body, int room, int user) {
        this.createdate = createdate;
        this.body = body;
        this.room = room;
        this.user = user;
    }

    public ChatMessage(long createdate, Object file, int room, int user, TypeMessage typeMessage) {
        this.createdate = createdate;
        this.file = file;
        this.room = room;
        this.user = user;
        this.typeMessage = typeMessage;
    }

    public ChatMessage(int id, long createdate, String body, int room, int user, StatusMessage status) {
        this.id = id;
        this.createdate = createdate;
        this.body = body;
        this.room = room;
        this.user = user;
        this.status = status;
    }

    public ChatMessage(int id, long createdate, String body, int room, int user, TypeMessage typeMessage) {
        this.id = id;
        this.createdate = createdate;
        this.body = body;
        this.room = room;
        this.user = user;
        this.typeMessage = typeMessage;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getCreatedate() {
        return createdate;
    }

    public void setCreatedate(long createdate) {
        this.createdate = createdate;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Object getFile() {
        return file;
    }

    public void setFile(Object file) {
        this.file = file;
    }

    public int getRoom() {
        return room;
    }

    public void setRoom(int group) {
        this.room = group;
    }

    public int getUser() {
        return user;
    }

    public void setUser(int user) {
        this.user = user;
    }

    public StatusMessage getStatus() {
        return status;
    }

    public void setStatus(StatusMessage status) {
        this.status = status;
    }

    public TypeMessage getTypeMessage() {
        return typeMessage;
    }

    public void setTypeMessage(TypeMessage typeMessage) {
        this.typeMessage = typeMessage;
    }

    public void cloneChatMessage(ChatMessage chatMessage){
        setId(chatMessage.getId());
        setBody(chatMessage.getBody());
        setRoom(chatMessage.getRoom());
        setUser(chatMessage.getUser());
        setCreatedate(chatMessage.getCreatedate());
        setStatus(chatMessage.getStatus());
        setTypeMessage(chatMessage.getTypeMessage());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;

        if (!(obj instanceof ChatMessage))
            return false;

        ChatMessage chatMessage = (ChatMessage) obj;

        if ((this.getUser() == chatMessage.getUser()
                && this.getRoom() == chatMessage.getRoom()
                && this.getBody().equals(chatMessage.getBody()) && this.getCreatedate() == chatMessage.getCreatedate())
                || this.getId() == chatMessage.getId()) {
            return true;
        }

        return false;
    }
}
