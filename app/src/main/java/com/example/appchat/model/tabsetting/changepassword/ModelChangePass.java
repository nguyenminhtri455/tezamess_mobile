package com.example.appchat.model.tabsetting.changepassword;

import android.util.Log;

import com.example.appchat.callback.ICallBackToModel;
import com.example.appchat.callback.ICallBackToPresenter;
import com.example.appchat.widget.retrofit.DataClient;
import com.example.appchat.widget.retrofit.RetrofitClient;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Retrofit;

public class ModelChangePass implements IModelChangePass {

    private Retrofit retrofit;
    private DataClient dataClient;
    private Call<String> call;

    @Override
    public void checkPassword(String token, String phoneNumber, String passWord, ICallBackToPresenter callBackToPresenter) {
        JSONObject object = new JSONObject();
        try {
            object.put("phone", phoneNumber);
            object.put("password", passWord);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        retrofit = RetrofitClient.getRetrofit();
        dataClient = retrofit.create(DataClient.class);
        call = dataClient.changePassword(token,object.toString());
        Log.d("BBBBB",object.toString());


        RetrofitClient.excute(call, new ICallBackToModel() {
            @Override
            public void callBack(String s) {
                callBackToPresenter.callBack(s);
                Log.d("BBBBB",s);
            }
        });
    }
}
