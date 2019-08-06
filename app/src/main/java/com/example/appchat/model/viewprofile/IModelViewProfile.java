package com.example.appchat.model.viewprofile;

import com.example.appchat.callback.ICallBackToPresenter;
import com.example.appchat.objectclass.Avatar;

import java.util.Date;

public interface IModelViewProfile {
    void update(String token, String phone, String name, boolean gender, Date birthday, Avatar avatar, ICallBackToPresenter iCallBackToPresenter);
}
