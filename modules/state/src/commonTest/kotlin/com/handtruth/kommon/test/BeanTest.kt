package com.handtruth.kommon.test

import com.handtruth.kommon.BeanJar
import com.handtruth.kommon.BeanNotFoundException
import com.handtruth.kommon.getBeanOrNull
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class BeanTest {

    data class ObjectWithBeans(val other: String) : BeanJar()

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

}
