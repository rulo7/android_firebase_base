package com.racobos.manhattan.datasource

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.*
import com.racobos.manhattan.entity.DEFAULT_ID
import com.racobos.manhattan.entity.formatToAppString
import java.util.*

typealias Cancellable = () -> Unit

open class Entity(
    var id: String = DEFAULT_ID,
    var createdAt: String = Calendar.getInstance().formatToAppString(),
    var updatedAt: String = createdAt
)

fun Throwable.toDomainException(): AppException =
    when (this) {
        is AppException -> this
        is FirebaseAuthWeakPasswordException -> BadArgumentsException(
            message
                ?: "Weak password"
        )
        is FirebaseAuthInvalidCredentialsException -> PermissionException(
            message
                ?: "Invalid credentials"
        )
        is FirebaseAuthUserCollisionException,
        is FirebaseAuthRecentLoginRequiredException -> OperationFailedException(
            message
                ?: "User collision"
        )
        is FirebaseNetworkException -> NetworkConnectionException(message ?: "User not found")
        is FirebaseAuthInvalidUserException -> NotFoundException(message ?: "User not found")
        is java.util.concurrent.TimeoutException -> TimeoutException(
            message
                ?: "Timeout was thrown"
        )
        else -> UnknownException(message ?: "Unknown exception ")
    }