package com.dev.salim.ChatApp.network;

import com.dev.salim.ChatApp.Utilities.Notifications.MyResponse;
import com.dev.salim.ChatApp.Utilities.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers({"Content-Type:application/json",
            "Authorization:key=AAAAkF8QMrc:APA91bEkgHu2j3B2VNXwLYibZFHK93nwa0U3ci2wjMuDNBExOs6Ht4aRwlhXV0Ho6FiDGffGKFk3FmLOFTWcTzMJvgaPzo3cTzTVKO1zNOX_SmHd20F1E9HrCWWZNlTOOazqyrW-tanr"})
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}


