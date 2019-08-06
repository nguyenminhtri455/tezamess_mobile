package com.example.appchat.model.login;

import com.example.appchat.callback.ICallBackToPresenter;

public interface IModelLogin {
    void checkLogin(String userName, String passWord, ICallBackToPresenter callBackToPresenter);
    void getContacts(String token ,int id, ICallBackToPresenter callBackToPresenter);
}
