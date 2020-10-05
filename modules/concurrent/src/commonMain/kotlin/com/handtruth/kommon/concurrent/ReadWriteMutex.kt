package com.handtruth.kommon.concurrent

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore

interface ReadWriteMutex {
    val read: Mutex
    val write: Mutex

    companion object {
        const val SIMPLE = 0
    }
}

@Suppress("FunctionName")
fun ReadWriteMutex(count: Int): ReadWriteMutex = when (count) {
    ReadWriteMutex.SIMPLE -> SimpleReadWriteMutex()
    else -> SemaphoreReadWriteMutexImpl(count)
}

private class SemaphoreReadWriteMutexImpl(val count: Int) : ReadWriteMutex {
    val semaphore = Semaphore(count)
    val mutex = Mutex()

    override val read = ReadMutex()
    override val write = WriteMutex()

    inner class ReadMutex : Mutex {
        override val isLocked get() = semaphore.availablePermits == 0
        override val onLock get() = throw UnsupportedOperationException()

        override fun holdsLock(owner: Any) = throw UnsupportedOperationException()

        override suspend fun lock(owner: Any?) {
            semaphore.acquire()
        }

        override fun tryLock(owner: Any?): Boolean = semaphore.tryAcquire()

        override fun unlock(owner: Any?) = semaphore.release()
    }

    inner class WriteMutex : Mutex {
        override val isLocked get() = mutex.isLocked
        override val onLock get() = throw UnsupportedOperationException()

        override fun holdsLock(owner: Any) = throw UnsupportedOperationException()

        override suspend fun lock(owner: Any?) {
            mutex.lock()
            repeat(count) {
                semaphore.acquire()
            }
        }

        override fun tryLock(owner: Any?): Boolean {
            mutex.tryLock() || return false
            repeat(count) {
                if (!semaphore.tryAcquire()) {
                    repeat(it) {
                        semaphore.release()
                    }
                    return false
                }
            }
            return true
        }

        override fun unlock(owner: Any?) {
            mutex.unlock()
            repeat(count) {
                semaphore.release()
            }
        }
    }
}

private class SimpleReadWriteMutex : ReadWriteMutex {
    private val shared = Mutex()
    override val read get() = shared
    override val write get() = shared
}
