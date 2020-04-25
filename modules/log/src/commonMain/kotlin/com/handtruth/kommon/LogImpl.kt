package com.handtruth.kommon

class AppendableLog(
    private val appendable: Appendable, tag: String = getDefaultTag(),
    level: LogLevel = LogLevel.Info
) : TaggedLog(tag, level) {
    override fun write(lvl: LogLevel, message: Any?): Unit = with(appendable) {
        append(tag, " [", lvl.actualName, "]: ", message.toString(), "\n")
    }
}

class PrintLog(tag: String = getDefaultTag(), level: LogLevel = LogLevel.Info) : TaggedLog(tag, level) {
    override fun write(lvl: LogLevel, message: Any?) {
        println("$tag [$lvl]: $message")
    }
}

object VoidLog : Log(LogLevel.None) {
    override fun write(lvl: LogLevel, message: Any?) = Unit
}
