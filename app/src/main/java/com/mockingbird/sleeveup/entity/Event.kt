package com.mockingbird.sleeveup.entity

import com.google.gson.annotations.SerializedName

data class Event(
    @SerializedName("date")
    val date: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("event_organizer")
    val eventOrganizer: String,
    @SerializedName("event_type")
    val eventType: String,
    @SerializedName("img")
    val img: String,
    @SerializedName("location")
    val location: String,
    @SerializedName("materials")
    val materials: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("presenter")
    val presenter: String,
    @SerializedName("register_link")
    val registerLink: String,
    @SerializedName("time")
    val time: String
)