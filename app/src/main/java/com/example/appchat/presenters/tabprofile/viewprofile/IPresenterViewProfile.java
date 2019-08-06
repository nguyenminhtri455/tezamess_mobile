package com.example.appchat.presenters.tabprofile.viewprofile;

import com.example.appchat.objectclass.Avatar;

import java.util.Date;

public interface IPresenterViewProfile {
    void update(String token, String phone, String name, boolean gender, Date birthday, Avatar avatar);
}
