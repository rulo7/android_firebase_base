package com.racobos.manhattan.repository

import com.racobos.manhattan.datasource.FirebaseAuthDataSource
import com.racobos.manhattan.datasource.FirebaseRealDatabaseDataSource
import com.racobos.manhattan.datasource.FirestoreDataSource
import com.racobos.manhattan.entity.UserEntity
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

interface AuthRepository {
    fun login(): Single<String>
    fun getUser(): Observable<UserEntity>
}

@Singleton
class AuthDataRepository
@Inject
constructor(
    firebaseRealDatabaseDataSource: FirebaseRealDatabaseDataSource,
    firestoreDataSource: FirestoreDataSource,
    firebaseAuthDataSource: FirebaseAuthDataSource
) : AuthRepository {
    override fun login(): Single<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getUser(): Observable<UserEntity> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}