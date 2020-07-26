pluginManagement {
    repositories {
        gradlePluginPortal()
        jcenter()
        google()
    }
    val kotlinVersion: String by settings
    val gitAndroidVersion: String by settings
    val androidGradleVersion: String by settings
    val atomicfuVersion: String by settings
    resolutionStrategy {
        eachPlugin {
            when {
                requested.id.id == "kotlinx-atomicfu" ->
                    useModule("org.jetbrains.kotlinx:atomicfu-gradle-plugin:$atomicfuVersion")
                requested.id.id.startsWith("org.jetbrains.kotlin") ->
                    useVersion(kotlinVersion)
                requested.id.id.startsWith("com.android") ->
                    useModule("com.android.tools.build:gradle:$androidGradleVersion")
            }
        }
    }
    plugins {
        id("com.gladed.androidgitversion") version gitAndroidVersion
    }
}

val prefix = "kommon"
rootProject.name = prefix

fun module(name: String) {
    include(":$prefix-$name")
    project(":$prefix-$name").projectDir = file("modules/$name")
}

module("log")
module("bom")
module("delegates")
module("state")
//module("all")
module("concurrent")

//include("test-app")
