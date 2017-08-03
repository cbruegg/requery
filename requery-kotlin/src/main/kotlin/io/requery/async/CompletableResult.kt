package io.requery.async

import io.requery.query.Result
import io.requery.query.ResultDelegate
import io.requery.query.element.QueryElement
import io.requery.query.element.QueryWrapper
import kotlinx.coroutines.experimental.sync.Mutex
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.function.Supplier
import kotlin.coroutines.experimental.suspendCoroutine

open class CompletableResult<E>(delegate: Result<E>, val executor: Executor) : ResultDelegate<E>(delegate), QueryWrapper<E> {

    fun toCompletableFuture(): CompletableFuture<out Result<E>> = CompletableFuture.supplyAsync(Supplier { this }, executor)

    inline fun <V> toCompletableFuture(crossinline block: CompletableResult<E>.() -> V): CompletableFuture<V> {
        return CompletableFuture.supplyAsync(Supplier { block() }, executor)
    }

    override fun unwrapQuery(): QueryElement<E> {
        return (delegate as QueryWrapper<E>).unwrapQuery()
    }

}

class NonBlockingCompletableResult<E>(delegate: Result<E>, executor: Executor, private val mutex: Mutex) : CompletableResult<E>(delegate, executor) {
    suspend fun <V> await(block: NonBlockingCompletableResult<E>.() -> V): V = mutex.use {
        suspendCoroutine<V> { cont ->
            executor.execute {
                try {
                    cont.resume(block())
                } catch (e: Exception) {
                    cont.resumeWithException(e)
                }
            }
        }
    }
}