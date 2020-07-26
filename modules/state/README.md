Kommon State
=======================================================

Currently, this module only contains `BeanJar` class. These class can be used for
extending an object state with associated user data.

Usage
-------------------------------------------------------

### Declare Dependency

#### Gradle

```kotlin
repositories {
    maven("https://mvn.handtruth.com")
}

dependencies {
    implementation("com.handtruth.kommon:kommon-state:$kommonVersion")
}
```

### BeanJar

`BeanJar` can extend object state with any other object by its type. There are
two ways to apply `BeanJar` in your class. The first one is direct extending from
`BeanJar.Simple` or `BeanJar.Sync` and the second one by
implementing `BeanContainer` interface.

```kotlin
class FirstWay : BeanJar.Simple()

class SecondWay : BeanContainer {
    override val beanJar = BeanJar.Simple()
}
```

Then later you can add properties to your object that are associated by type.

```kotlin
class MyType : BeanJar()

MyType.setBean(listOf(3, 5, 8))
MyType.setBean(listOf("one", "four", "two"))

val ints: List<Int> = MyType.getBean()
val strings: List<String>? = MyType.getBeanOrNull()
assert(ints == listOf(3, 5, 8))
assert(strings == listOf("one", "four", "two"))
```

For purpose of extending object's properties, I recommend you to create an
external property.

```kotlin
var MyType.property: List<Int>
    get() = getBean()
    set(value) = setBean(value)
```

The another usage case for `BeanJar` is creating mutable coroutine context.
You can place `BeanJar` in your coroutine context.

```kotlin
withContext(BeanJar.Simple()) {
    launch {
        setBean(23)
    }
    launch {
        yield()
        val bean: Int = getBean()
        assert(23 == bean)
    }
}
```

If you need to share a `BeanJar` across threads you can use `BeanJar.Sync`
instead of `BeanJar.Simple`.
