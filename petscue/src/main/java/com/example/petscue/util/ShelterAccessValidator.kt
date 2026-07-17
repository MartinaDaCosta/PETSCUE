package com.example.petscue.util

object ShelterAccessValidator {

    fun canAccessShelterFeatures(
        role: String,
        approvalStatus: String
    ): Boolean {
        return role == "PROTECTORA" &&
                approvalStatus == "APPROVED"
    }
}