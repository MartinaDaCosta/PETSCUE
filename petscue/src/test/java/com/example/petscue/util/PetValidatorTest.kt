package com.example.petscue.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PetValidatorTest {

    @Test
    fun validate_rechaza_nombre_vacio() {
        val result = PetValidator.validate(
            name = "",
            species = "Perro",
            breed = "Mestizo"
        )

        assertFalse(result.isValid)

        assertEquals(
            "El nombre de la mascota es obligatorio.",
            result.errorMessage
        )
    }

    @Test
    fun validate_rechaza_especie_vacia() {
        val result = PetValidator.validate(
            name = "Luna",
            species = "",
            breed = "Mestizo"
        )

        assertFalse(result.isValid)

        assertEquals(
            "La especie de la mascota es obligatoria.",
            result.errorMessage
        )
    }

    @Test
    fun validate_rechaza_raza_vacia() {
        val result = PetValidator.validate(
            name = "Luna",
            species = "Perro",
            breed = ""
        )

        assertFalse(result.isValid)

        assertEquals(
            "La raza de la mascota es obligatoria.",
            result.errorMessage
        )
    }

    @Test
    fun validate_acepta_mascota_con_datos_completos() {
        val result = PetValidator.validate(
            name = "Luna",
            species = "Perro",
            breed = "Mestizo"
        )

        assertTrue(result.isValid)
        assertNull(result.errorMessage)
    }

    @Test
    fun validate_acepta_nombre_con_un_solo_caracter() {
        val result = PetValidator.validate(
            name = "L",
            species = "Perro",
            breed = "Mestizo"
        )

        assertTrue(result.isValid)
        assertNull(result.errorMessage)
    }
}