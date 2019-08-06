package com.example.appchat.model.tabsetting.changepassword;

import com.example.appchat.callback.ICallBackToPresenter;

public interface IModelChangePass {
    void checkPassword(String token, String phoneNumber, String passWord, ICallBackToPresenter callBackToPresenter);
}
