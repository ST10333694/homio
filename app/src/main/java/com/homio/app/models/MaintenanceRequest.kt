package com.homio.app.models

import com.google.firebase.Timestamp

data class MaintenanceRequest(
    val requestId: String = "",
    val tenantId: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val status: String = "Submitted",
    val createdDate: Timestamp? = null
)
