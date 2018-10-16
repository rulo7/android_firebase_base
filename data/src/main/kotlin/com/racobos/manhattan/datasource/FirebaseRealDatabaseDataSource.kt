package com.racobos.manhattan.datasource

import com.google.firebase.database.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseRealDatabaseDataSource
@Inject
constructor() {
    private val firebaseDatabase = FirebaseDatabase.getInstance()

    fun <T : Entity> getByPath(
        entity: Class<T>,
        path: String,
        onData: (T) -> Unit,
        onError: (Exception) -> Unit,
        singleRequest: Boolean = false
    ): Cancellable {
        val ref = firebaseDatabase.getReference(path)

        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                onError(error.toDomainException())
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) onError(NotFoundException())
                try {
                    val data = snapshot.getValue(entity)
                    if (data == null) onError(ParseException())
                    else onData(data.apply {
                        if (snapshot.key != null) id = snapshot.key!!
                    })
                } catch (e: Exception) {
                    onError(ParseException(e.message ?: "Error when parsing data"))
                }
            }

        }
        if (singleRequest) {
            ref.addListenerForSingleValueEvent(valueEventListener)
            return {}
        } else {
            val listenerRegistration = ref.addValueEventListener(valueEventListener)
            return { ref.removeEventListener(listenerRegistration) }
        }

    }

    fun <T : Entity> getListByPath(
        entity: Class<T>,
        path: String,
        onData: (List<T>) -> Unit,
        onError: (Exception) -> Unit,
        orderBy: String? = null,
        startAt: Any? = null,
        endAt: Any? = null,
        limitToFirst: Int? = null,
        limitToLast: Int? = null,
        singleRequest: Boolean = false
    ): Cancellable {

        var ref = if (orderBy == null) firebaseDatabase.getReference(path).orderByKey()
        else firebaseDatabase.getReference(path).orderByChild(orderBy)

        ref = when (startAt) {
            is String? -> ref.startAt(startAt)
            is Double -> ref.startAt(startAt)
            is Boolean -> ref.startAt(startAt)
            else -> ref
        }
        ref = when (endAt) {
            is String? -> ref.endAt(endAt)
            is Double -> ref.endAt(endAt)
            is Boolean -> ref.endAt(endAt)
            else -> ref
        }

        if (limitToFirst != null) ref = ref.limitToFirst(limitToFirst)
        if (limitToLast != null) ref = ref.limitToLast(limitToLast)

        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                onError(error.toDomainException())
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) onError(NotFoundException())
                try {
                    val data = snapshot.children.mapNotNull {
                        it.getValue(entity)?.apply {
                            if (it.key != null) id = it.key!!
                        }
                    }
                    onData(data)
                } catch (e: Exception) {
                    onError(ParseException(e.message ?: "Error when parsing data"))
                }
            }

        }
        if (singleRequest) {
            ref.addListenerForSingleValueEvent(valueEventListener)
            return {}
        } else {
            val listenerRegistration = ref.addValueEventListener(valueEventListener)
            return { ref.removeEventListener(listenerRegistration) }
        }
    }

    fun <T : Entity> writeEntityByPath(
        entity: T,
        path: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit,
        isTransaction: Boolean = false
    ) {
        val ref = firebaseDatabase.getReference(path)
        if (isTransaction)
            ref.runTransaction(object : Transaction.Handler {
                override fun doTransaction(mutableData: MutableData): Transaction.Result {
                    mutableData.value = entity
                    return Transaction.success(mutableData)
                }

                override fun onComplete(error: DatabaseError?, p1: Boolean, snapshot: DataSnapshot?) {
                    val exception = error?.toDomainException()
                    if (exception != null) onError(exception)
                    else onSuccess()
                }
            })
        else ref.setValue(entity).addOnCompleteListener { task ->
            val exception = task.exception
            if (exception != null) onError(exception)
            else onSuccess()
        }
    }

    fun updateEntityByPath(
        fields: Updater.() -> Unit,
        path: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val updater = Updater(fields)
        val ref = firebaseDatabase.getReference(path)
        ref.updateChildren(updater.fields).addOnCompleteListener { task ->
            val exception = task.exception
            if (exception != null) onError(exception)
            else onSuccess()
        }
    }

    class Updater(block: Updater.() -> Unit) {
        init {
            this.apply(block)
        }

        val fields = hashMapOf<String, Any?>()
        fun field(field: String, value: Any?) {
            fields[field] = value
        }
    }

    fun deleteDocumentByPath(path: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        firebaseDatabase.getReference(path).removeValue().addOnCompleteListener { task ->
            val exception = task.exception
            if (exception != null) onError(exception)
            else onSuccess()
        }
    }
}

fun DatabaseError.toDomainException(): AppException =
    when (code) {
        -4, -10, -24 -> NetworkConnectionException(message)
        -3, -7, -11 -> PermissionException(message)
        -2, -6, -8, -9, -25 -> OperationFailedException(message)
        else -> UnknownException("Unknown exception")
    }


