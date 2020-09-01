package com.handtruth.kommon.concurrent

import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface Later<out T> {
    val isInitialized: Boolean

    suspend fun get(): T
    val value: T?
}

private val UNINITIALIZED = Any()

@PublishedApi
internal abstract class SafeLater<out T> : Later<T> {
    private val data: AtomicRef<Any?> = atomic(UNINITIALIZED)

    @Suppress("UNCHECKED_CAST")
    private inline val actual
        get() = data.value as T
    private val mutex = Mutex()

    abstract suspend fun callable(): T

    final override val isInitialized get() = data.value !== UNINITIALIZED

    final override suspend fun get(): T {
        return if (data.value === UNINITIALIZED) mutex.withLock {
            if (data.value === UNINITIALIZED) {
                val result = callable()
                data.value = result
                return result
            } else {
                actual
            }
        } else {
            actual
        }
    }

    @Suppress("UNCHECKED_CAST")
    final override val value: T? get() = if (isInitialized) data.value as T else null

    final override fun toString(): String = if (isInitialized) actual.toString() else "Later value not initialized yet."
}

inline fun <T> later(crossinline block: suspend () -> T): Later<T> {
    return object : SafeLater<T>() {
        override suspend fun callable(): T = block()
    }
}

internal class InitializedLater<T>(override val value: T): Later<T> {
    override val isInitialized get() = true
    override suspend fun get() = value
    override fun toString(): String = value.toString()
}

fun <T> laterOf(value: T): Later<T> = InitializedLater(value)
