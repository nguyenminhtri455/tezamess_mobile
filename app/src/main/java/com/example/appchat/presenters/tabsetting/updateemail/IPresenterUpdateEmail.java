package com.example.appchat.presenters.tabsetting.updateemail;

public interface IPresenterUpdateEmail {
    void checkValid(String passWord, String reenterPassword);

    void checkUpdate(String token, String phoneNumber, String passWord);
}
