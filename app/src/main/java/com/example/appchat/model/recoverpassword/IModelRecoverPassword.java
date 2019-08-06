package com.example.appchat.model.recoverpassword;

import com.example.appchat.callback.ICallBackToPresenter;

public interface IModelRecoverPassword {
    void checkResetCode(String email, ICallBackToPresenter callBackToPresenter);

    void checkPassword(String email, String passWord, String reenterPassword, ICallBackToPresenter callBackToPresenter);
}
