package android.app

import kotlin.jvm.JvmStatic

object ActivityThread {
    @JvmStatic
    fun currentApplication() = androidx.test.core.app.ApplicationProvider.getApplicationContext<Application>()
}
