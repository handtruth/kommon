Kommon Delegates
========================================================

Some delegates with doubtful usability.

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
    implementation("com.handtruth.kommon:kommon-delegates:$kommonVersion")
}
```

### Single Assign

This delegate should not be used, but if there is no other option you can add it
in your code. This delegate works like `lateinit var` but it can be assigned
only once. It may be useful when you can't initialize all class properties at
construction time.

```kotlin
var property: Int by singleAssign()

property = 23
println(property) // Out: 23
property = 42 // ERROR
```
