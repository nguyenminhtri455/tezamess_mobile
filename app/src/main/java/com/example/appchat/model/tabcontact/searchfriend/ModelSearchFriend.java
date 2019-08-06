package com.example.appchat.model.tabcontact.searchfriend;

import com.example.appchat.callback.ICallBackToPresenter;
import com.example.appchat.widget.retrofit.DataClient;
import com.example.appchat.widget.retrofit.RetrofitClient;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;

public class ModelSearchFriend implements IModelSearchFriend {
    private Retrofit retrofit;
    private DataClient dataClient;
    private Call<String> call;

    @Override
    public void searchUser(String token, String phoneNumber, final ICallBackToPresenter callBackToPresenter) {

        JSONObject object = new JSONObject();
        try {
            object.put("phone", phoneNumber);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        retrofit = RetrofitClient.getRetrofit();
        dataClient = retrofit.create(DataClient.class);
        call = dataClient.serachFriend(token, object.toString());
        RetrofitClient.excute(call, s -> callBackToPresenter.callBack(s));
    }

    @Override
    public void getContactsNoFriend(String token, List<String> listPhone, ICallBackToPresenter callBackToPresenter) {
        Gson gson = new Gson();

        JSONArray object = null;
        try {
            object = new JSONArray(gson.toJson(listPhone));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        retrofit = RetrofitClient.getRetrofit();
        dataClient = retrofit.create(DataClient.class);
        call = dataClient.getContactsNotFriend(token, object.toString());

        RetrofitClient.excute(call, s -> callBackToPresenter.callBack(s));
    }
}
