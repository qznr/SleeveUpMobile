package com.mockingbird.sleeveup.entity

data class Experience(
    val name: String = "",
    val description: String = "",
    val location: String? = null,
    val role: String = "",
    val startDate: String = "",
    val endDate: String? = null,
    val skills: List<String>? = null
)