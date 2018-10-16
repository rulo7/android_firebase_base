package com.racobos.manhattan.datasource

import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthDataSource
@Inject
constructor() {
    private val firebaseAuth = FirebaseAuth.getInstance()

    fun login(email: String, password: String, onComplete: (Exception?) -> Unit) {
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            var exception: Exception? = task.exception?.toDomainException()
            if (task.result?.user?.isEmailVerified != true) {
                exception = UnverifiedEmailException()
                firebaseAuth.signOut()
            }
            onComplete(exception)
        }
    }

    fun register(email: String, password: String, onComplete: (Exception?) -> Unit) {
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { createTask ->
            if (createTask.exception != null && createTask.isSuccessful && createTask.result?.user != null) {
                createTask.result!!.user.sendEmailVerification().addOnCompleteListener { verificationTask ->
                    onComplete(verificationTask.exception?.toDomainException())
                }
            } else onComplete(createTask.exception?.toDomainException())
        }
    }

    fun logout() {
        firebaseAuth.signOut()
    }
}

class UnverifiedEmailException : Exception("The email has not been verified yet")