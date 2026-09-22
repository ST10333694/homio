package com.homio.app.models

data class User(
    val userId: String = "",
    val fullName: String = "",
    val email: String = "",
    val userType: String = "", // "tenant" or "landlord"
    val phone: String = "",
    val language: String = "en"
)
