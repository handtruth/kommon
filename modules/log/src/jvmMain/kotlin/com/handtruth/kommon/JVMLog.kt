package com.handtruth.kommon

actual fun getDefaultTag() = Thread.currentThread().name!!

actual fun Log.Companion.default(tag: String, lvl: LogLevel): Log = SLF4JLog(tag)
