package com.example.appchat.model.register;

import android.util.Log;

import com.example.appchat.callback.ICallBackToModel;
import com.example.appchat.callback.ICallBackToPresenter;
import com.example.appchat.widget.retrofit.DataClient;
import com.example.appchat.widget.retrofit.RetrofitClient;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Retrofit;

public class ModelRegister implements IModelRegister {

    private Retrofit retrofit;
    private DataClient dataClient;
    private Call<String> call;

    @Override
    public void register(String name, String phoneNumber, String password, final ICallBackToPresenter iCallBackToPresenter) {
        JSONObject jsonRegister = new JSONObject();

        try {
            jsonRegister.put("name",name);
            jsonRegister.put("phone",phoneNumber);
            jsonRegister.put("password",password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        retrofit = RetrofitClient.getRetrofit();
        dataClient = retrofit.create(DataClient.class);
        call = dataClient.register(jsonRegister.toString());

        RetrofitClient.excute(call, new ICallBackToModel() {
            @Override
            public void callBack(String s) {
                Log.d("BBBBB",s);
                iCallBackToPresenter.callBack(s);
            }
        });


    }
}
