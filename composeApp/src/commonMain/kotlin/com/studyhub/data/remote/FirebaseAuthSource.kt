package com.studyhub.data.remote

import com.studyhub.domain.model.User
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth

class FirebaseAuthSource {
    private val auth = Firebase.auth

    suspend fun login(email: String, password: String): User {
        val result = auth.signInWithEmailAndPassword(email, password)
        val firebaseUser = result.user ?: throw Exception("Login gagal")
        return User(firebaseUser.uid, firebaseUser.email ?: "", firebaseUser.displayName ?: "")
    }

    suspend fun register(email: String, password: String, name: String): User {
        val result = auth.createUserWithEmailAndPassword(email, password)
        val firebaseUser = result.user ?: throw Exception("Register gagal")
        return User(firebaseUser.uid, firebaseUser.email ?: "", name)
    }

    suspend fun logout() = auth.signOut()

    fun getCurrentUser(): User? = auth.currentUser?.let {
        User(it.uid, it.email ?: "", it.displayName ?: "")
    }
}
