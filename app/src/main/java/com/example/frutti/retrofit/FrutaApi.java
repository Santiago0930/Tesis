package com.example.frutti.retrofit;

import com.example.frutti.model.Fruta;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface FrutaApi {

    @POST("/fruta/registrarFruta")
    Call<Fruta> registrarFruta(@Body Fruta frutaNueva);

    @GET("/fruta/listarFrutas")
    Call<List<Fruta>> listarFrutas();

    @PATCH("/fruta/actualizarFruta/{id}")
    Call<String> actualizarFruta(@Path("id") Long id, @Body Fruta fruta);

    @DELETE("/fruta/eliminar/{id}")
    Call<Void> eliminarFruta(@Path("id") Long id);

    @GET("/fruta/historial/{id}")
    Call<List<Fruta>> obtenerHistorialFrutas(@Path ("id") Long id);

    @GET("/fruta/obtenerFruta/{idFruta}, {idUsuario}")
    Call<Fruta> obtenerFruta(@Path("idFruta") Long idFruta, @Path ("idUsuario") Long idUsuario);

}
