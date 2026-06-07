package com.example.petscue.ui.navigation

object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val AUTH = "auth"
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val PENDING_APPROVAL = "pending_approval"
    const val MAIN = "main"
    const val ADMIN_APPROVAL = "admin_approval"
    const val ADD_PET = "add_pet"

    const val PET_DETAIL = "pet_detail/{petId}"
    const val EDIT_PET = "edit_pet/{petId}"

    fun petDetailRoute(petId: String) = "pet_detail/$petId"
    fun editPetRoute(petId: String) = "edit_pet/$petId"
}