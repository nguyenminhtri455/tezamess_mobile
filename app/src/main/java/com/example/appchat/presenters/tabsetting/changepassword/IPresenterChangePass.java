package com.example.appchat.presenters.tabsetting.changepassword;

public interface IPresenterChangePass {
    void checkValid(String passWord, String reenterPassword);

    void checkUpdate(String token, String phoneNumber, String passWord);
}
