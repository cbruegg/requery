package io.requery.async

import io.requery.RollbackException
import io.requery.TransactionIsolation
import io.requery.kotlin.*
import io.requery.kotlin.Deletion
import io.requery.kotlin.InsertInto
import io.requery.kotlin.Insertion
import io.requery.kotlin.Selection
import io.requery.kotlin.Update
import io.requery.meta.Attribute
import io.requery.query.*
import io.requery.sql.Configuration
import io.requery.sql.KotlinEntityDataStore
import io.requery.util.function.Function
import kotlinx.coroutines.experimental.asCoroutineDispatcher
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.sync.Mutex
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.reflect.KClass

fun <T : Any> KotlinEntityDataStore(configuration: Configuration) =
        KotlinNonBlockingEntityStore<T>(KotlinEntityDataStore(configuration))

class KotlinNonBlockingEntityStore<T : Any>(private val store: KotlinEntityDataStore<T>) : EntityStore<T, Any> {

    private val executor = Executors.newSingleThreadExecutor()
    private val transactionContext by lazy { KotlinNonBlockingEntityStore(store) }
    private val mutex = Mutex()

    override fun close() = store.close()

    override infix fun <E : T> select(type: KClass<E>): Selection<NonBlockingCompletableResult<E>> = result(store.select(type))
    override fun <E : T> select(type: KClass<E>, vararg attributes: QueryableAttribute<E, *>): Selection<NonBlockingCompletableResult<E>> = result(store.select(type, *attributes))
    override fun select(vararg expressions: Expression<*>): Selection<NonBlockingCompletableResult<Tuple>> = result(store.select(*expressions))

    override fun <E : T> insert(type: KClass<E>): Insertion<NonBlockingCompletableResult<Tuple>> = result(store.insert(type))
    override fun <E : T> insert(type: KClass<E>, vararg attributes: QueryableAttribute<E, *>): InsertInto<out Result<Tuple>> = result(store.insert(type, *attributes))
    override fun update(): Update<NonBlockingCompletableScalar<Int>> = scalar(store.update())
    override fun <E : T> update(type: KClass<E>): Update<NonBlockingCompletableScalar<Int>> = scalar(store.update(type))

    override fun delete(): Deletion<NonBlockingCompletableScalar<Int>> = scalar(store.delete())
    override fun <E : T> delete(type: KClass<E>): Deletion<NonBlockingCompletableScalar<Int>> = scalar(store.delete(type))

    override fun <E : T> count(type: KClass<E>): Selection<NonBlockingCompletableScalar<Int>> = scalar(store.count(type))
    override fun count(vararg attributes: QueryableAttribute<T, *>): Selection<NonBlockingCompletableScalar<Int>> = scalar(store.count(*attributes))

    override fun <E : T> insert(entity: E): SuspendResult<E> = execute { store.insert(entity) }
    override fun <E : T> insert(entities: Iterable<E>): SuspendResult<Iterable<E>> = execute { store.insert(entities) }
    override fun <K : Any, E : T> insert(entity: E, keyClass: KClass<K>): SuspendResult<K> = execute { store.insert(entity, keyClass) }
    override fun <K : Any, E : T> insert(entities: Iterable<E>, keyClass: KClass<K>): SuspendResult<Iterable<K>> = execute { store.insert(entities, keyClass) }

    override fun <E : T> update(entity: E): SuspendResult<E> = execute { store.update(entity) }
    override fun <E : T> update(entities: Iterable<E>): SuspendResult<Iterable<E>> = execute { store.update(entities) }

    override fun <E : T> upsert(entity: E): SuspendResult<E> = execute { store.upsert(entity) }
    override fun <E : T> upsert(entities: Iterable<E>): SuspendResult<Iterable<E>> = execute { store.upsert(entities) }

    override fun <E : T> refresh(entity: E): SuspendResult<E> = execute { store.refresh(entity) }
    override fun <E : T> refresh(entity: E, vararg attributes: Attribute<*, *>): SuspendResult<E> = execute { store.refresh(entity, *attributes) }

    override fun <E : T> refresh(entities: Iterable<E>, vararg attributes: Attribute<*, *>): SuspendResult<Iterable<E>> = execute { store.refresh(entities, *attributes) }
    override fun <E : T> refreshAll(entity: E): SuspendResult<E> = execute { store.refreshAll(entity) }

    override fun <E : T> delete(entity: E): SuspendResult<*> = execute { store.delete(entity) }
    override fun <E : T> delete(entities: Iterable<E>): SuspendResult<*> = execute { store.delete(entities) }

    override fun raw(query: String, vararg parameters: Any): Result<Tuple> = store.raw(query, parameters)
    override fun <E : T> raw(type: KClass<E>, query: String, vararg parameters: Any): Result<E> = store.raw(type, query, parameters)

    override fun <E : T, K> findByKey(type: KClass<E>, key: K): SuspendResult<E?> = execute { store.findByKey(type, key) }

    override fun toBlocking(): BlockingEntityStore<T> = store

    suspend fun <V> withTransaction(body: suspend KotlinNonBlockingEntityStore<T>.() -> V): SuspendResult<V> = execute {
        mutex.use {
            val transaction = store.data.transaction().begin()
            try {
                val result = transactionContext.body()
                transaction.commit()
                return@use result
            } catch (e: Exception) {
                transaction.rollback()
                throw RollbackException(e)
            }
        }
    }

    suspend fun <V> withTransaction(isolation: TransactionIsolation,
                                    body: suspend KotlinNonBlockingEntityStore<T>.() -> V): SuspendResult<V> = execute {
        mutex.use {
            val transaction = store.data.transaction().begin(isolation)
            try {
                val result = transactionContext.body()
                transaction.commit()
                return@use result
            } catch (e: Exception) {
                transaction.rollback()
                throw RollbackException(e)
            }
        }
    }

    fun <V> execute(block: suspend KotlinNonBlockingEntityStore<T>.() -> V): SuspendResult<V> =
            SuspendResult(executor, mutex) { block() }

    @Suppress("UNCHECKED_CAST")
    private fun <E> result(query: Return<out Result<E>>): QueryDelegate<NonBlockingCompletableResult<E>> {
        val element = query as QueryDelegate<Result<E>>
        return element.extend(Function { result -> NonBlockingCompletableResult(result, executor, mutex) })
    }

    @Suppress("UNCHECKED_CAST")
    private fun <E> scalar(query: Return<out Scalar<E>>): QueryDelegate<NonBlockingCompletableScalar<E>> {
        val element = query as QueryDelegate<Scalar<E>>
        return element.extend(Function { result -> NonBlockingCompletableScalar(result, executor, mutex) })
    }
}

class SuspendResult<out V> internal constructor(
        executor: Executor,
        private val mutex: Mutex,
        private val block: suspend () -> V) {

    private val dispatcher = executor.asCoroutineDispatcher()

    suspend fun await(): V = async(dispatcher) { mutex.use(block) }.await()
}

internal suspend fun <T> Mutex.use(f: suspend () -> T): T {
    lock()
    try {
        return f()
    } finally {
        unlock()
    }
}