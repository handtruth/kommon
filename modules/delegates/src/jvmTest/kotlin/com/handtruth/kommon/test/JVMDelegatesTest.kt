package com.handtruth.kommon.test

import com.handtruth.kommon.delegate
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class JVMDelegatesTest {

    @Test
    fun jvmPropertiesTest() {
        val props = Properties()
        var name by props.delegate("Ktlo")
        assertEquals(null, props.getProperty("name"))
        assertEquals("Ktlo", name)
        name = "Word"
        assertEquals("Word", name)
        assertEquals("Word", props.getProperty("name"))

        var integer by props.delegate(23)
        assertEquals(23, integer)
        integer = 13
        assertEquals(13, integer)

        var character by props.delegate('8')
        assertEquals('8', character)
        character = 'r'
        assertEquals('r', character)

        var boolean by props.delegate(true)
        assertTrue(boolean)
        boolean = false
        assertFalse(boolean)

        var byte by props.delegate(23.toByte())
        assertEquals(23, byte)
        byte = 13
        assertEquals(13, byte)

        var short by props.delegate(23.toShort())
        assertEquals(23, short)
        short = 13
        assertEquals(13, short)

        var long by props.delegate(23L)
        assertEquals(23L, long)
        long = 13L
        assertEquals(13L, long)

        var float by props.delegate(12.5f)
        assertEquals(12.5f, float)
        float = 13.125f
        assertEquals(13.125f, float)

        var double by props.delegate(12.5)
        assertEquals(12.5, double)
        double = 13.125
        assertEquals(13.125, double)
    }
}
