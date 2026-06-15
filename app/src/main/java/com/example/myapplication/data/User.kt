package com.example.myapplication.data

data class User(
    var id: String = "",
    var name: String = "",
    var email: String = "",
    var role: String = "LECTURER", // "LECTURER" or "STUDENT"
    var profilePicUri: String? = null
)
