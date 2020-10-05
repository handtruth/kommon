# Module kommon-log

Log module introduces lightweight logging system for Kotlin.

Usage
-----------------------------------------------------

### Declare dependency

Firstly, you need to add the dependency to your project in Gradle.

```kotlin
repositories {
    maven("https://mvn.handtruth.com")
}

dependencies {
    implementation("com.handtruth.kommon:kommon-log:$kommonVersion")
}
```

### Logger Tree

Then in your code you need to think about parts of your projects that need
logging. You can make a general logger for the whole project or maybe you
find out that it's better to create a different logger for each class in your
project. To make it easier this library has LogFactory class.

```kotlin
val mainLogFactory = LogFactory("App Name", LogLevel.Verbose)
val log = mainLogFactory.log()

class BackgroundWorkers {
    val logFactory = mainLogFactory.factory("Workers")
    val log = logFactory.log()
    inner class WorkerUnit(workerID: Int) {
        val log = logFactory.log("#$workerID")
        init {
            log.verbose { "Worker #$workerID initialized" }
        }
    }
    init {
        log.debug { "Initializing background workers" }
        repeat(2) {
            WorkerUnit(it)
        }
    }
}

fun main() {
    log.info { "Starting" }
    BackgrounWorker()
}
```

Code above will the following messages to *default* log.

```log
App Name [info]: Starting
App Name\Workers\#0 [verbose]: Worker #0 initialized
App Name\Workers\#1 [verbose]: Worker #1 initialized
```

### Default Logger

`LogFactory` by default creates logger with the best variant for a current
platform. In JVM it's `SLF4JLog` class, in native logger will be object of
`PrintLog` class and JavaScript will use `JSConsoleLog` class. Android will
use `AndroidLog` class which wraps `android.util.Log`. You can obtain default
Log implementation from `Log.default` function.

If you don't like default Log implementation you can specify your `Log`
implementation like so.

```kotlin
val factory = LogFactory { tag, lvl -> PrintLog(tag, lvl) }
```

As you may notice all the parameters of `LogFactory` class has default
arguments. For `tag` parameter it is `Log.defaultTag` value which has own
implementation for different platforms. By the way it's not recommended using
it, because it's value may change in the future. `level` parameter has
`LogLevel.Info` value as the default one on every platform.

### Logger Functionality

#### LogLevel

Each log message is an object of type Any? that will be evaluated only if
`LogLevel` of the message is allowed in an instance of `Log` class.

Log levels have priorities in order:
- `LogLevel.None`
- `LogLevel.Fatal`
- `LogLevel.Error`
- `LogLevel.Warning`
- `LogLevel.Info`
- `LogLevel.Verbose`
- `LogLevel.Debug`

If you specify `LogLevel.Warning` in `Log` instance all the messages bellow
`LogLevel.Warning` won't be shown. Thus, you can restrict log verbosity or
disable it at all with `LogLevel.None`.

`LogLevel.Fatal` is a special one. After printing log message it will raise
an `FatalError`. This was made for such scenarios when you need to stop
application after fatal errors. As you may guess, `FatalError` class inherits
`Error` class. So, you can still catch regular exceptions and pass
`FatalError` to the top of application call stack.

#### Exceptions

Except for regular messages you can also send a message with `Throwable`
object. This is useful when you want to print stack trace of the caught
exception. Example:

```kotlin
try {
    ...
} catch (e: Exception) {
    log.error(e) { "Something went wrong" }
}
```

#### Table

On JVM and Android target you can also print a collection of objects in table
format.

```kotlin
data class MyData(val name: String, val id: Int)

log.info(listOf(MyData("First", 0), MyData("Second", 23)))

/* Output:
TAG [info]: MyData
    |name  |id|
    |------|--|
    |First |0 |
    |Second|23|
*/
```

You can also specify which properties should be included in table and change
headers names.

```kotlin
val list = listOf(MyData("First", 0), MyData("Second", 23))
log.info(list, MyData::name, header = listOf("Key")) { "Keys" }

/* Output:
TAG [info]: Keys
    |Key   |
    |------|
    |First |
    |Second|
*/
```

#### Current Location in Code

Sometimes you may wonder to print file name and line number of the place
where you use logger. You can do this using `String.here` extension function.

```kotlin
log.info { "I am".here }
/* Output:
TAG [info]: I am at (Example.kt:10)
*/
```

#### Appendable Log Level

You can obtain an `Appendable` instance of your log for specific level.
Remember by doing this you will miss a feature of inline log message
evaluation.

```kotlin
val log = Log.default("TAG", LogLevel.Info)

val info = log.info
info.append("Hello World!\nNot Printed")
val debug = log.debug
debug.append("Message\n")

/* Output:
TAG [info]: Hello World!
*/
```

If appended string not ended with '\n' character than it won't be shown until
'\n' is not appended to the same `Appendable` instance.

If `LogLevel` is not allowed in the instance of `Log` than `Appendable`
object won't do anything.

### Log Class Implementations

There are several implementations of the Log class. They don't implement Log
class directly but implement `TaggedLog` instead which has `tag` property and
inherits `Log` class. (An exception is `VoidLog` that implements `Log`
directly).

#### AppendableLog

This logger appends messages to `Appendable` object (for an instance
`StringBuilder`). This logger is not thread safe. Mainly used in tests.

#### PrintLog

Basically prints messages using `println` function.

#### VoidLog

This logger always has `LogLevel.None` and does not output messages anywhere.

#### JSConsoleLog

Sends evaluated log messages to `console` is JavaScript. This implementation
only exists in JavaScript target.

#### SLF4JLog

Uses `org.slf4j.Logger` inside. Doesn't have (level: LogLevel) parameter in
a constructor, because log level will be deduced from `org.slf4j.Logger`
instance. This implementation only available in Android and JVM targets.

#### WriterLog

Uses `java.util.Writer` class and prints messages to it's instance. This
implementation only available in Android and JVM targets.

#### AndroidLog

Wraps `android.util.Log`. `Log.fatal` method will call
`android.util.Log.wtf`. This implementation available only in Android target.
