package com.example.myapplication

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GradilyViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = GradilyDatabase.getDatabase(application).gradilyDao()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _currentSubject = MutableStateFlow<Subject?>(null)
    val currentSubject: StateFlow<Subject?> = _currentSubject.asStateFlow()

    private val _currentStudent = MutableStateFlow<Student?>(null)
    val currentStudent: StateFlow<Student?> = _currentStudent.asStateFlow()

    fun login(email: String, password: String, role: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = dao.getUserByEmail(email)
            if (user != null && user.password == password) {
                if (user.role == role) {
                    _currentUser.value = user
                    onResult(true, "Login successful")
                } else {
                    onResult(false, "Account registered as different role")
                }
            } else {
                onResult(false, "Invalid email or password")
            }
        }
    }

    fun signUp(email: String, password: String, role: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            if (email.isBlank() || password.isBlank()) {
                onResult(false, "Fields cannot be empty")
                return@launch
            }
            val existing = dao.getUserByEmail(email)
            if (existing != null) {
                onResult(false, "Email already exists")
                return@launch
            }
            val user = User(email = email, password = password, role = role)
            dao.insertUser(user)
            onResult(true, "Account created successfully")
        }
    }

    fun updateProfilePicture(uri: String) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val updatedUser = user.copy(profilePicUri = uri)
            dao.updateUser(updatedUser)
            _currentUser.value = updatedUser
        }
    }

    fun logout() {
        _currentUser.value = null
        _currentSubject.value = null
        _currentStudent.value = null
    }

    fun getSubjects() = dao.getSubjectsByLecturer(_currentUser.value?.id ?: -1)

    fun createSubject(courseName: String, creditHours: Int) {
        viewModelScope.launch {
            val lecturerId = _currentUser.value?.id ?: return@launch
            dao.insertSubject(Subject(courseName = courseName, creditHours = creditHours, lecturerId = lecturerId))
        }
    }

    fun setCurrentSubject(subject: Subject) {
        _currentSubject.value = subject
    }

    fun deleteSubject(subject: Subject) {
        viewModelScope.launch {
            dao.deleteSubject(subject)
            if (_currentSubject.value?.subjectId == subject.subjectId) {
                _currentSubject.value = null
            }
        }
    }

    fun getStudents() = dao.getStudentsBySubject(_currentSubject.value?.subjectId ?: -1)

    fun createStudent(studentName: String, email: String) {
        viewModelScope.launch {
            val subjectId = _currentSubject.value?.subjectId ?: return@launch
            val studentId = dao.insertStudent(Student(studentName = studentName, email = email, subjectId = subjectId))
            // Initialize empty assessment record for the new student
            dao.insertAssessment(Assessment(studentId = studentId.toInt()))
        }
    }

    fun setCurrentStudent(student: Student) {
        _currentStudent.value = student
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch {
            dao.deleteStudent(student)
            if (_currentStudent.value?.studentId == student.studentId) {
                _currentStudent.value = null
            }
        }
    }

    fun getAssessment() = dao.getAssessmentByStudent(_currentStudent.value?.studentId ?: -1)

    fun getAssessmentByStudentId(studentId: Int) = dao.getAssessmentByStudent(studentId)

    fun updateAssessment(assessment: Assessment) {
        viewModelScope.launch {
            dao.updateAssessment(assessment)
        }
    }

    fun calculateGPA(assessment: Assessment?): Double {
        if (assessment == null) return 0.0
        val totalMarks = assessment.quiz1 + assessment.assign1 + assessment.midterm + assessment.quiz2 + assessment.assign2 + assessment.finalExam
        val percentage = (totalMarks / 180.0) * 100
        return when {
            percentage >= 80 -> 4.0
            percentage >= 75 -> 3.67
            percentage >= 70 -> 3.33
            percentage >= 65 -> 3.0
            percentage >= 60 -> 2.67
            percentage >= 55 -> 2.33
            percentage >= 50 -> 2.0
            percentage >= 45 -> 1.67
            percentage >= 40 -> 1.33
            percentage >= 35 -> 1.0
            else -> 0.0
        }
    }

    // Student specific logic
    fun getEnrolledStudents() = dao.getStudentsByEmail(_currentUser.value?.email ?: "")

    suspend fun getSubjectById(subjectId: Int) = dao.getSubjectById(subjectId)
}
