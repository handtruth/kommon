# Module kommon-concurrent

This module consists of several coroutine synchronization primitives.

Usage
-----------------------------------------------------

### Declare dependency

Firstly, you need to add the dependency to your project in Gradle.

#### Gradle

```kotlin
repositories {
    maven("https://mvn.handtruth.com")
}

dependencies {
    implementation("com.handtruth.kommon:kommon-concurrent:$kommonVersion")
}
```

### Condition

`Condition` is a synchronization primitive notifies a bunch of coroutines
about certain condition with optional result.

Each coroutine can subscribe for notification with `Condition::wait` function.
You can use ether `Condition::wakeOne` or `Condition::wakeAll` to notify
subscribed coroutines. There is also `Condition::onWake` property for `select`
clause.

### ReadWriteMutex

A solution for https://github.com/Kotlin/kotlinx.coroutines/issues/94.
This primitive is not quite good because it limits a number of readers by finite
number. It is based on `Semaphore` and `Mutex`.

It's possible to create a different solution with dynamic number of readers, but
I'm not that smart... Maybe one day...

### RecursiveMutex

Recursive mutex or reentrant lock that sticks to the job in the coroutine
context. Very limited but working solution that is useful in many cases.
