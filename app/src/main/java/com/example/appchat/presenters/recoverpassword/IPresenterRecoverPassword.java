package com.example.appchat.presenters.recoverpassword;

public interface IPresenterRecoverPassword {
    void checkValid(String resetCode, String passWord, String reenterPassword);
    void checkValidEmail(String email);

    void checkUpdate(String resetCode, String passWord, String reenterPassword);
    void getResetCode(String code);
}
