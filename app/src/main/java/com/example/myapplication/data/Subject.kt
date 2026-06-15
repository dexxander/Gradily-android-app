package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "subjects",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["lecturerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("lecturerId")]
)
data class Subject(
    @PrimaryKey(autoGenerate = true) val subjectId: Int = 0,
    val courseName: String,
    val creditHours: Int,
    val lecturerId: Int
)
