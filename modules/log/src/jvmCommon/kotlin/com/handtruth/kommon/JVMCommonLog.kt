package com.handtruth.kommon

import org.slf4j.LoggerFactory
import java.io.Writer
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

private fun getColumn(obj: Any, property: KProperty<*>): String {
    return property.getter.call(obj).toString()
}

private fun printValue(builder: StringBuilder, value: String, size: Int) {
    builder.append(value)
    for (i in 0 until size - value.length)
        builder.append(' ')
    builder.append('|')
}

private const val tableNewLine = "\n\t|"

private inline fun <reified T> tableName() = T::class.simpleName ?: "Table"

@PublishedApi
internal fun Log.table(
    lvl: LogLevel, objects: Collection<Any>, props: Array<out KProperty<*>>,
    header: List<String>?, name: String
) {
    if (objects.isEmpty()) {
        internalWrite(lvl, "$name: EMPTY")
        return
    }
    val properties = if (props.isEmpty())
        objects.first()::class.memberProperties.toTypedArray()
    else
        props
    val columns = properties.size
    val rows = objects.size
    // Create table header
    val headers = if (header != null)
        Array(columns) { if (header.size > it) header[it] else properties[it].name }
    else
        Array(columns) { properties[it].name }
    val columnSizes = IntArray(columns) { headers[it].length }
    val iter = objects.iterator()
    // Get all the values
    val values = Array(rows) {
        val obj = iter.next()
        Array(columns) { i ->
            getColumn(
                obj,
                properties[i]
            )
        }
    }
    // Calculate table width
    for (row in values) {
        for ((i, column) in row.withIndex()) {
            if (column.length > columnSizes[i]) {
                columnSizes[i] = column.length
            }
        }
    }
    // Build table
    val builder = StringBuilder(name).append(tableNewLine)
    // Build table header
    for ((i, head) in headers.withIndex())
        printValue(builder, head, columnSizes[i])
    builder.append(tableNewLine)
    for (i in 0 until columns) {
        for (j in 0 until columnSizes[i])
            builder.append('-')
        builder.append('|')
    }
    // Fill table with values
    for (row in values) {
        builder.append(tableNewLine)
        for ((i, column) in row.withIndex()) {
            printValue(
                builder,
                column,
                columnSizes[i]
            )
        }
    }
    internalWrite(lvl, builder.toString())
}

inline fun <reified T : Any> Log.table(
    lvl: LogLevel, objects: Collection<T>, properties: Array<out KProperty1<T, *>>,
    header: List<String>?, name: () -> Any?
) {
    if (lvl <= level)
        table(lvl, objects, properties, header, name().toString())
}

inline fun <reified T : Any> Log.fatal(
    objects: Collection<T>, vararg properties: KProperty1<T, *>,
    header: List<String>? = null,
    message: () -> Any? = { T::class.simpleName ?: "Table" }
): Nothing {
    val str = message().toString()
    if (LogLevel.Fatal <= level)
        table(LogLevel.Fatal, objects, properties, header, str)
    throw FatalException(str)
}

inline fun <reified T : Any> Log.error(
    objects: Collection<T>, vararg properties: KProperty1<T, *>,
    header: List<String>? = null,
    name: () -> Any? = { T::class.simpleName ?: "Table" }
) = table(LogLevel.Error, objects, properties, header, name)

inline fun <reified T : Any> Log.warning(
    objects: Collection<T>, vararg properties: KProperty1<T, *>,
    header: List<String>? = null,
    name: () -> Any? = { T::class.simpleName ?: "Table" }
) = table(LogLevel.Warning, objects, properties, header, name)

inline fun <reified T : Any> Log.info(
    objects: Collection<T>, vararg properties: KProperty1<T, *>,
    header: List<String>? = null,
    name: () -> Any? = { T::class.simpleName ?: "Table" }
) =
    table(LogLevel.Info, objects, properties, header, name)

inline fun <reified T : Any> Log.verbose(
    objects: Collection<T>, vararg properties: KProperty1<T, *>,
    header: List<String>? = null,
    name: () -> Any? = { T::class.simpleName ?: "Table" }
) = table(LogLevel.Verbose, objects, properties, header, name)

inline fun <reified T : Any> Log.debug(
    objects: Collection<T>, vararg properties: KProperty1<T, *>,
    header: List<String>? = null,
    name: () -> Any? = { T::class.simpleName ?: "Table" }
) = table(LogLevel.Debug, objects, properties, header, name)

class SLF4JLog(tag: String = getDefaultTag()) : TaggedLog(tag, LogLevel.None) {
    private val logger = LoggerFactory.getLogger(tag)

    init {
        lvl = when {
            logger.isDebugEnabled -> LogLevel.Debug
            logger.isTraceEnabled -> LogLevel.Verbose
            logger.isInfoEnabled -> LogLevel.Info
            logger.isWarnEnabled -> LogLevel.Warning
            logger.isErrorEnabled -> LogLevel.Error
            else -> LogLevel.Fatal
        }
    }

    override fun write(lvl: LogLevel, message: Any?) {
        val str = message.toString()
        when (lvl) {
            LogLevel.None -> {}
            LogLevel.Fatal -> logger.error(str)
            LogLevel.Error -> logger.error(str)
            LogLevel.Warning -> logger.warn(str)
            LogLevel.Info -> logger.info(str)
            LogLevel.Verbose -> logger.trace(str)
            LogLevel.Debug -> logger.debug(str)
        }
    }

    override fun exception(lvl: LogLevel, message: Any?, throwable: Throwable) {
        val str = message.toString()
        when (lvl) {
            LogLevel.None -> {}
            LogLevel.Fatal -> logger.error(str, throwable)
            LogLevel.Error -> logger.error(str, throwable)
            LogLevel.Warning -> logger.warn(str, throwable)
            LogLevel.Info -> logger.info(str, throwable)
            LogLevel.Verbose -> logger.trace(str, throwable)
            LogLevel.Debug -> logger.debug(str, throwable)
        }
    }
}

internal actual fun Log.writeException(lvl: LogLevel, message: Any?, throwable: Throwable) {
    val stringWriter = StringWriter()
    val printWriter = PrintWriter(stringWriter)
    throwable.printStackTrace(printWriter)
    printWriter.close()
    stringWriter.close()
    internalWrite(lvl, "$message: $stringWriter")
}

class WriterLog(
    private val writer: Writer,
    tag: String = getDefaultTag(),
    level: LogLevel = LogLevel.Info
) : TaggedLog(tag, level) {
    override fun write(lvl: LogLevel, message: Any?) {
        writer.write("$tag [$lvl]: ${message.toString()}\n")
    }
}
