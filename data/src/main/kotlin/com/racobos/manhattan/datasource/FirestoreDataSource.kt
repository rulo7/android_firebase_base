package com.racobos.manhattan.datasource

import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import  com.google.firebase.firestore.Query.Direction
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreDataSource
@Inject
constructor() {
    private val firebaseFirestore = FirebaseFirestore.getInstance()

    fun <T : Entity> getDocumentByPath(
        entity: Class<T>,
        path: String,
        onData: (T) -> Unit,
        onError: (Exception) -> Unit
    ): Cancellable {
        val listenerRegistration = firebaseFirestore.document(path).addSnapshotListener { snapshot, exception ->
            if (exception != null) onError(exception)
            else if (snapshot == null || !snapshot.exists()) onError(NotFoundException())
            else {
                val data = snapshot.toObject(entity)
                if (data == null) onError(ParseException())
                else onData(data.apply {
                    id = snapshot.id
                })
            }
        }
        return { listenerRegistration.remove() }
    }

    fun <T : Entity> getCollectionByPath(
        entity: Class<T>,
        path: String,
        onData: (List<T>) -> Unit,
        onError: (Exception) -> Unit
    ): Cancellable {
        val listenerRegistration = firebaseFirestore.collection(path).addSnapshotListener { snapshot, exception ->
            if (exception != null) onError(exception)
            else if (snapshot == null || snapshot.isEmpty) onError(NotFoundException())
            else {
                val data = snapshot.documents.mapNotNull {
                    it.toObject(entity)?.apply {
                        id = it.id
                    }
                }
                if (data.isEmpty()) onError(NotFoundException())
                else onData(data)
            }
        }
        return { listenerRegistration.remove() }
    }

    fun <T : Entity> writeDocumentByPath(
        entity: T,
        path: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit,
        isTransaction: Boolean = false
    ) {
        if (isTransaction)
            firebaseFirestore.runTransaction { transaction ->
                transaction.set(firebaseFirestore.document(path), entity)
            }.addOnCompleteListener(processResponse(onSuccess, onError))
        else firebaseFirestore.document(path).set(entity).addOnCompleteListener(processResponse(onSuccess, onError))
    }

    private fun <K> processResponse(onSuccess: () -> Unit, onError: (Exception) -> Unit) =
        OnCompleteListener<K> { task ->
            val exception = task.exception
            if (exception != null) onError(exception)
            else onSuccess()
        }

    fun updateDocumentByPath(
        fields: Updater.() -> Unit,
        path: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit,
        isTransaction: Boolean = false
    ) {
        val updater = Updater(fields)
        if (isTransaction)
            firebaseFirestore.runTransaction { transaction ->
                transaction.update(firebaseFirestore.document(path), updater.fields)
            }.addOnCompleteListener(processResponse(onSuccess, onError))
        else firebaseFirestore.document(path).update(updater.fields).addOnCompleteListener(
            processResponse(
                onSuccess,
                onError
            )
        )
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
        firebaseFirestore.document(path).delete().addOnCompleteListener { task ->
            val exception = task.exception
            if (exception != null) onError(exception)
            else onSuccess()
        }
    }

    fun <T : Entity> searchDocumentsInPath(
        entity: Class<T>,
        path: String,
        block: SearchQuery.() -> Unit,
        onData: (List<T>) -> Unit,
        onError: (Exception) -> Unit
    ): Cancellable {
        val query = SearchQuery(block)
        var ref: Query = firebaseFirestore.collection(path)


        // Query execution
        if (query.orderByField != null && query.orderByDirection != null)
            ref = ref.orderBy(query.orderByField!!, query.orderByDirection!!)
        query.fields.entries.forEach { entrySet ->
            val key = entrySet.key
            val operation = entrySet.value.first
            val value = entrySet.value.second

            ref = when (operation) {
                SearchQuery.Operations.EQUAL_THAN -> ref.whereEqualTo(key, value)
                else -> {
                    if (value != null) when (operation) {
                        SearchQuery.Operations.GREATER_THAN -> ref.whereGreaterThan(key, value)
                        SearchQuery.Operations.GREATER_OR_EQUAL_THAN -> ref.whereGreaterThanOrEqualTo(key, value)
                        SearchQuery.Operations.LESS_THAN -> ref.whereLessThan(key, value)
                        SearchQuery.Operations.LESS_OR_EQUAL_THAN -> ref.whereLessThanOrEqualTo(key, value)
                        else -> ref
                    }
                    else ref
                }
            }
        }
        //______

        val listenerRegistration = ref.addSnapshotListener { snapshot, exception ->
            if (exception != null) onError(exception)
            else if (snapshot == null || snapshot.isEmpty) onError(NotFoundException())
            else {
                val data = snapshot.documents.mapNotNull {
                    it.toObject(entity)?.apply {
                        id = it.id
                    }
                }
                if (data.isEmpty()) onError(NotFoundException())
                else onData(data)
            }
        }
        return { listenerRegistration.remove() }
    }

    class SearchQuery(block: SearchQuery.() -> Unit) {
        init {
            this.apply(block)
        }

        var orderByField: String? = null
        var orderByDirection: Direction? = null

        val fields = hashMapOf<String, Pair<Operations, Any?>>()

        fun orderBy(field: String, direction: Direction) {
            this.orderByField = field
            this.orderByDirection = direction
        }

        fun operation(name: String, operator: Operations, value: Any?) {
            fields[name] = Pair(operator, value)
        }

        enum class Operations { EQUAL_THAN, GREATER_THAN, GREATER_OR_EQUAL_THAN, LESS_THAN, LESS_OR_EQUAL_THAN, }
    }
}

