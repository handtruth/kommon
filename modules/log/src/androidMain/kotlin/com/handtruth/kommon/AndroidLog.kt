package com.handtruth.kommon

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log as ALog

@SuppressLint("PrivateApi")
private fun getApplicationUsingReflection(): Application {
    return Class.forName("android.app.ActivityThread")
        .getMethod("currentApplication").invoke(null) as Application
}

actual fun getDefaultTag() = getApplicationUsingReflection().packageName!!

class AndroidLog(tag: String, level: LogLevel) : TaggedLog(tag, level) {
    override fun write(lvl: LogLevel, message: Any?) {
        val str = message.toString()
        when (lvl) {
            LogLevel.None -> {}
            LogLevel.Fatal -> ALog.wtf(tag, str)
            LogLevel.Error -> ALog.e(tag, str)
            LogLevel.Warning -> ALog.w(tag, str)
            LogLevel.Info -> ALog.i(tag, str)
            LogLevel.Verbose -> ALog.v(tag, str)
            LogLevel.Debug -> ALog.d(tag, str)
        }
    }

    override fun exception(lvl: LogLevel, message: Any?, throwable: Throwable) {
        val str = message.toString()
        when (lvl) {
            LogLevel.None -> {}
            LogLevel.Fatal -> ALog.wtf(tag, str, throwable)
            LogLevel.Error -> ALog.e(tag, str, throwable)
            LogLevel.Warning -> ALog.w(tag, str, throwable)
            LogLevel.Info -> ALog.i(tag, str, throwable)
            LogLevel.Verbose -> ALog.v(tag, str, throwable)
            LogLevel.Debug -> ALog.d(tag, str, throwable)
        }
    }
}

actual fun Log.Companion.default(tag: String, lvl: LogLevel): Log = AndroidLog(tag, lvl)
