package com.example.myapplication.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GradilyDao {
    @Insert
    suspend fun insertUser(user: User): Long

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Update
    suspend fun updateUser(user: User)

    @Insert
    suspend fun insertSubject(subject: Subject)

    @Update
    suspend fun updateSubject(subject: Subject)

    @Delete
    suspend fun deleteSubject(subject: Subject)

    @Query("SELECT * FROM subjects WHERE lecturerId = :lecturerId")
    fun getSubjectsByLecturer(lecturerId: Int): Flow<List<Subject>>

    @Query("SELECT * FROM subjects WHERE subjectId = :subjectId LIMIT 1")
    suspend fun getSubjectById(subjectId: Int): Subject?

    @Insert
    suspend fun insertStudent(student: Student): Long

    @Update
    suspend fun updateStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)

    @Query("SELECT * FROM students WHERE subjectId = :subjectId")
    fun getStudentsBySubject(subjectId: Int): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE email = :email")
    fun getStudentsByEmail(email: String): Flow<List<Student>>

    @Insert
    suspend fun insertAssessment(assessment: Assessment)

    @Update
    suspend fun updateAssessment(assessment: Assessment)

    @Query("SELECT * FROM assessments WHERE studentId = :studentId LIMIT 1")
    fun getAssessmentByStudent(studentId: Int): Flow<Assessment?>
}
