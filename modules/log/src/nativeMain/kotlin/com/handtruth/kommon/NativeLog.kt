package com.handtruth.kommon

internal actual fun getDefaultTag(): String = "App"

internal actual fun Log.writeException(lvl: LogLevel, message: Any?, throwable: Throwable) {
    internalWrite(lvl, "$message: $throwable")
}

actual fun Log.Key.default(tag: String, lvl: LogLevel): Log = PrintLog(tag, lvl)

actual fun Mark.Companion.here(offset: Int): Mark {
    return Mark("unknown", 0)
}
