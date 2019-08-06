package com.example.appchat.presenters.tabcontact.searchfriend;

import java.util.List;

public interface IPresenterSearchFriend {
    void searchUser(String token, String phoneNumber);
    void checkValid(String phoneNumber);
    void getContactsNoFriend(String token, List<String> listPhone);
}
