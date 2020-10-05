package com.handtruth.kommon

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

fun getLog(context: CoroutineContext): Log = context[Log] ?: error(
    "log is not defined in the coroutine context"
)

suspend inline fun getLog(): Log = getLog(coroutineContext)
