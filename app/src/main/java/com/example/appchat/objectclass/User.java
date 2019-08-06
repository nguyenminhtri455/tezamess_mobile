package com.example.appchat.objectclass;

import java.io.Serializable;
import java.util.Date;

public abstract class User implements Serializable {
    protected int id;
    protected String name;
    protected String phone;
    protected Date birthday;
    protected boolean gender;
    protected String urlavatar;
    protected long lastactive;

    protected User() {
    }

    protected User(int id, String name, String phone, String urlavatar) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.urlavatar = urlavatar;
    }

    protected User(int id, String name, String phone, Date birthday, boolean gender, String urlavatar, long lastactive) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.birthday = birthday;
        this.gender = gender;
        this.urlavatar = urlavatar;
        this.lastactive = lastactive;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public boolean isGender() {
        return gender;
    }

    public void setGender(boolean gender) {
        this.gender = gender;
    }

    public String getUrlavatar() {
        return urlavatar;
    }

    public void setUrlavatar(String urlavatar) {
        this.urlavatar = urlavatar;
    }

    public long getLastactive() {
        return lastactive;
    }

    public void setLastactive(long lastactive) {
        this.lastactive = lastactive;
    }
}
