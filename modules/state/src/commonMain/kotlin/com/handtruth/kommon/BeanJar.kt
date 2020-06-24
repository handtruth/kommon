package com.handtruth.kommon

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.typeOf

class BeanNotFoundException internal constructor(val type: KType) : RuntimeException("bean of type $type not found")
class NotBeanJarContextException(message: String) : RuntimeException(message)

private fun throwBeanContextException(context: CoroutineContext): Nothing {
    throw NotBeanJarContextException("Coroutine context $context has not bean jar")
}

interface BeanContainer {
    val beanJar: BeanJar

    fun setBean(type: KType, bean: Any) = beanJar.setBean(type, bean)
    fun getBeanOrNull(type: KType): Any? = beanJar.getBeanOrNull(type)
    fun getBean(type: KType): Any = beanJar.getBean(type)
}

inline fun <reified B : Any> BeanContainer.setBean(bean: B) = setBean(typeOf<B>(), bean)
inline fun <reified B : Any> BeanContainer.getBeanOrNull(): B? = getBeanOrNull(typeOf<B>()) as? B
inline fun <reified B : Any> BeanContainer.getBean(): B = getBean(typeOf<B>()) as B

open class BeanJar : BeanContainer, CoroutineContext.Element {
    final override val beanJar get() = this

    private var beans: Map<KType, Any> = emptyMap()

    final override fun setBean(type: KType, bean: Any) {
        beans = beans + (type to bean)
    }

    final override fun getBeanOrNull(type: KType): Any? = beans[type]
    final override fun getBean(type: KType): Any = beans[type] ?: throw BeanNotFoundException(type)

    inline operator fun <reified B : Any> getValue(thisRef: Any?, property: KProperty<*>): B = getBean()
    inline operator fun <reified B : Any> setValue(thisRef: Any?, property: KProperty<*>, value: B) = setBean(value)

    companion object Key : CoroutineContext.Key<BeanJar>

    override val key get() = Key
}

@PublishedApi
internal fun getBeanOrNull(context: CoroutineContext, type: KType): Any? {
    val jar = context[BeanJar] ?: throwBeanContextException(context)
    return jar.getBeanOrNull(type)
}

@PublishedApi
internal fun getBean(context: CoroutineContext, type: KType): Any {
    val jar = context[BeanJar] ?: throwBeanContextException(context)
    return jar.getBean(type)
}

@PublishedApi
internal fun setBean(context: CoroutineContext, type: KType, bean: Any) {
    val jar = context[BeanJar] ?: throwBeanContextException(context)
    jar.setBean(type, bean)
}

suspend inline fun getBeanOrNull(type: KType): Any? = getBeanOrNull(coroutineContext, type)
suspend inline fun getBean(type: KType): Any = getBean(coroutineContext, type)
suspend inline fun setBean(type: KType, bean: Any) = setBean(coroutineContext, type, bean)

suspend inline fun <reified B : Any> getBeanOrNull() = getBeanOrNull(typeOf<B>())
suspend inline fun <reified B : Any> getBean() = getBean(typeOf<B>())
suspend inline fun <reified B : Any> setBean(bean: B) = setBean(typeOf<B>(), bean)
