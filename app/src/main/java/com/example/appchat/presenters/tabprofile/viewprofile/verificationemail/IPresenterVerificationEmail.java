package com.example.appchat.presenters.tabprofile.viewprofile.verificationemail;

public interface IPresenterVerificationEmail {
    void checkValid(String email);

    void checkConfirm(String token, String phone, String email);
}
