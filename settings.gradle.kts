pluginManagement {
    repositories {
        gradlePluginPortal()
        jcenter()
        google()
    }
    val kotlinVersion: String by settings
    val androidGradleVersion: String by settings
    val atomicfuVersion: String by settings
    resolutionStrategy {
        eachPlugin {
            when {
                requested.id.id == "kotlinx-atomicfu" ->
                    useModule("org.jetbrains.kotlinx:atomicfu-gradle-plugin:$atomicfuVersion")
                requested.id.id.startsWith("org.jetbrains.kotlin") -> useVersion(kotlinVersion)
                requested.id.id.startsWith("com.android") ->
                    useModule("com.android.tools.build:gradle:$androidGradleVersion")
            }
        }
    }
    val gitVersionPlugin: String by settings
    val dokkaVersion: String by settings
    val ktlintVersion: String by settings
    plugins {
        id("com.gladed.androidgitversion") version gitVersionPlugin
        id("org.jetbrains.dokka") version dokkaVersion
        id("org.jlleitschuh.gradle.ktlint") version ktlintVersion
    }
}

rootProject.name = "kommon"

val prefix = "${rootProject.name}-"

val kotlinProjects: List<String> = listOf(
)

val androidProjects: List<String> = listOf(
    "log",
    "delegates",
    "state",
    "concurrent"
)

val projectNames = (kotlinProjects + androidProjects).toSet().toList()

fun subproject(name: String) {
    include(":${rootProject.name}-$name")
    project(":${rootProject.name}-$name").projectDir = file("modules/$name")
}

subproject("bom")
projectNames.forEach { subproject(it) }

gradle.allprojects {
    extra["kotlinProjects"] = projectNames
    extra["isAndroid"] = name.startsWith(prefix) && name.removePrefix(prefix) in androidProjects
}
