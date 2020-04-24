package com.handtruth.kommon

import org.junit.Test
import kotlin.test.assertEquals

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

}
