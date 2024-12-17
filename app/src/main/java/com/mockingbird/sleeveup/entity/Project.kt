package com.mockingbird.sleeveup.entity

data class Project(
    val name: String = "",
    val description: String = "",
    val type: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val link: String? = null
)