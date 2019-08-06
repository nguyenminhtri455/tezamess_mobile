package com.example.appchat.widget.retrofit;


import com.example.appchat.callback.ICallBackToModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitClient {

            public static final String baseUrl = "http://tezamess-tezamess.b9ad.pro-us-east-1.openshiftapps.com/";
//    public static final String baseUrl = "http://192.168.0.103:8080/";
//    public static final String baseUrl = "http://192.168.43.74:8080/";
//    public static final String baseUrl = "http://172.16.26.173:8080/";

            public static final String webSocketUrl = "ws://tezamess-tezamess.b9ad.pro-us-east-1.openshiftapps.com/ws/websocket";
//    public static final String webSocketUrl = "ws://192.168.0.103:8080/ws/websocket";
//    public static final String webSocketUrl = "ws://172.16.26.173:8080/ws/websocket";
//    public static final String webSocketUrl = "ws://192.168.43.74:8080/ws/websocket";

            public static final String pathImage = "http://tezamess-tezamess.b9ad.pro-us-east-1.openshiftapps.com/profile/";
//        public static final String pathImage = "http://192.168.0.103:8080/profile/";
//    public static final String pathImage = "http://172.16.26.173:8080/profile/";
//    public static final String pathImage = "http://192.168.43.74:8080/profile/";

    private static Retrofit retrofit = null;

    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            OkHttpClient builder = new OkHttpClient.Builder()
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .build();
            Gson gson = new GsonBuilder().setLenient().create();
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(builder)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }

    public static void excute(Call<String> call, final ICallBackToModel callBackToModel) {
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                String s = null;
                try {
                    if (response.isSuccessful()) {
                        s = response.body();
                    } else {
                        s = response.errorBody().string();
                    }
                    List<String> headers = response.headers().values("token");
                    if (headers.size() > 0) {
                        JSONObject jsonObject = new JSONObject(s);
                        jsonObject.put("token", headers.get(0));
                        s = jsonObject.toString();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                callBackToModel.callBack(s);
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                callBackToModel.callBack(t.toString());
            }
        });
    }
}
