Kommon BOM
=============================================================

Bill of Materials module contains a big POM file with versions of each module
in this library and its target implementations. Basically, versions should be
the same and equal the version of *kommon-bom* module itself.

You can use this module to inherit BOM file inside your BOM file, or you can
use it as platform dependency in your application or library.

Usage
------------------------------------------------------------

### Example of platform dependency in Gradle.

```kotlin
repositories {
    maven("https://mvn.handtruth.com")
}

dependencies {
    val kommonPlatform = platform("com.handtruth.kommon:kommon-bom:$platformVersion")
    implementation(kommonPlatform)
    // Other dependencies without versions
    implementation("com.handtruth.kommon:kommon-log")
}
```

### Example of BOM project in Gradle

```kotlin
plugins {
    `java-platform`
}

group = "com.example.platform"
version = "0.1.0"

javaPlatform {
    allowDependencies()
}

dependencies {
    api(platform("com.handtruth.kommon:kommon-bom:$kommonVersion"))
}
```
