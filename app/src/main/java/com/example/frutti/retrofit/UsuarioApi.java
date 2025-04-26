package com.example.frutti.retrofit;

import com.example.frutti.model.Usuario;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface UsuarioApi {

    @POST("/usuario/registrarUsuario")
    Call<Usuario> registrarUsuario(@Body Usuario usuario);
    @GET("/usuario/obtenerUsuario/{email}")
    Call<Usuario> obtenerUsuario(@Path("email") String email);

    @GET("/usuario/obtenerid/{email}")
    Call<Long> obtenerIdUsuario(@Path("email") String email);

}
