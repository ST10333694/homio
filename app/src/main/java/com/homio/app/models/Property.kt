package com.homio.app.models

data class Property(
    val propertyId: String = "",
    val landlordId: String = "",
    val address: String = "",
    val propertyType: String = "",
    val rooms: Int = 0,
    val rentAmount: Double = 0.0,
    val depositAmount: Double = 0.0,
    val status: String = "Vacant"
)
