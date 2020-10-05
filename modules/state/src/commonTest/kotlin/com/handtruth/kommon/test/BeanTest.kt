package com.handtruth.kommon.test

import com.handtruth.kommon.*
import io.ktor.test.dispatcher.testSuspend
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlinx.coroutines.*

class BeanTest {

    data class ObjectWithBeans(val other: String) : BeanJar.Simple()

    @Test
    fun beanJarTest() {
        val obj = ObjectWithBeans("some value")
        assertNull(obj.getBeanOrNull<List<String>>())
        var list: List<String> by obj.beanJar
        assertFailsWith<BeanNotFoundException> {
            println(list)
        }
        list = listOf("1", "2", "D", "3")
        assertEquals(listOf("1", "2", "D", "3"), list)
        var otherList: List<Int> by obj.beanJar
        assertFailsWith<BeanNotFoundException> {
            println(otherList)
        }
        otherList = listOf(1, 2, 12, 3)
        assertEquals(listOf(1, 2, 12, 3), otherList)
        assertEquals(listOf("1", "2", "D", "3"), list)
    }

    // @Test TODO: Something wrong with coroutines-core-js
    fun contextualBeanJarTest() = testSuspend(BeanJar.Sync()) {
        launch {
            setBean(23)
        }
        val actual = async {
            yield()
            getBean<Int>()
        }
        assertEquals(23, actual.await())
        assertEquals(23, removeBean<Int>())
        assertNull(getBeanOrNull<Int>())
    }
}
