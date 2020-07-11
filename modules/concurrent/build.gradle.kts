plugins {
    id("kotlinx-atomicfu")
}

dependencies {
    val coroutinesVersion = "1.3.7"
    commonMainApi("org.jetbrains.kotlinx:kotlinx-coroutines-core-common")
    "jvmCommonApi"("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    "jsMainApi"("org.jetbrains.kotlinx:kotlinx-coroutines-core-js")
    "nativeMainApi"("org.jetbrains.kotlinx:kotlinx-coroutines-core-native")
    val atomicfuVersion: String by project
    commonMainImplementation("org.jetbrains.kotlinx:atomicfu-common:$atomicfuVersion")
    "jvmCommonImplementation"("org.jetbrains.kotlinx:atomicfu:$atomicfuVersion")
    "jsMainImplementation"("org.jetbrains.kotlinx:atomicfu-js:$atomicfuVersion")
    "nativeMainImplementation"("org.jetbrains.kotlinx:atomicfu-native:$atomicfuVersion")
}
