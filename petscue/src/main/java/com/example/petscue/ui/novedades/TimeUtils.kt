package com.example.petscue.ui.novedades

fun tiempoRelativo(timestamp: Long): String {
    val ahora = System.currentTimeMillis()
    val diff  = ahora - timestamp

    val segundos = diff / 1_000
    val minutos  = diff / 60_000
    val horas    = diff / 3_600_000
    val dias     = diff / 86_400_000
    val semanas  = dias / 7
    val meses    = dias / 30
    val años     = dias / 365

    return when {
        segundos < 60   -> "hace ${segundos}s"
        minutos  < 60   -> "hace ${minutos}min"
        horas    < 24   -> "hace ${horas}h"
        dias     < 7    -> "hace ${dias}d"
        semanas  < 4    -> "hace ${semanas}sem"
        meses    < 12   -> "hace ${meses}mes"
        else            -> "hace ${años}año${if (años > 1) "s" else ""}"
    }
}