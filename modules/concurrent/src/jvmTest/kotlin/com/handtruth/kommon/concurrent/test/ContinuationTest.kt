package com.handtruth.kommon.concurrent.test

import com.handtruth.kommon.concurrent.Condition
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select
import kotlin.test.Test
import kotlin.test.assertEquals

class ContinuationTest {

    @Test
    fun selectClause(): Unit = runBlocking {
        println("started")
        val condition = Condition<String>()
        val result = async(start = CoroutineStart.UNDISPATCHED) {
            select<String> {
                condition.onWake {
                    println(it)
                    it
                }
                launch { delay(1000) }.onJoin {
                    println("bad")
                    "bad"
                }
            }
        }
        condition.wakeOne("good")
        assertEquals("good", result.await())
    }

}
