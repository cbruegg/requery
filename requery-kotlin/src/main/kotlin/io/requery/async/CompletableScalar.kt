package io.requery.async

import io.requery.query.Scalar
import io.requery.query.ScalarDelegate
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import kotlin.coroutines.experimental.suspendCoroutine

open class CompletableScalar<E>(delegate: Scalar<E>, protected val executor: Executor) : ScalarDelegate<E>(delegate) {
    override fun toCompletableFuture(): CompletableFuture<E> = toCompletableFuture(executor)
}

class NonBlockingCompletableScalar<E>(delegate: Scalar<E>, executor: Executor) : CompletableScalar<E>(delegate, executor) {
    suspend fun await(): E = suspendCoroutine { cont ->
        executor.execute {
            try {
                cont.resume(call())
            } catch (e: Exception) {
                cont.resumeWithException(e)
            }
        }
    }
}