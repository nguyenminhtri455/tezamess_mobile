package com.example.appchat.model.tabprofile.verificationemail;

import com.example.appchat.callback.ICallBackToPresenter;

public interface IModelVerificationEmail {
    void checkEmail(String token, String phone, String email, ICallBackToPresenter callBackToPresenter);
}
