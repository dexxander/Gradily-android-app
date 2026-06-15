package com.example.myapplication.data

data class Assessment(
    var gradeId: String = "",
    var studentId: String = "",
    var subjectId: String = "", // Added to make querying easier if needed
    var quiz1: Double = 0.0,
    var assign1: Double = 0.0,
    var midterm: Double = 0.0,
    var quiz2: Double = 0.0,
    var assign2: Double = 0.0,
    var finalExam: Double = 0.0
)
