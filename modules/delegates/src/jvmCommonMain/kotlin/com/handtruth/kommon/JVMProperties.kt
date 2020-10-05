package com.handtruth.kommon

import java.util.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class PropException(message: String) : RuntimeException(message)

abstract class Prop<in R, T>(protected val props: Properties) : ReadWriteProperty<R, T> {
    protected fun obtain(name: kotlin.String): kotlin.String? = props.getProperty(name)

    protected inline fun <T> validate(name: kotlin.String, block: () -> T?): T {
        return block() ?: throw PropException(
            "Failed to treat property \"$name\" as ${this::class.simpleName} type"
        )
    }

    class String<in R>(
        props: Properties,
        private val default: kotlin.String = ""
    ) : Prop<R, kotlin.String>(props) {
        override fun getValue(thisRef: R, property: KProperty<*>) =
            props.getProperty(property.name) ?: default

        override fun setValue(thisRef: R, property: KProperty<*>, value: kotlin.String) {
            props.put(property.name, value)
        }
    }

    class Int<in R>(
        props: Properties,
        private val default: kotlin.Int = 0
    ) : Prop<R, kotlin.Int>(props) {
        override fun getValue(thisRef: R, property: KProperty<*>) =
            props.getProperty(property.name)?.let {
                validate(property.name) { it.toIntOrNull() }
            } ?: default

        override fun setValue(thisRef: R, property: KProperty<*>, value: kotlin.Int) {
            props.put(property.name, value.toString())
        }
    }

    class Double<in R>(
        props: Properties,
        private val default: kotlin.Double = .0
    ) : Prop<R, kotlin.Double>(props) {
        override fun getValue(thisRef: R, property: KProperty<*>) =
            props.getProperty(property.name)?.let {
                validate(property.name) { it.toDoubleOrNull() }
            } ?: default

        override fun setValue(thisRef: R, property: KProperty<*>, value: kotlin.Double) {
            props.put(property.name, value.toString())
        }
    }

    class Boolean<in R>(
        props: Properties,
        private val default: kotlin.Boolean = false
    ) : Prop<R, kotlin.Boolean>(props) {
        override fun getValue(thisRef: R, property: KProperty<*>) =
            props.getProperty(property.name)?.let {
                validate(property.name) {
                    when (it.toLowerCase()) {
                        "true" -> true
                        "false" -> false
                        else -> null
                    }
                }
            } ?: default

        override fun setValue(thisRef: R, property: KProperty<*>, value: kotlin.Boolean) {
            props.put(property.name, value.toString())
        }
    }

    class Byte<in R>(
        props: Properties,
        private val default: kotlin.Byte = 0
    ) : Prop<R, kotlin.Byte>(props) {
        override fun getValue(thisRef: R, property: KProperty<*>) =
            props.getProperty(property.name)?.let {
                validate(property.name) { it.toByteOrNull() }
            } ?: default

        override fun setValue(thisRef: R, property: KProperty<*>, value: kotlin.Byte) {
            props.put(property.name, value.toString())
        }
    }

    class Short<in R>(
        props: Properties,
        private val default: kotlin.Short = 0
    ) : Prop<R, kotlin.Short>(props) {
        override fun getValue(thisRef: R, property: KProperty<*>) =
            props.getProperty(property.name)?.let {
                validate(property.name) { it.toShortOrNull() }
            } ?: default

        override fun setValue(thisRef: R, property: KProperty<*>, value: kotlin.Short) {
            props.put(property.name, value.toString())
        }
    }

    class Long<in R>(
        props: Properties,
        private val default: kotlin.Long = 0
    ) : Prop<R, kotlin.Long>(props) {
        override fun getValue(thisRef: R, property: KProperty<*>) =
            props.getProperty(property.name)?.let {
                validate(property.name) { it.toLongOrNull() }
            } ?: default

        override fun setValue(thisRef: R, property: KProperty<*>, value: kotlin.Long) {
            props.put(property.name, value.toString())
        }
    }

    class Float<in R>(
        props: Properties,
        private val default: kotlin.Float = .0f
    ) : Prop<R, kotlin.Float>(props) {
        override fun getValue(thisRef: R, property: KProperty<*>) =
            props.getProperty(property.name)?.let {
                validate(property.name) { it.toFloatOrNull() }
            } ?: default

        override fun setValue(thisRef: R, property: KProperty<*>, value: kotlin.Float) {
            props.put(property.name, value.toString())
        }
    }

    class Char<in R>(
        props: Properties,
        private val default: kotlin.Char = ' '
    ) : Prop<R, kotlin.Char>(props) {
        override fun getValue(thisRef: R, property: KProperty<*>) =
            props.getProperty(property.name)?.let {
                validate(property.name) { if (it.length == 1) it[0] else null }
            } ?: default

        override fun setValue(thisRef: R, property: KProperty<*>, value: kotlin.Char) {
            props.put(property.name, value.toString())
        }
    }
}

fun Properties.string(default: String = "") = Prop.String<Any?>(this, default)
fun Properties.char(default: Char = ' ') = Prop.Char<Any?>(this, default)
fun Properties.byte(default: Byte = 0) = Prop.Byte<Any?>(this, default)
fun Properties.boolean(default: Boolean = false) = Prop.Boolean<Any?>(this, default)
fun Properties.short(default: Short = 0) = Prop.Short<Any?>(this, default)
fun Properties.int(default: Int = 0) = Prop.Int<Any?>(this, default)
fun Properties.long(default: Long = 0) = Prop.Long<Any?>(this, default)
fun Properties.float(default: Float = .0f) = Prop.Float<Any?>(this, default)
fun Properties.double(default: Double = .0) = Prop.Double<Any?>(this, default)

inline fun <reified T> Properties.delegate(default: T): Prop<Any?, T> {
    @Suppress("UNCHECKED_CAST")
    return when (default) {
        is String -> string(default)
        is Char -> char(default)
        is Boolean -> boolean(default)
        is Byte -> byte(default)
        is Short -> short(default)
        is Int -> int(default)
        is Long -> long(default)
        is Float -> float(default)
        is Double -> double(default)
        else -> error("Not supported property type")
    } as Prop<Any?, T>
}

operator fun Properties.getValue(thisRef: Any?, property: KProperty<*>): String? = getProperty(
    property.name
)

operator fun Properties.setValue(thisRef: Any?, property: KProperty<*>, value: Any?) = put(
    property.name,
    value
)
