package com.example.appchat.views.home.tabcontact.searchfriend.view;

import com.example.appchat.objectclass.Contact;

import java.util.List;

public interface IViewSeachFriend {
    void dataError(String s);

    void searchSucessUser(Contact contact);

    void searchSucessMember();

    void searchFail(String s);

    void connectError(String messgae);

    void loadContact(List<Contact> contacts);


}
