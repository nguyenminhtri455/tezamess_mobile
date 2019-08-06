package com.example.appchat.model.tabsetting.changepassword.updateemail;

import com.example.appchat.callback.ICallBackToPresenter;

public interface IModelUpdateEmail {
    void checkPassword(String token, String phoneNumber, String passWord, ICallBackToPresenter callBackToPresenter);
}
