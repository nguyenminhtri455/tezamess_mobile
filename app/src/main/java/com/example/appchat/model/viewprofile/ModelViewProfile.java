package com.example.appchat.model.viewprofile;

import com.example.appchat.callback.ICallBackToModel;
import com.example.appchat.callback.ICallBackToPresenter;
import com.example.appchat.objectclass.Avatar;
import com.example.appchat.widget.retrofit.DataClient;
import com.example.appchat.widget.retrofit.RetrofitClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import retrofit2.Call;
import retrofit2.Retrofit;

public class ModelViewProfile implements IModelViewProfile {

    private Retrofit retrofit;
    private DataClient dataClient;
    private Call<String> call;

    @Override
    public void update(String token, String phone, String name, boolean gender, Date birthday, Avatar avatar, final ICallBackToPresenter iCallBackToPresenter) {
        JSONObject jsonUpdate = new JSONObject();
        JSONObject jsonAvatar;

        try {
            if (avatar != null) {
                jsonAvatar = new JSONObject();
                jsonAvatar.put("valueBase64", avatar.getValueBase64());
                jsonAvatar.put("name", avatar.getName());
                jsonUpdate.put("avatar", jsonAvatar.toString());
            }

            jsonUpdate.put("phone", phone);
            jsonUpdate.put("name", name);
            jsonUpdate.put("gender", gender);
            jsonUpdate.put("birthday", birthday.getTime());


        } catch (JSONException e) {
            e.printStackTrace();
        }


        retrofit = RetrofitClient.getRetrofit();
        dataClient = retrofit.create(DataClient.class);
        call = dataClient.updateInfoMember(token, jsonUpdate.toString());

        RetrofitClient.excute(call, new ICallBackToModel() {
            @Override
            public void callBack(String s) {
                iCallBackToPresenter.callBack(s);
//                Log.d("BBB", s);
            }
        });
    }
}
