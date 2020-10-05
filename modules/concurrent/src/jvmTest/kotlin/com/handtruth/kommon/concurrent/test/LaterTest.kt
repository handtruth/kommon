package com.handtruth.kommon.concurrent.test

import com.handtruth.kommon.concurrent.later
import io.ktor.test.dispatcher.testSuspend
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LaterTest {

    class Atom {
        private val cnt = atomic(0)

        fun inc() {
            cnt.incrementAndGet()
        }

        fun get() = cnt.value
    }

    @Test
    fun laterTest() {
        val cnt = Atom()

        val prop = later {
            cnt.inc()
            delay(1000)
            23
        }

        testSuspend(Dispatchers.IO) {
            repeat(10000) {
                launch {
                    assertEquals(23, prop.get())
                }
            }
        }

        assertEquals(1, cnt.get())
    }
}
