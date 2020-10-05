package com.handtruth.kommon

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.locks.SynchronizedObject

class BeanNotFoundException internal constructor(val type: KType) : RuntimeException(
    "bean of type $type not found"
)

class NotBeanJarContextException(message: String) : RuntimeException(message)

private fun getJar(context: CoroutineContext): BeanJar {
    return context[BeanJar] ?: throw NotBeanJarContextException(
        "Coroutine context $context has not bean jar"
    )
}

interface BeanContainer {
    val beanJar: BeanJar

    fun setBean(type: KType, bean: Any): Unit = beanJar.setBean(type, bean)
    fun getBeanOrNull(type: KType): Any? = beanJar.getBeanOrNull(type)
    fun getBean(type: KType): Any = beanJar.getBean(type)
    fun removeBean(type: KType): Any? = beanJar.removeBean(type)
}

inline fun <reified B : Any> BeanContainer.setBean(bean: B) = setBean(typeOf<B>(), bean)
inline fun <reified B : Any> BeanContainer.getBeanOrNull(): B? = getBeanOrNull(typeOf<B>()) as? B
inline fun <reified B : Any> BeanContainer.getBean(): B = getBean(typeOf<B>()) as B
inline fun <reified B : Any> BeanContainer.removeBean(): B? = removeBean(typeOf<B>()) as B?

sealed class BeanJar : BeanContainer, CoroutineContext.Element {
    companion object Key : CoroutineContext.Key<BeanJar>

    final override val key get() = Key

    open class Simple : BeanJar(), CoroutineContext.Element {
        final override val beanJar get() = this

        private var beans: MutableMap<KType, Any> = hashMapOf()

        final override fun setBean(type: KType, bean: Any) {
            beans[type] = bean
        }

        final override fun getBeanOrNull(type: KType): Any? = beans[type]
        final override fun getBean(type: KType): Any = beans[type] ?: throw BeanNotFoundException(
            type
        )

        final override fun removeBean(type: KType): Any? = beans.remove(type)
    }

    open class Sync : BeanJar(), CoroutineContext.Element {
        final override val beanJar get() = this

        private val beans: AtomicRef<Map<KType, Any>> = atomic(emptyMap())

        private val lock = SynchronizedObject()

        final override fun setBean(type: KType, bean: Any) {
            kotlinx.atomicfu.locks.synchronized(lock) {
                beans.value = beans.value + (type to bean)
            }
        }

        final override fun getBeanOrNull(type: KType): Any? = beans.value[type]
        final override fun getBean(type: KType): Any = beans.value[type]
            ?: throw BeanNotFoundException(type)

        final override fun removeBean(type: KType): Any? {
            kotlinx.atomicfu.locks.synchronized(lock) {
                val bean = beans.value[type]
                beans.value = beans.value - type
                return bean
            }
        }
    }
}

@PublishedApi
internal fun getBeanOrNull(context: CoroutineContext, type: KType): Any? {
    val jar = getJar(context)
    return jar.getBeanOrNull(type)
}

@PublishedApi
internal fun getBean(context: CoroutineContext, type: KType): Any {
    val jar = getJar(context)
    return jar.getBean(type)
}

@PublishedApi
internal fun setBean(context: CoroutineContext, type: KType, bean: Any) {
    val jar = getJar(context)
    jar.setBean(type, bean)
}

@PublishedApi
internal fun removeBean(context: CoroutineContext, type: KType): Any? {
    val jar = getJar(context)
    return jar.removeBean(type)
}

inline operator fun <reified B : Any> BeanJar.getValue(thisRef: Any?, property: KProperty<*>): B =
    getBean()

inline operator fun <reified B : Any> BeanJar.setValue(
    thisRef: Any?,
    property: KProperty<*>,
    value: B
) = setBean(value)

suspend inline fun getBeanOrNull(type: KType): Any? = getBeanOrNull(coroutineContext, type)
suspend inline fun getBean(type: KType): Any = getBean(coroutineContext, type)
suspend inline fun setBean(type: KType, bean: Any) = setBean(coroutineContext, type, bean)
suspend inline fun removeBean(type: KType) = removeBean(coroutineContext, type)

suspend inline fun <reified B : Any> getBeanOrNull(): B? = getBeanOrNull(typeOf<B>()) as B?
suspend inline fun <reified B : Any> getBean(): B = getBean(typeOf<B>()) as B
suspend inline fun <reified B : Any> setBean(bean: B) = setBean(typeOf<B>(), bean)
suspend inline fun <reified B : Any> removeBean() = removeBean(typeOf<B>()) as B?
