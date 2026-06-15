package com.example.myapplication

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.*
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

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
        Log.d("GradilyLogin", "Attempting login: email=$email, role=$role")
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener
                Log.d("GradilyLogin", "Auth succeeded, uid=$uid. Fetching Firestore doc...")
                firestore.collection("users").document(uid).get()
                    .addOnSuccessListener { snapshot ->
                        Log.d("GradilyLogin", "Firestore doc exists=${snapshot.exists()}, data=${snapshot.data}")
                        if (snapshot.exists()) {
                            val user = snapshot.toObject(User::class.java)
                            Log.d("GradilyLogin", "Parsed user: id=${user?.id}, email=${user?.email}, role='${user?.role}'")
                            
                            if (user == null) {
                                // Document exists but failed to parse — recreate it
                                Log.w("GradilyLogin", "User document exists but could not be parsed. Recreating...")
                                val newUser = User(id = uid, email = email, role = role)
                                firestore.collection("users").document(uid).set(newUser)
                                    .addOnSuccessListener {
                                        _currentUser.value = newUser
                                        onResult(true, "Login successful (profile fixed)")
                                    }
                                    .addOnFailureListener { e ->
                                        auth.signOut()
                                        onResult(false, "Failed to fix profile: ${e.message}")
                                    }
                            } else if (user.role.isBlank()) {
                                // Role field is empty — update it with the selected role
                                Log.w("GradilyLogin", "User has blank role. Setting role=$role")
                                firestore.collection("users").document(uid).update("role", role)
                                    .addOnSuccessListener {
                                        val updatedUser = user.copy(role = role)
                                        _currentUser.value = updatedUser
                                        onResult(true, "Login successful")
                                    }
                                    .addOnFailureListener { e ->
                                        auth.signOut()
                                        onResult(false, "Failed to update role: ${e.message}")
                                    }
                            } else if (user.role == role) {
                                _currentUser.value = user
                                onResult(true, "Login successful")
                            } else {
                                Log.w("GradilyLogin", "Role mismatch: stored='${user.role}', requested='$role'")
                                auth.signOut()
                                onResult(false, "Account registered as ${user.role}, not $role")
                            }
                        } else {
                            // User exists in Auth but not in Firestore — create the document
                            Log.w("GradilyLogin", "No Firestore doc found. Creating new user doc with role=$role")
                            val newUser = User(id = uid, email = email, role = role)
                            firestore.collection("users").document(uid).set(newUser)
                                .addOnSuccessListener {
                                    _currentUser.value = newUser
                                    onResult(true, "Login successful (profile created)")
                                }
                                .addOnFailureListener { e ->
                                    auth.signOut()
                                    onResult(false, "Failed to create profile: ${e.message}")
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("GradilyLogin", "Firestore fetch failed", e)
                        auth.signOut()
                        onResult(false, "Failed to retrieve user data: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                Log.e("GradilyLogin", "Auth failed", e)
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
                        _currentUser.value = user
                        onResult(true, "Account created successfully")
                    }
                    .addOnFailureListener { e ->
                        // Even if firestore fails, they are logged into Auth. Let's sign out to be safe.
                        auth.signOut()
                        onResult(false, "Failed to save user data")
                    }
            }
            .addOnFailureListener { e ->
                onResult(false, e.message ?: "Sign up failed")
            }
    }

    fun signInWithGoogle(context: Context, webClientId: String, role: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val credentialManager = CredentialManager.create(context)
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(webClientId)
                    .setAutoSelectEnabled(true)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(context, request)
                val credential = result.credential
                
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken
                    
                    val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                    val authResult = auth.signInWithCredential(authCredential).await()
                    
                    val uid = authResult.user?.uid ?: throw Exception("Google Auth successful but no user ID")
                    val email = authResult.user?.email ?: ""
                    
                    // Check if user document exists
                    val snapshot = firestore.collection("users").document(uid).get().await()
                    val existingUser = snapshot.toObject(User::class.java)
                    
                    if (existingUser != null) {
                        if (existingUser.role == role) {
                            _currentUser.value = existingUser
                            onResult(true, "Login successful")
                        } else {
                            auth.signOut()
                            onResult(false, "Account registered as different role")
                        }
                    } else {
                        // Create new user document
                        val newUser = User(id = uid, email = email, role = role)
                        firestore.collection("users").document(uid).set(newUser).await()
                        _currentUser.value = newUser
                        onResult(true, "Account created and logged in")
                    }
                } else {
                    onResult(false, "Invalid credential type")
                }
            } catch (e: Exception) {
                Log.e("GoogleAuth", "Failed", e)
                onResult(false, e.message ?: "Google Sign-In failed")
            }
        }
    }

    fun updateProfilePicture(uriString: String) {
        val user = _currentUser.value ?: return
        val uid = user.id
        if (uid.isEmpty()) return
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>()
                val uri = android.net.Uri.parse(uriString)
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (bitmap != null) {
                    // Compress to a reasonable size for Base64 (max 500x500)
                    val maxDim = 500f
                    val scale = Math.min(maxDim / bitmap.width, maxDim / bitmap.height)
                    val scaledBitmap = if (scale < 1f) {
                        android.graphics.Bitmap.createScaledBitmap(
                            bitmap, 
                            (bitmap.width * scale).toInt(), 
                            (bitmap.height * scale).toInt(), 
                            true
                        )
                    } else bitmap

                    val outputStream = java.io.ByteArrayOutputStream()
                    scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, outputStream)
                    val byteArray = outputStream.toByteArray()
                    val base64String = "data:image/jpeg;base64," + android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)

                    firestore.collection("users").document(uid).update("profilePicUri", base64String)
                        .addOnSuccessListener {
                            _currentUser.value = user.copy(profilePicUri = base64String)
                        }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateName(newName: String) {
        val user = _currentUser.value ?: return
        val uid = user.id
        if (uid.isEmpty()) return
        
        firestore.collection("users").document(uid).update("name", newName)
            .addOnSuccessListener {
                _currentUser.value = user.copy(name = newName)
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

    fun markAttendance(student: Student, attended: Boolean) {
        val newTotal = student.totalClasses + 1
        val newAttended = student.classesAttended + (if (attended) 1 else 0)
        firestore.collection("students").document(student.studentId).update(
            mapOf(
                "totalClasses" to newTotal,
                "classesAttended" to newAttended
            )
        )
    }

    fun getAnnouncements(subjectId: String? = null): Flow<List<com.example.myapplication.data.Announcement>> = callbackFlow {
        val collection = firestore.collection("announcements")
        val query = if (subjectId != null) collection.whereEqualTo("subjectId", subjectId) else collection
        val subscription = query.addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                trySend(snapshot.toObjects(com.example.myapplication.data.Announcement::class.java).sortedByDescending { it.timestamp })
            }
        }
        awaitClose { subscription.remove() }
    }

    fun postAnnouncement(subjectId: String, title: String, content: String) {
        val announcement = com.example.myapplication.data.Announcement(
            id = UUID.randomUUID().toString(),
            subjectId = subjectId,
            title = title,
            content = content,
            timestamp = System.currentTimeMillis()
        )
        firestore.collection("announcements").document(announcement.id).set(announcement)
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
        if (assessment.gradeId.isEmpty()) {
            val ref = firestore.collection("assessments").document()
            assessment.gradeId = ref.id
            ref.set(assessment)
        } else {
            firestore.collection("assessments").document(assessment.gradeId).set(assessment)
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

    fun getStudentsBySubject(subjectId: String): Flow<List<Student>> = callbackFlow {
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

    suspend fun getSubjectById(subjectId: String): Subject? {
        if (subjectId.isEmpty()) return null
        return try {
            val snapshot = firestore.collection("subjects").document(subjectId).get().await()
            snapshot.toObject(Subject::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getAssessmentsForStudents(studentIds: List<String>): Map<String, Assessment?> {
        val result = mutableMapOf<String, Assessment?>()
        if (studentIds.isEmpty()) return result
        
        try {
            // Fetch individually to avoid whereIn limit
            for (id in studentIds) {
                val snapshot = firestore.collection("assessments").whereEqualTo("studentId", id).limit(1).get().await()
                if (!snapshot.isEmpty) {
                    result[id] = snapshot.documents[0].toObject(Assessment::class.java)
                } else {
                    result[id] = null
                }
            }
        } catch (e: Exception) {
            Log.e("GradilyViewModel", "Error fetching assessments", e)
        }
        return result
    }

    fun observeAssessmentsForStudents(studentIds: List<String>): Flow<Map<String, Assessment?>> {
        if (studentIds.isEmpty()) return flowOf(emptyMap())
        val flows = studentIds.map { id ->
            getAssessmentByStudentId(id).map { id to it }
        }
        return combine(flows) { array: Array<Pair<String, Assessment?>> ->
            array.toMap()
        }
    }
}
