package com.example.appchat.views.register;

public interface IViewRegister {
    void validateError(String error);
    void registerSuccess(String message);
    void registerFailed(String message);
    void connectError(String message);
}
