package com.example.appchat.views.recoverpassword;

public interface IViewRecoverPassword {
    void dataError(String messgae);
    void connectError(String messgae);
    void sentResetCode(String message);
    void updateSucess(String messgae);
    void updateFail(String messgae);
    void getResetCodeSuccess(String email);
}
