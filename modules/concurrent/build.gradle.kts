plugins {
    id("kotlinx-atomicfu")
}

dependencies {
    commonMainApi("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    val atomicfuVersion: String by project
    commonMainImplementation("org.jetbrains.kotlinx:atomicfu:$atomicfuVersion")
    commonTestApi("io.ktor:ktor-test-dispatcher")
}
