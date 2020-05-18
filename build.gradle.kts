@file:Suppress("UNUSED_VARIABLE")

import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper
import com.android.build.gradle.LibraryPlugin

plugins {
    id("com.gladed.androidgitversion")
    id("com.android.library") apply false
    kotlin("multiplatform") apply false
}

androidGitVersion {
    prefix = "v"
}

val versionName = androidGitVersion.name()!!
val versionCode = androidGitVersion.code().let { if (it == 0) 1 else it }

allprojects {
    repositories {
        google()
        jcenter()
        mavenCentral()
        maven("https://mvn.handtruth.com")
    }
    group = "com.handtruth.kommon"
    version = versionName
}

val libModules by extra { listOf("log", "delegates", "state") }

fun Project.configureProject() {
    apply<LibraryPlugin>()
    apply<KotlinMultiplatformPluginWrapper>()
    apply<JacocoPlugin>()
    apply<MavenPublishPlugin>()

    val android = extensions["android"] as com.android.build.gradle.LibraryExtension

    with(android) {
        compileSdkVersion(29)
        buildToolsVersion("29.0.3")

        defaultConfig {
            minSdkVersion(16)
            targetSdkVersion(29)
            versionCode = versionCode
            versionName = versionName

            testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
            consumerProguardFiles.add(file("consumer-rules.pro"))
        }

        buildTypes {
            val release by getting {
                isMinifyEnabled = false
                proguardFiles(
                    file(getDefaultProguardFile("proguard-android-optimize.txt")),
                    file("proguard-rules.pro")
                )
            }
        }
    }

    val kotlin = extensions["kotlin"] as org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

    with(kotlin) {
        jvm {
            compilations.all {
                kotlinOptions {
                    jvmTarget = "1.8"
                }
            }
        }
        android {
            publishAllLibraryVariants()
        }
        js {
            browser {
                testTask {
                    useKarma {
                        usePhantomJS()
                    }
                }
            }
            nodejs()
        }
        wasm32()
        /*
        linuxArm32Hfp()
        linuxArm64()
        linuxMips32()
        linuxMipsel32()
        linuxX64()
        mingwX86()
        mingwX64()
        macosX64()
        ios()
        iosArm32()
        iosArm64()
        */

        sourceSets {
            fun kotlinx(name: String) = "org.jetbrains.kotlinx:kotlinx-$name"
            val platformVersion: String by project
            val platform = dependencies.platform("com.handtruth.internal:platform:$platformVersion")
            all {
                dependencies {
                    implementation(platform)
                }
            }
            val commonMain by getting {
                dependencies {
                    implementation(kotlin("stdlib"))
                }
            }
            val commonTest by getting {
                dependencies {
                    implementation(kotlin("test-common"))
                    implementation(kotlin("test-annotations-common"))
                }
            }
            val jvmCommon by creating {
                dependsOn(commonMain)
                dependencies {
                    compileOnly(platform)
                    implementation(kotlin("stdlib-jdk8"))
                }
            }
            val jvmMain by getting {
                dependsOn(jvmCommon)
            }
            val jvmTest by getting {
                dependencies {
                    implementation(kotlin("test-junit"))
                }
            }
            val androidMain by getting {
                dependsOn(jvmCommon)
            }
            val androidTest by getting {
                dependencies {
                    implementation(kotlin("test-junit"))
                    implementation("androidx.test:core")
                    implementation("androidx.test:runner")
                    implementation("androidx.test.ext:junit")
                    implementation("org.robolectric:robolectric")
                }
            }
            val jsMain by getting {
                dependencies {
                    implementation(kotlin("stdlib-js"))
                }
            }
            val jsTest by getting {
                dependencies {
                    implementation(kotlin("test-js"))
                }
            }
            val nativeCommon by creating {
                dependsOn(commonMain)
            }

            sequenceOf(
                "wasm32"//, "linuxArm32Hfp", "linuxArm64", "linuxMips32", "linuxMipsel32", "linuxX64",
                //"mingwX86", "mingwX64", "ios", "iosArm32", "iosArm64"
            ).forEach {
                asMap["${it}Main"]?.apply {
                    dependsOn(nativeCommon)
                }
            }
        }
    }

    val jacoco = extensions["jacoco"] as JacocoPluginExtension

    with(jacoco) {
        toolVersion = "0.8.5"
        reportsDir = file("${buildDir}/jacoco-reports")
    }

    tasks {
        val jvmTest by getting {}
        val testCoverageReport by creating(JacocoReport::class) {
            dependsOn(jvmTest)
            group = "Reporting"
            description = "Generate Jacoco coverage reports."
            val coverageSourceDirs = arrayOf(
                "commonMain/src",
                "jvmMain/src"
            )
            val classFiles = file("${buildDir}/classes/kotlin/jvm/")
                .walkBottomUp()
                .toSet()
            classDirectories.setFrom(classFiles)
            sourceDirectories.setFrom(files(coverageSourceDirs))
            additionalSourceDirs.setFrom(files(coverageSourceDirs))

            executionData.setFrom(files("${buildDir}/jacoco/jvmTest.exec"))
            reports {
                xml.isEnabled = true
                csv.isEnabled = false
                html.isEnabled = true
                html.destination = file("${buildDir}/jacoco-reports/html")
            }
        }
    }
}

libModules.forEach { project(":kommon-$it").configureProject() }
project(":kommon").configureProject()
