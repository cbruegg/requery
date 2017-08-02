package io.requery.async

import io.requery.query.Result
import io.requery.query.ResultDelegate
import io.requery.query.element.QueryElement
import io.requery.query.element.QueryWrapper
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

class NonBlockingCompletableResult<E>(delegate: Result<E>, executor: Executor) : CompletableResult<E>(delegate, executor) {
    inline suspend fun <V> await(crossinline block: CompletableResult<E>.() -> V): V = suspendCoroutine { cont ->
        executor.execute {
            try {
                cont.resume(block())
            } catch (e: Exception) {
                cont.resumeWithException(e)
            }
        }
    }
}