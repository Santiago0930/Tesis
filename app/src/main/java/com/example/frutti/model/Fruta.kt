package com.example.frutti.model

import java.time.LocalDate

data class Fruta(
    var id: Long? = null,
    var nombre: String = "",
    var estado: String = "",
    var precio: Float = 0.0f,
    var peso: Float = 0.0f,
    var lugarAnalisis: String = "",
    var fechaAnalisis: LocalDate = LocalDate.now(),
    var usuarioId: Long? = null,
)
