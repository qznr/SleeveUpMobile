package com.mockingbird.sleeveup.entity

data class Certificate(
    val name: String = "",
    val type: String? = null,
    val startDate: String? = null,
    val skills: List<String>? = null,
    val tools: List<String>? = null,
    val link: String? = null
)
