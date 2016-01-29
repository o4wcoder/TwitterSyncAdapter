package com.bignerdranch.android.twittersyncadapter.web;


import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

/**
 * Created by Chris Hare on 10/22/2015.
 */
public interface AuthenticationInterface {

@POST("/oauth/request_token")
    void fetchRequestToken(@Body String body, Callback<Response> callback);

@FormUrlEncoded
    @POST("/oauth/access_token")
    void fetchAccessToken(@Field("oauth_verifier") String verifier,Callback<Response> callback);

}
