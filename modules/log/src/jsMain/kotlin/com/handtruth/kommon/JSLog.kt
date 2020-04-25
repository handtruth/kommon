package com.handtruth.kommon

internal actual fun getDefaultTag() = "App"

internal actual fun Log.writeException(lvl: LogLevel, message: Any?, throwable: Throwable) {
    internalWrite(lvl, "$message: $throwable")
}

class JSConsoleLog(tag: String, level: LogLevel) : TaggedLog(tag, level) {
    override fun write(lvl: LogLevel, message: Any?) {
        when (lvl) {
            LogLevel.None -> {}
            LogLevel.Fatal -> console.error(tag, level, message)
            LogLevel.Error -> console.error(tag, level, message)
            LogLevel.Warning -> console.warn(tag, level, message)
            LogLevel.Info -> console.info(tag, level, message)
            LogLevel.Verbose -> console.log(tag, level, message)
            LogLevel.Debug -> console.log(tag, level, message)
        }
    }
}

actual fun Log.Companion.default(tag: String, lvl: LogLevel): Log = JSConsoleLog(tag, lvl)

actual fun Mark.Companion.here(offset: Int): Mark {
    return Mark("unknown", 0)
}
