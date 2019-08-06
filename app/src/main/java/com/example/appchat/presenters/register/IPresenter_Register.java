package com.example.appchat.presenters.register;

public interface IPresenter_Register {
    void checkValidate(String name, String phoneNumber, String password);
    void register(String name, String phoneNumber, String password);
}
