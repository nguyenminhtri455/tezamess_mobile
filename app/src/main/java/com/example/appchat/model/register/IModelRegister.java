package com.example.appchat.model.register;

import com.example.appchat.callback.ICallBackToPresenter;

public interface IModelRegister {
    void register(String name, String phoneNumber, String password, ICallBackToPresenter iCallBackToPresenter);
}
