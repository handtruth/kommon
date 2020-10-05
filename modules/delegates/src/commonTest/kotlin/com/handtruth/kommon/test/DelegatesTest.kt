package com.handtruth.kommon.test

import com.handtruth.kommon.AlreadyInitializedException
import com.handtruth.kommon.singleAssign
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith

class DelegatesTest {

    @Test
    fun singleAssignTest() {
        var name: String by singleAssign()

        assertFails {
            val value = name
            println(value)
        }
        name = "Ktlo"
        assertFailsWith<AlreadyInitializedException> {
            name = "Ninja"
        }
        assertEquals("Ktlo", name)
    }
}
