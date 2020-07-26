plugins {
    id("kotlinx-atomicfu")
}

kotlin {
    sourceSets.all {
        with(languageSettings) {
            useExperimentalAnnotation("kotlin.ExperimentalStdlibApi")
        }
    }
}

dependencies {
    val atomicfuVersion: String by project
    commonMainImplementation("org.jetbrains.kotlinx:atomicfu-common:$atomicfuVersion")
    "jvmCommonImplementation"("org.jetbrains.kotlinx:atomicfu:$atomicfuVersion")
    "jsMainImplementation"("org.jetbrains.kotlinx:atomicfu-js:$atomicfuVersion")
    "nativeMainImplementation"("org.jetbrains.kotlinx:atomicfu-native:$atomicfuVersion")
}
