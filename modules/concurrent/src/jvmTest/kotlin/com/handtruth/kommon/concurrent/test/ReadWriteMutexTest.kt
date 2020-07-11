package com.handtruth.kommon.concurrent.test

import com.handtruth.kommon.concurrent.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.withLock
import org.junit.Test
import kotlin.test.assertEquals

class ReadWriteMutexTest {

    fun readWriteMutexTest(): Unit = runBlocking {
        val resource = Resource(Unit)
        val mutex = ReadWriteMutex(10)
        val writeMutex = RecursiveMutex(mutex.write)
        writeMutex as JobRecursiveMutexImpl
        val condition = Condition<Unit>()
        val write = launch(Dispatchers.IO) {
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
        val read = launch(Dispatchers.IO) {
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
        delay(100)
        condition.wakeAll(Unit)
        write.join()
        read.join()
        Unit
    }

    @Test
    fun readWriteMutexTestRepeat() {
        repeat(100) {
            readWriteMutexTest()
        }
    }

}
