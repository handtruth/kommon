package com.handtruth.kommon

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JVMLogTest {

    @Test
    fun testLogTable() {
        val pairs = mapOf(
            "Jump" to "Cave",
            "Bless" to "God",
            "Say Hello" to "Me"
        )
        val builder = StringBuilder()
        val log: Log = AppendableLog(builder, "TAG", LogLevel.Error)
        log.error(pairs.entries, Map.Entry<String, String>::key, Map.Entry<String, String>::value,
            header = listOf("Verb", "Noun")) { "Header" }
        assertEquals("TAG [error]: Header\n\t|Verb     |Noun|\n\t|---------|----|\n\t" +
                "|Jump     |Cave|\n\t|Bless    |God |\n\t|Say Hello|Me  |\n", builder.toString())
    }

    @Test
    fun testMark() {
        val builder = StringBuilder()
        val log = AppendableLog(builder, "TAG")
        log.info { "Hello World!".here }
        assertTrue(builder.toString().startsWith("TAG [info]: Hello World! at (JVMLogTest.kt:"))
        val mark = Mark.here()
        assertEquals(Mark("JVMLogTest.kt", 30), mark)
    }

}
