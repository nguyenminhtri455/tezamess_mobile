package com.example.appchat.objectclass;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Room implements Serializable {
    private int id;
    private String name;
    private int creator;
    private String type;
    private String urlAvatar;
    private int members;
    private List<Contact> contacts = new ArrayList<>();
    private ChatMessage lastChatMessage = null;
    private int quantityUnreadMessage = 0;

    public Room() {
    }

    public Room(int id) {
        this.id = id;
    }

    public Room(int id, String name, int creator, String type, int members) {
        this.id = id;
        this.name = name;
        this.creator = creator;
        this.type = type;
        this.members = members;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCreator() {
        return creator;
    }

    public void setCreator(int creator) {
        this.creator = creator;
    }

    public String getUrlAvatar() {
        return urlAvatar;
    }

    public void setUrlAvatar(String urlAvatar) {
        this.urlAvatar = urlAvatar;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getMembers() {
        return members;
    }

    public void setMembers(int members) {
        this.members = members;
    }

    public List<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
    }

    public ChatMessage getLastChatMessage() {
        return lastChatMessage;
    }

    public void setLastChatMessage(ChatMessage lastChatMessage) {
        this.lastChatMessage = lastChatMessage;
    }

    public int getQuantityUnreadMessage() {
        return quantityUnreadMessage;
    }

    public void setQuantityUnreadMessage(int quantityUnreadMessage) {
        this.quantityUnreadMessage = quantityUnreadMessage;
    }

    public void increaseQuantityUnreadMessage(int quantityUnreadMessage) {
        this.quantityUnreadMessage += quantityUnreadMessage;
    }

    public void reduceQuantityUnreadMessage(int quantityUnreadMessage) {
        this.quantityUnreadMessage -= quantityUnreadMessage;
    }

    public void cloneRoom(Room room) {
        setId(room.getId());
        setName(room.getName());
        setCreator(room.getCreator());
        setType(room.getType());
        setUrlAvatar(room.getUrlAvatar());
        setMembers(room.getMembers());
        this.contacts.addAll(room.getContacts());
        this.lastChatMessage = room.getLastChatMessage();
        setQuantityUnreadMessage(room.getQuantityUnreadMessage());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;

        if (!(obj instanceof Room)) return false;

        Room room = (Room) obj;

        if (this.getId() == room.getId()) {
            return true;
        }

        return false;
    }
}

