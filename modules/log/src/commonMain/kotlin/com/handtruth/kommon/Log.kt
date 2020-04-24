package com.handtruth.kommon

enum class LogLevel {
    None, Fatal, Error, Warning, Info, Verbose, Debug;
    val actualName = name.toLowerCase()
    override fun toString() = actualName

    companion object {
        private val names = values().associateBy { it.actualName }
        fun getByName(name: String) = names[name.toLowerCase()]
    }
}

class FatalException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}

internal expect fun getDefaultTag(): String

internal expect fun Log.writeException(lvl: LogLevel, message: Any?, throwable: Throwable)

abstract class Log(protected var lvl: LogLevel) {
    val level: LogLevel get() = lvl

    companion object {
        val void: Log get() = VoidLog
    }

    protected abstract fun write(lvl: LogLevel, message: Any?)
    @PublishedApi
    internal fun internalWrite(lvl: LogLevel, message: Any?) = write(lvl, message)

    fun appender(lvl: LogLevel): Appendable = if (lvl <= level) LogAppender(this, lvl) else VoidAppender

    protected open fun exception(lvl: LogLevel, message: Any?, throwable: Throwable) {
        writeException(lvl, message, throwable)
    }
    @PublishedApi
    internal fun internalException(lvl: LogLevel, message: Any?, throwable: Throwable) =
        exception(lvl, message, throwable)

    inline fun write(lvl: LogLevel, message: () -> Any?) {
        if (lvl <= level)
            internalWrite(lvl, message())
    }

    inline fun exception(lvl: LogLevel, message: () -> Any?, throwable: Throwable) {
        if (lvl <= level)
            internalException(lvl, message(), throwable)
    }

    inline fun fatal(message: () -> Any?): Nothing {
        val str = message()
        if (LogLevel.Fatal <= level)
            internalWrite(LogLevel.Fatal, str)
        throw FatalException(str.toString())
    }
    inline fun fatal(throwable: Throwable, message: () -> Any? = { "error occured" }): Nothing {
        val str = message()
        if (LogLevel.Fatal <= level)
            internalException(LogLevel.Fatal, message().toString(), throwable)
        throw FatalException(str.toString(), throwable)
    }
    inline fun error(message: () -> Any?) = write(LogLevel.Error, message)
    inline fun error(throwable: Throwable, message: () -> Any? = { "error occured" }) =
        exception(LogLevel.Error, message, throwable)
    val error: Appendable get() = appender(LogLevel.Error)
    inline fun warning(message: () -> Any?) = write(LogLevel.Warning, message)
    inline fun warning(throwable: Throwable, message: () -> Any? = { "error occured" }) =
        exception(LogLevel.Warning, message, throwable)
    val warning: Appendable get() = appender(LogLevel.Warning)
    inline fun info(message: () -> Any?) = write(LogLevel.Info, message)
    inline fun info(throwable: Throwable, message: () -> Any? = { "error occured" }) =
        exception(LogLevel.Info, message, throwable)
    val info: Appendable get() = appender(LogLevel.Info)
    inline fun verbose(message: () -> Any?) = write(LogLevel.Verbose, message)
    inline fun verbose(throwable: Throwable, message: () -> Any? = { "error occured" }) =
        exception(LogLevel.Verbose, message, throwable)
    val verbose: Appendable get() = appender(LogLevel.Verbose)
    inline fun debug(message: () -> Any?) = write(LogLevel.Debug, message)
    inline fun debug(throwable: Throwable, message: () -> Any? = { "error occured" }) =
        exception(LogLevel.Debug, message, throwable)
    val debug: Appendable get() = appender(LogLevel.Debug)
}

expect fun Log.Companion.default(tag: String = getDefaultTag(), lvl: LogLevel = LogLevel.Info): Log

abstract class TaggedLog(val tag: String, level: LogLevel) : Log(level)

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
