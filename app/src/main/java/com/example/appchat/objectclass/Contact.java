package com.example.appchat.objectclass;

import java.util.Date;

public class Contact extends User {
    private int mRelationship = -1;
    private int mRoomId = -1;
    private int mStatusAddFriend = 1;
    //0: Đã là bạn bè (relationship ==1)
    //1: Không là bạn bè(Chưa gửi y/c kết bạn)(rela == 0 | rela == -1)
    //2: Đã gửi y/c kết bạn(rela == 0 || rela == -1)
    //3: Đã nhận y/c kết bạn(rela == 0 || rela == -1)

    public int getmStatusAddFriend() {
        return mStatusAddFriend;
    }

    public void setmStatusAddFriend(int mStatusAddFriend) {
        this.mStatusAddFriend = mStatusAddFriend;
    }

    public Contact() {
        super();
    }

    public Contact(String phone) {
        this.phone = phone;
    }

    public Contact(int id) {
        this.id = id;
    }

    public Contact(int id, String name, String phone, String urlavatar) {
        super(id, name, phone, urlavatar);
    }

    public Contact(int id, String name, String phone, String urlavatar, int mRelationship) {
        super(id, name, phone, urlavatar);
        this.mRelationship = mRelationship;
    }

    public Contact(int id, String name, String phone, Date birthday, boolean gender, String urlavatar, long lastactive) {
        super(id, name, phone, birthday, gender, urlavatar, lastactive);
    }

    public int getmRelationship() {
        return mRelationship;
    }

    public void setmRelationship(int mRelationship) {
        this.mRelationship = mRelationship;
    }

    public int getmRoomId() {
        return mRoomId;
    }

    public void setmRoomId(int mRoomId) {
        this.mRoomId = mRoomId;
    }

    public void setContact(Contact c) {
        this.setId(c.getId());
        this.setPhone(c.getPhone());
        this.setName(c.getName());
        this.setUrlavatar(c.getUrlavatar());
        this.setLastactive(c.getLastactive());
//        this.setGender(c.isGender());
//        this.setBirthday(c.getBirthday());
        if (c.getmStatusAddFriend() != 1) {
            this.setmStatusAddFriend(c.getmStatusAddFriend());
        }
        if (c.getmRelationship() != -1) {
            this.setmRelationship(c.getmRelationship());
        }
        if (c.getmRoomId() != -1) {
            this.setmRoomId(c.getmRoomId());
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof Contact)) {
            return false;
        }

        Contact contact = (Contact) obj;
        if (this.id == contact.getId()) {
            return true;
        }
        return false;
    }
}
