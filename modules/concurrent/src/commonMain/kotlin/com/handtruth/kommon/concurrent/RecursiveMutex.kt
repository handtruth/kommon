@file:Suppress("FunctionName")

package com.handtruth.kommon.concurrent

import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.Job
import kotlinx.coroutines.selects.SelectClause1
import kotlinx.coroutines.sync.Mutex
import kotlin.coroutines.coroutineContext

fun RecursiveMutex(parent: Mutex = Mutex()): RecursiveMutex = JobRecursiveMutexImpl(parent)

interface RecursiveMutex {
    val isLocked: Boolean
    val onLock: SelectClause1<RecursiveMutex>
    suspend fun lock()
    suspend fun tryLock(): Boolean
    fun unlock()
}

suspend inline fun <R> RecursiveMutex.withLock(block: () -> R): R {
    lock()
    try {
        return block()
    } finally {
        unlock()
    }
}

internal class JobRecursiveMutexImpl(private val parent: Mutex) : RecursiveMutex {
    private val job: AtomicRef<Job?> = atomic(null)
    private val count = atomic(0)

    fun getCount() = count.value

    override val isLocked get() = count.value > 0

    override val onLock get() = throw NotImplementedError("later")

    suspend inline fun checkContext(): Job {
        return checkNotNull(coroutineContext[Job]) {
            "Not found Job in current coroutine context, this is not supported"
        }
    }

    override suspend fun lock() {
        val currentJob = checkContext()
        if (job.value === currentJob) {
            count.incrementAndGet()
            return
        } else {
            parent.lock()
            job.value = currentJob
            count.incrementAndGet()
        }
    }

    override suspend fun tryLock(): Boolean {
        val currentJob = checkContext()
        return if (job.value === currentJob) {
            count.incrementAndGet()
            true
        } else {
            val locked = parent.tryLock()
            if (locked) {
                job.value = currentJob
                count.incrementAndGet()
            }
            locked
        }
    }

    override fun unlock() {
        val n = count.decrementAndGet()
        check(n >= 0)
        if (n == 0) {
            job.value = null
            parent.unlock()
        }
    }
}
