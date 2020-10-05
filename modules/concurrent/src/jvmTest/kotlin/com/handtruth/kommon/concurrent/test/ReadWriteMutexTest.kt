package com.handtruth.kommon.concurrent.test

import com.handtruth.kommon.concurrent.*
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.withLock

class ReadWriteMutexTest {

    @Test
    fun readWriteMutexTest(): Unit = runBlocking {
        val resource = Resource(Unit)
        val mutex = ReadWriteMutex(10)
        val writeMutex = RecursiveMutex(mutex.write)
        writeMutex as JobRecursiveMutexImpl
        val condition = Condition<Unit>(Condition.Variants.LinkedQueue)
        val write = launch(Dispatchers.IO, start = CoroutineStart.UNDISPATCHED) {
            condition.wait()
            println("write")
            repeat(10000) {
                launch {
                    yield()
                    writeMutex.withLock {
                        resource.write {
                            writeMutex.withLock {
                                yield()
                            }
                        }
                        assertEquals(1, writeMutex.getCount())
                    }
                }
            }
        }
        val read = launch(Dispatchers.IO, start = CoroutineStart.UNDISPATCHED) {
            condition.wait()
            println("read")
            repeat(10000) {
                launch {
                    yield()
                    mutex.read.withLock {
                        resource.read {
                            yield()
                        }
                    }
                }
            }
        }
        condition.wakeAll(Unit)
        write.join()
        read.join()
        Unit
    }

    @Test @Ignore
    fun readWriteMutexTestRepeat() {
        repeat(100) {
            readWriteMutexTest()
        }
    }
}
