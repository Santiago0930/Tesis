package com.example.frutti.retrofit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import java.time.LocalDate;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitService {
    private Retrofit retrofit;

    public RetrofitService() {
        initializeRetrofit();
    }

    private void initializeRetrofit() {
        // Registrar adaptador personalizado para LocalDate
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>)
                        (json, type, context) -> LocalDate.parse(json.getAsString()))
                .registerTypeAdapter(LocalDate.class, (JsonSerializer<LocalDate>)
                        (src, typeOfSrc, context) -> new JsonPrimitive(src.toString()))
                .create();


        retrofit = new Retrofit.Builder()
                .baseUrl("http://ec2-18-116-62-7.us-east-2.compute.amazonaws.com:8080") // IP Amazon EC2
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }
}
