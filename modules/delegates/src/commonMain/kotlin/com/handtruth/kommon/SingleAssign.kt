package com.handtruth.kommon

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class AlreadyInitializedException(name: String) : RuntimeException(
    "property $name already initialized"
)

private class SingleAssignProperty<T : Any> : ReadWriteProperty<Any?, T> {
    private lateinit var value: T

    override fun getValue(thisRef: Any?, property: KProperty<*>): T = value

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        if (this::value.isInitialized) {
            throw AlreadyInitializedException(property.name)
        }
        this.value = value
    }
}

fun <T : Any> singleAssign(): ReadWriteProperty<Any?, T> = SingleAssignProperty()
