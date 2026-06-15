package com.example.myapplication.data

data class Announcement(
    var id: String = "",
    var subjectId: String = "",
    var title: String = "",
    var content: String = "",
    var timestamp: Long = System.currentTimeMillis()
)
