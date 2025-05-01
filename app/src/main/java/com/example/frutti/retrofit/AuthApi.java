package com.example.frutti.retrofit;

import com.example.frutti.model.JwtAuthenticationResponse;
import com.example.frutti.model.LoginRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {
    @POST("/auth/login")
    Call<JwtAuthenticationResponse> login(@Body LoginRequest request);
}
