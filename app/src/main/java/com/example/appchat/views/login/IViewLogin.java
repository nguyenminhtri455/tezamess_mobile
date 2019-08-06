package com.example.appchat.views.login;

public interface IViewLogin {
    void dataError(String messgae);
    void loginSucess(String messgae);
    void loginFail(String messgae);
    void connectError(String messgae);
}
