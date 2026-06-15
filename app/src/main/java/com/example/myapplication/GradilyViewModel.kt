package com.example.myapplication

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class GradilyViewModel(application: Application) : AndroidViewModel(application) {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _currentSubject = MutableStateFlow<Subject?>(null)
    val currentSubject: StateFlow<Subject?> = _currentSubject.asStateFlow()

    private val _currentStudent = MutableStateFlow<Student?>(null)
    val currentStudent: StateFlow<Student?> = _currentStudent.asStateFlow()

    init {
        // Restore user if already logged in
        auth.currentUser?.let { firebaseUser ->
            viewModelScope.launch {
                try {
                    val snapshot = firestore.collection("users").document(firebaseUser.uid).get().await()
                    val user = snapshot.toObject(User::class.java)
                    _currentUser.value = user
                } catch (e: Exception) {
                    Log.e("GradilyViewModel", "Error fetching user", e)
                }
            }
        }
    }

    fun login(email: String, password: String, role: String, onResult: (Boolean, String) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener
                firestore.collection("users").document(uid).get()
                    .addOnSuccessListener { snapshot ->
                        val user = snapshot.toObject(User::class.java)
                        if (user != null && user.role == role) {
                            _currentUser.value = user
                            onResult(true, "Login successful")
                        } else {
                            auth.signOut()
                            onResult(false, "Account registered as different role")
                        }
                    }
                    .addOnFailureListener {
                        auth.signOut()
                        onResult(false, "Failed to retrieve user data")
                    }
            }
            .addOnFailureListener { e ->
                onResult(false, e.message ?: "Login failed")
            }
    }

    fun signUp(email: String, password: String, role: String, onResult: (Boolean, String) -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            onResult(false, "Fields cannot be empty")
            return
        }
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener
                val user = User(id = uid, email = email, role = role)
                firestore.collection("users").document(uid).set(user)
                    .addOnSuccessListener {
                        onResult(true, "Account created successfully")
                    }
                    .addOnFailureListener { e ->
                        onResult(false, "Failed to save user data")
                    }
            }
            .addOnFailureListener { e ->
                onResult(false, e.message ?: "Sign up failed")
            }
    }

    fun updateProfilePicture(uri: String) {
        val user = _currentUser.value ?: return
        val uid = user.id
        if (uid.isEmpty()) return
        
        firestore.collection("users").document(uid).update("profilePicUri", uri)
            .addOnSuccessListener {
                _currentUser.value = user.copy(profilePicUri = uri)
            }
    }

    fun logout() {
        auth.signOut()
        _currentUser.value = null
        _currentSubject.value = null
        _currentStudent.value = null
    }

    fun getSubjects(): Flow<List<Subject>> = callbackFlow {
        val lecturerId = _currentUser.value?.id ?: ""
        if (lecturerId.isEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val subscription = firestore.collection("subjects")
            .whereEqualTo("lecturerId", lecturerId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    trySend(snapshot.toObjects(Subject::class.java))
                }
            }
        awaitClose { subscription.remove() }
    }

    fun createSubject(courseName: String, creditHours: Int) {
        val lecturerId = _currentUser.value?.id ?: return
        val ref = firestore.collection("subjects").document()
        val subject = Subject(subjectId = ref.id, courseName = courseName, creditHours = creditHours, lecturerId = lecturerId)
        ref.set(subject)
    }

    fun setCurrentSubject(subject: Subject) {
        _currentSubject.value = subject
    }

    fun deleteSubject(subject: Subject) {
        firestore.collection("subjects").document(subject.subjectId).delete()
        if (_currentSubject.value?.subjectId == subject.subjectId) {
            _currentSubject.value = null
        }
    }

    fun getStudents(): Flow<List<Student>> = callbackFlow {
        val subjectId = _currentSubject.value?.subjectId ?: ""
        if (subjectId.isEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val subscription = firestore.collection("students")
            .whereEqualTo("subjectId", subjectId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    trySend(snapshot.toObjects(Student::class.java))
                }
            }
        awaitClose { subscription.remove() }
    }

    fun createStudent(studentName: String, email: String) {
        val subjectId = _currentSubject.value?.subjectId ?: return
        val studentRef = firestore.collection("students").document()
        val studentId = studentRef.id
        val student = Student(studentId = studentId, studentName = studentName, email = email, subjectId = subjectId)
        
        studentRef.set(student).addOnSuccessListener {
            val assessmentRef = firestore.collection("assessments").document()
            val assessment = Assessment(gradeId = assessmentRef.id, studentId = studentId, subjectId = subjectId)
            assessmentRef.set(assessment)
        }
    }

    fun setCurrentStudent(student: Student) {
        _currentStudent.value = student
    }

    fun deleteStudent(student: Student) {
        firestore.collection("students").document(student.studentId).delete()
        if (_currentStudent.value?.studentId == student.studentId) {
            _currentStudent.value = null
        }
    }

    fun getAssessment(): Flow<Assessment?> = getAssessmentByStudentId(_currentStudent.value?.studentId ?: "")

    fun getAssessmentByStudentId(studentId: String): Flow<Assessment?> = callbackFlow {
        if (studentId.isEmpty()) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val subscription = firestore.collection("assessments")
            .whereEqualTo("studentId", studentId)
            .limit(1)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && !snapshot.isEmpty) {
                    trySend(snapshot.documents[0].toObject(Assessment::class.java))
                } else {
                    trySend(null)
                }
            }
        awaitClose { subscription.remove() }
    }

    fun updateAssessment(assessment: Assessment) {
        firestore.collection("assessments").document(assessment.gradeId).set(assessment)
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

    fun getEnrolledStudents(): Flow<List<Student>> = callbackFlow {
        val email = _currentUser.value?.email ?: ""
        if (email.isEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val subscription = firestore.collection("students")
            .whereEqualTo("email", email)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    trySend(snapshot.toObjects(Student::class.java))
                }
            }
        awaitClose { subscription.remove() }
    }

    suspend fun getSubjectById(subjectId: String): Subject? {
        if (subjectId.isEmpty()) return null
        return try {
            val snapshot = firestore.collection("subjects").document(subjectId).get().await()
            snapshot.toObject(Subject::class.java)
        } catch (e: Exception) {
            null
        }
    }
}
