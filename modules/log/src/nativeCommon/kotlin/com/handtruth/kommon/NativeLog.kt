package com.handtruth.kommon

internal actual fun getDefaultTag(): String = "App"

internal actual fun Log.writeException(lvl: LogLevel, message: Any?, throwable: Throwable) {
    internalWrite(lvl, "$message: $throwable")
}

actual fun Log.Companion.default(tag: String, lvl: LogLevel): Log = PrintLog(tag, lvl)
