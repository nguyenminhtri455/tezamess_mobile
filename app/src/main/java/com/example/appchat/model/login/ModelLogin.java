package com.example.appchat.model.login;

import com.example.appchat.callback.ICallBackToModel;
import com.example.appchat.callback.ICallBackToPresenter;
import com.example.appchat.widget.retrofit.DataClient;
import com.example.appchat.widget.retrofit.RetrofitClient;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Retrofit;

public class ModelLogin implements IModelLogin {

    private Retrofit retrofit;
    private DataClient dataClient;
    private Call<String> call;

    public void checkLogin(String userName, String passWord, final ICallBackToPresenter callBackToPresenter) {
        JSONObject object = new JSONObject();
        try {
            object.put("phone", userName);
            object.put("password", passWord);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        retrofit = RetrofitClient.getRetrofit();
        dataClient = retrofit.create(DataClient.class);
        call = dataClient.loginData(object.toString());


        RetrofitClient.excute(call, new ICallBackToModel() {
            @Override
            public void callBack(String s) {
                callBackToPresenter.callBack(s);
            }
        });
    }

    @Override
    public void getContacts(String token, int id, ICallBackToPresenter callBackToPresenter) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", id);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        retrofit = RetrofitClient.getRetrofit();
        dataClient = retrofit.create(DataClient.class);
        call = dataClient.getFriends(token, jsonObject.toString());

        RetrofitClient.excute(call, s -> {
            callBackToPresenter.callBack(s);
        });
    }
}