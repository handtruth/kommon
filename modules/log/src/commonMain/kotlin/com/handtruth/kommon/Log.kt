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

class FatalError : Error {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}

internal expect fun getDefaultTag(): String

internal expect fun Log.writeException(lvl: LogLevel, message: Any?, throwable: Throwable)

object LogContext {
    inline val String.here get() = "$this at ${Mark.here()}"
}

abstract class Log(protected var lvl: LogLevel) {
    val level: LogLevel get() = lvl

    companion object {
        val void: Log get() = VoidLog
        val defaultTag get() = getDefaultTag()
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

    inline fun write(lvl: LogLevel, message: LogContext.() -> Any?) {
        if (lvl <= level)
            internalWrite(lvl, LogContext.message())
    }

    inline fun exception(lvl: LogLevel, message: LogContext.() -> Any?, throwable: Throwable) {
        if (lvl <= level)
            internalException(lvl, LogContext.message(), throwable)
    }

    inline fun fatal(message: () -> Any?): Nothing {
        val str = message()
        if (LogLevel.Fatal <= level)
            internalWrite(LogLevel.Fatal, str)
        throw FatalError(str.toString())
    }
    inline fun fatal(throwable: Throwable, message: () -> Any? = { "error occured" }): Nothing {
        val str = message()
        if (LogLevel.Fatal <= level)
            internalException(LogLevel.Fatal, message().toString(), throwable)
        throw FatalError(str.toString(), throwable)
    }
    inline fun error(message: LogContext.() -> Any?) = write(LogLevel.Error, message)
    inline fun error(throwable: Throwable, message: LogContext.() -> Any? = { "error occured" }) =
        exception(LogLevel.Error, message, throwable)
    val error: Appendable get() = appender(LogLevel.Error)
    inline fun warning(message: LogContext.() -> Any?) = write(LogLevel.Warning, message)
    inline fun warning(throwable: Throwable, message: LogContext.() -> Any? = { "error occured" }) =
        exception(LogLevel.Warning, message, throwable)
    val warning: Appendable get() = appender(LogLevel.Warning)
    inline fun info(message: LogContext.() -> Any?) = write(LogLevel.Info, message)
    inline fun info(throwable: Throwable, message: LogContext.() -> Any? = { "error occured" }) =
        exception(LogLevel.Info, message, throwable)
    val info: Appendable get() = appender(LogLevel.Info)
    inline fun verbose(message: LogContext.() -> Any?) = write(LogLevel.Verbose, message)
    inline fun verbose(throwable: Throwable, message: LogContext.() -> Any? = { "error occured" }) =
        exception(LogLevel.Verbose, message, throwable)
    val verbose: Appendable get() = appender(LogLevel.Verbose)
    inline fun debug(message: LogContext.() -> Any?) = write(LogLevel.Debug, message)
    inline fun debug(throwable: Throwable, message: LogContext.() -> Any? = { "error occured" }) =
        exception(LogLevel.Debug, message, throwable)
    val debug: Appendable get() = appender(LogLevel.Debug)
}

expect fun Log.Companion.default(tag: String = getDefaultTag(), lvl: LogLevel = LogLevel.Info): Log

abstract class TaggedLog(val tag: String, level: LogLevel) : Log(level)
