package com.example.frutti.retrofit;

import com.example.frutti.model.Usuario;
import com.example.frutti.model.UsuarioUpdate;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface UsuarioApi {

    @POST("/usuario/registrarUsuario")
    Call<Usuario> registrarUsuario(@Body Usuario usuario);
    @GET("/usuario/obtenerUsuario/{email}")
    Call<Usuario> obtenerUsuario(@Path("email") String email);

    @GET("/usuario/obtenerid/{email}")
    Call<Long> obtenerIdUsuario(@Path("email") String email);

    @PATCH("/usuario/actualizarUsuario/{id}")
    Call<Usuario> actualizarUsuario(@Path("id") Long id, @Body UsuarioUpdate usuario);

    @DELETE("/usuario/eliminar/{id}")
    Call<Void> eliminarUsuario(@Path("id") Long id);

}
