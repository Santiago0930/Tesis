package com.example.frutti.model

data class Usuario(
    var id: Long? = null,
    var email: String = "",
    var password: String = "",
    var nombre: String = "",
    var edad: Int = 0,
    var genero: String = ""
)
