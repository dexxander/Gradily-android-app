package com.example.myapplication.data

data class Student(
    var studentId: String = "",
    var studentName: String = "",
    var email: String = "",
    var subjectId: String = "",
    var classesAttended: Int = 0,
    var totalClasses: Int = 0
)
