@file:Suppress("FunctionName")

package com.handtruth.kommon.concurrent

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.selects.SelectClause1
import kotlinx.coroutines.selects.SelectInstance
import kotlinx.coroutines.sync.Mutex
import kotlin.coroutines.resume

interface Condition<T> {
    val count: Int
    val isEmpty: Boolean
    val onWake: SelectClause1<T>
    fun wakeOne(item: T)
    fun wakeAll(item: T)
    suspend fun wait(): T
    fun cancel(exception: CancellationException)

    enum class Variants {
        COW, LinkedQueue
    }
}

fun <T> Condition(variant: Condition.Variants = Condition.Variants.COW): Condition<T> {
    return when (variant) {
        Condition.Variants.COW -> COWConditionImpl()
        Condition.Variants.LinkedQueue -> LinkedListConditionImpl()
    }
}

private class COWConditionImpl<T> : Condition<T>, SelectClause1<T> {
    private class Info<T>(val continuation: CancellableContinuation<T>) {
        private val notUsed = atomic(true)
        fun use() = notUsed.getAndSet(false)
        val isNotUsed: Boolean get() = notUsed.value
        inline fun use(block: (CancellableContinuation<T>) -> Unit) {
            if (use()) {
                if (continuation.isActive)
                    return block(continuation)
            }
        }
    }

    override val count get() = waiters.value.size

    override val isEmpty get() = waiters.value.isEmpty()

    private val waiters = atomic(emptyList<Info<T>>())

    private inline fun forEach(block: (CancellableContinuation<T>) -> Unit) {
        val pending = waiters.getAndSet(emptyList())
        pending.forEach { info ->
            info.use {
                block(it)
            }
        }
    }

    override val onWake: SelectClause1<T> get() = this

    override fun wakeOne(item: T) {
        val pending = waiters.value
        pending.forEach { info ->
            info.use {
                it.resume(item)
                return
            }
        }
        error("nothing to wake")
    }

    override fun wakeAll(item: T) {
        forEach { it.resume(item) }
    }

    override fun cancel(exception: CancellationException) {
        forEach { it.cancel(exception) }
    }

    private val lock = Mutex()

    private fun add(continuation: CancellableContinuation<T>) {
        val newWaiters = mutableListOf(Info(continuation))
        waiters.value.filterTo(newWaiters) { info ->
            info.isNotUsed && info.continuation.isActive
        }
        waiters.value = newWaiters
    }

    override suspend fun wait(): T {
        lock.lock()
        return suspendCancellableCoroutine { continuation ->
            add(continuation)
            lock.unlock()
        }
    }

    @InternalCoroutinesApi
    override fun <R> registerSelectClause1(select: SelectInstance<R>, block: suspend (T) -> R) {
        val task = CoroutineScope(select.completion.context).async { wait() }
        task.onAwait.registerSelectClause1(select, block)
        select.disposeOnSelect(DisposableHandle { if (task.isActive) task.cancel() })
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
private class LinkedListConditionImpl<T> : Condition<T>, SelectClause1<T> {

    private val channel = Channel<CancellableContinuation<T>>(Channel.UNLIMITED)

    override val count get() = throw UnsupportedOperationException()
    override val isEmpty get() = channel.isEmpty
    override val onWake get() = this

    override fun wakeOne(item: T) {
        var continuation: CancellableContinuation<T>
        do {
            continuation = channel.poll() ?: error("nothing to wake")
        } while (!continuation.isActive)
        continuation.resume(item)
    }

    private inline fun onEach(block: (CancellableContinuation<T>) -> Unit) {
        do {
            val continuation = channel.poll()
            continuation?.let {
                if (it.isActive)
                    block(it)
            }
        } while (continuation != null)
    }

    override fun wakeAll(item: T) {
        onEach { it.resume(item) }
    }

    override suspend fun wait(): T = suspendCancellableCoroutine {
        check(channel.offer(it))
    }

    override fun cancel(exception: CancellationException) {
        onEach { it.cancel(exception) }
    }

    @InternalCoroutinesApi
    override fun <R> registerSelectClause1(select: SelectInstance<R>, block: suspend (T) -> R) {
        val task = CoroutineScope(select.completion.context).async { wait() }
        task.onAwait.registerSelectClause1(select, block)
        select.disposeOnSelect(DisposableHandle { if (task.isActive) task.cancel() })
    }
}
