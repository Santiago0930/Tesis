package com.example.frutti.model

data class JwtAuthenticationResponse(
    val token: String = "",
    val email: String = "",
    val nombre: String = "",
    val rol: String = "",
)
