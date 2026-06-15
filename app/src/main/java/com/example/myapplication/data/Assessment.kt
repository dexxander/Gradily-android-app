package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "assessments",
    foreignKeys = [
        ForeignKey(
            entity = Student::class,
            parentColumns = ["studentId"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["studentId"], unique = true)] // one assessment record per student per subject context
)
data class Assessment(
    @PrimaryKey(autoGenerate = true) val gradeId: Int = 0,
    val studentId: Int,
    val quiz1: Double = 0.0,
    val assign1: Double = 0.0,
    val midterm: Double = 0.0,
    val quiz2: Double = 0.0,
    val assign2: Double = 0.0,
    val finalExam: Double = 0.0
)
