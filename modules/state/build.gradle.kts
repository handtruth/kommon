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
    commonMainImplementation("org.jetbrains.kotlinx:atomicfu:$atomicfuVersion")
    commonTestImplementation("io.ktor:ktor-test-dispatcher")
}
