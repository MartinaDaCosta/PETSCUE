package com.example.petscue.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShelterAccessValidatorTest {

    @Test
    fun protectora_aprobada_puede_acceder_a_funciones_exclusivas() {
        val result = ShelterAccessValidator.canAccessShelterFeatures(
            role = "PROTECTORA",
            approvalStatus = "APPROVED"
        )

        assertTrue(result)
    }

    @Test
    fun protectora_pendiente_no_puede_acceder_a_funciones_exclusivas() {
        val result = ShelterAccessValidator.canAccessShelterFeatures(
            role = "PROTECTORA",
            approvalStatus = "PENDING"
        )

        assertFalse(result)
    }

    @Test
    fun protectora_rechazada_no_puede_acceder_a_funciones_exclusivas() {
        val result = ShelterAccessValidator.canAccessShelterFeatures(
            role = "PROTECTORA",
            approvalStatus = "REJECTED"
        )

        assertFalse(result)
    }

    @Test
    fun usuario_estandar_no_puede_acceder_a_funciones_de_protectora() {
        val result = ShelterAccessValidator.canAccessShelterFeatures(
            role = "USER",
            approvalStatus = "APPROVED"
        )

        assertFalse(result)
    }
}