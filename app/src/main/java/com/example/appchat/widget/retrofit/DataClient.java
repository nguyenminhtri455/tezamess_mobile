package com.example.appchat.widget.retrofit;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface DataClient {

    @POST("tezamess/api-login")
    @Headers({"Content-Type: application/json;charset=UTF-8"})
    Call<String> loginData(@Body String body);

    @POST("tezamess/api-register")
    @Headers({"Content-Type: application/json;charset=UTF-8"})
    Call<String> register(@Body String body);

    @POST("tezamess/api/search-user")
    @Headers({"Content-Type: application/json;charset=UTF-8"})
    Call<String> serachFriend(@Header("authorization") String token, @Body String body);

    @POST("tezamess/api-resetcode")
    @Headers({"Content-Type: application/json;charset=UTF-8"})
    Call<String> getResetCode(@Body String body);

    @POST("tezamess/api-recoverpassword")
    @Headers({"Content-Type: application/json;charset=UTF-8"})
    Call<String> recoverPassword(@Body String body);

    @PUT("tezamess/api/update-user")
    @Headers({"Content-Type: application/json;charset=UTF-8"})
    Call<String> updateInfoMember(@Header("authorization") String token, @Body String body);

    @POST("tezamess/api/user-using-app")
    @Headers({"Content-Type: application/json;charset=UTF-8"})
    Call<String> getContactsNotFriend(@Header("authorization") String token, @Body String body);

    @POST("tezamess/api/get-friends")
    @Headers({"Content-Type: application/json;charset=UTF-8"})
    Call<String> getFriends(@Header("authorization") String token, @Body String body);

    @PUT("tezamess/api/change-password")
    @Headers({"Content-Type: application/json;charset=UTF-8"})
    Call<String> changePassword(@Header("authorization") String token, @Body String body);

    @PUT("tezamess/api/update-email")
    @Headers({"Content-Type: application/json;charset=UTF-8"})
    Call<String> updateEmail(@Header("authorization") String token, @Body String body);

    @POST("tezamess/api-savemessage")
    @Headers({"Content-Type: application/json;charset=UTF-8"})
    Call<String> saveMessage(@Body String body);

    @POST("tezamess/api/createRoom")
    @Headers({"Content-Type: application/json;charset=UTF-8"})
    Call<String> createRoom(@Header("authorization") String token, @Body String body);

    @PUT("tezamess/api/updateRoom")
    @Headers({"Content-Type: application/json;charset=UTF-8"})
    Call<String> updateRoom(@Header("authorization") String token, @Body String body);

    @POST("tezamess/api/postStatus")
    @Headers({"Content-Type: application/json;charset=UTF-8"})
    Call<String> postStatus(@Header("authorization") String token, @Body String body);

    @POST("tezamess/api/getStatuses")
    @Headers({"Content-Type: application/json;charset=UTF-8"})
    Call<String> getStatuses(@Header("authorization") String token, @Body String body);

    @POST("tezamess/api/loadMoreStatused")
    @Headers({"Content-Type: application/json;charset=UTF-8"})
    Call<String> loadMoreStatused(@Header("authorization") String token, @Body String body);
}
