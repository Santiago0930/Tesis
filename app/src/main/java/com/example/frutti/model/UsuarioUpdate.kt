package com.example.frutti.model

data class UsuarioUpdate(
    var nombre: String = "",
    var email: String = "",
    val edad: Int? = null // Add this line if missing
)
