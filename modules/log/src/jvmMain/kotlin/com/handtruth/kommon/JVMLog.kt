package com.handtruth.kommon

actual fun getDefaultTag() = Thread.currentThread().name!!

actual fun Log.Key.default(tag: String, lvl: LogLevel): Log = SLF4JLog(tag)
