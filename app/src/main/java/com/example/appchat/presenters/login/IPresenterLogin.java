package com.example.appchat.presenters.login;

import com.example.appchat.objectclass.Contact;

import java.util.List;

public interface IPresenterLogin {
    //login
    void loginAccount(String userName, String passWord);
    void checkValid(String userName, String passWord);
    //get_contacts
    void getContacts(String token, int id, List<Contact> contacts);
}
