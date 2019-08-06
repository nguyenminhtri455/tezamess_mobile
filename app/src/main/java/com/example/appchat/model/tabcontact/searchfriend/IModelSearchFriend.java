package com.example.appchat.model.tabcontact.searchfriend;

import com.example.appchat.callback.ICallBackToPresenter;

import java.util.List;

public interface IModelSearchFriend {
    void searchUser(String token, String phoneNumber,ICallBackToPresenter callBackToPresenter);
    void getContactsNoFriend(String token , List<String> listPhone, ICallBackToPresenter callBackToPresenter);
}
