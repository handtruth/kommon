@file:Suppress("UNUSED_VARIABLE")

import org.jetbrains.dokka.gradle.DokkaMultiModuleTask
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.dokka.gradle.DokkaPlugin
import org.jlleitschuh.gradle.ktlint.KtlintPlugin
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.LibraryExtension

plugins {
    id("com.gladed.androidgitversion")
    id("org.jetbrains.dokka")
    kotlin("multiplatform") apply false
    id("org.jlleitschuh.gradle.ktlint") apply false
    id("com.android.library") apply false
    jacoco
}

androidGitVersion {
    prefix = "v"
}

val groupString = "com.handtruth.kommon"
val versionString: String = androidGitVersion.name()
val androidVersionCode: Int = androidGitVersion.code().let { if (it == 0) 1 else it }

allprojects {
    group = groupString
    version = versionString

    repositories {
        maven("https://mvn.handtruth.com")
        jcenter()
        google()
    }
}

val kotlinProjects: List<String> by extra

val platformVersion: String by project

val coverageSourceDirsNames = arrayOf(
    "src/commonMain/kotlin/",
    "src/jvmMain/kotlin/"
)

fun KotlinSourceSet.collectSources(): Iterable<File> {
    return kotlin.srcDirs.filter { it.exists() } + dependsOn.flatMap { it.collectSources() }
}

fun KotlinSourceSet.collectSourceFiles(): ConfigurableFileCollection {
    return files(collectSources().map { fileTree(it) })
}

fun Project.kotlinProject() {
    val isAndroid: Boolean by extra
    if (isAndroid) apply<LibraryPlugin>()
    apply<KotlinMultiplatformPluginWrapper>()
    apply<JacocoPlugin>()
    apply<MavenPublishPlugin>()
    apply<DokkaPlugin>()
    apply<KtlintPlugin>()

    configure<PublishingExtension> {
        if (!System.getenv("CI").isNullOrEmpty()) repositories {
            maven {
                url = uri("https://git.handtruth.com/api/v4/projects/${System.getenv("CI_PROJECT_ID")}/packages/maven")
                credentials(HttpHeaderCredentials::class) {
                    name = "Job-Token"
                    value = System.getenv("CI_JOB_TOKEN")!!
                }
                authentication {
                    create<HttpHeaderAuthentication>("header")
                }
            }
        }
    }

    if (isAndroid) configure<LibraryExtension> {
        compileSdkVersion(30)
        buildToolsVersion("29.0.2")

        defaultConfig {
            minSdkVersion(16)
            targetSdkVersion(30)
            versionCode = androidVersionCode
            versionName = versionString

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

    lateinit var jvmMainSourceSet: KotlinSourceSet

    configure<KotlinMultiplatformExtension> {
        jvm {
            compilations.all {
                kotlinOptions {
                    jvmTarget = "1.8"
                }
            }
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
        linuxX64()
        if (isAndroid) android {
            publishAllLibraryVariants()
        }
        sourceSets {
            all {
                with(languageSettings) {
                    useExperimentalAnnotation("kotlinx.coroutines.ExperimentalCoroutinesApi")
                    useExperimentalAnnotation("kotlin.RequiresOptIn")
                }
                dependencies {
                    val handtruthPlatform = dependencies.platform("com.handtruth.internal:platform:$platformVersion")
                    implementation(handtruthPlatform)
                    runtimeOnly(handtruthPlatform)
                    api(handtruthPlatform)
                    compileOnly(handtruthPlatform)
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
            val jvmCommonMain by creating {
                targetFromPreset(presets["jvm"])
                dependsOn(commonMain)
            }
            val jvmCommonTest by creating {
                dependsOn(commonTest)
            }
            val jvmMain by getting {
                dependsOn(jvmCommonMain)
            }
            jvmMainSourceSet = jvmMain
            val jvmTest by getting {
                dependsOn(jvmCommonTest)
                dependencies {
                    implementation(kotlin("test-junit5"))
                    runtimeOnly("org.junit.jupiter:junit-jupiter-engine")
                }
            }
            if (isAndroid) {
                val androidMain by getting {
                    dependsOn(jvmCommonMain)
                }
                val androidTest by getting {
                    dependsOn(jvmCommonTest)
                    dependencies {
                        implementation(kotlin("test-junit"))
                        implementation("androidx.test:core")
                        implementation("androidx.test:runner")
                        implementation("androidx.test.ext:junit")
                        implementation("org.robolectric:robolectric")
                    }
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
            val nativeMain by creating {
                dependsOn(commonMain)
            }
            val nativeTest by creating {
                dependsOn(nativeMain)
            }

            sequenceOf(
                "linuxX64"
            ).forEach {
                val map = asMap
                map["${it}Main"]?.apply {
                    dependsOn(nativeMain)
                }
                map["${it}Test"]?.apply {
                    dependsOn(nativeTest)
                }
            }
        }
    }

    configure<KtlintExtension> {
        version.set("0.39.0")
        verbose.set(true)
        outputToConsole.set(true)
        enableExperimentalRules.set(true)
        outputColorName.set("RED")
        android.set(true)
        disabledRules.add("no-wildcard-imports")

        reporters {
            reporter(ReporterType.PLAIN)
            reporter(ReporterType.CHECKSTYLE)
        }
    }

    configure<JacocoPluginExtension> {
        toolVersion = "0.8.6"
    }

    val jvmTest by tasks.getting(Test::class) {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    val testCoverageReport by tasks.creating(JacocoReport::class) {
        dependsOn(jvmTest)
        group = "Reporting"
        description = "Generate Jacoco coverage reports."

        val classFiles = Callable {
            File(buildDir, "classes/kotlin/jvm/main")
                .walkBottomUp()
                .toList()
        }

        val sourceDirs = files(*coverageSourceDirsNames)

        classDirectories.setFrom(classFiles)
        sourceDirectories.setFrom(jvmMainSourceSet.collectSources())

        executionData.setFrom(files("${buildDir}/jacoco/jvmTest.exec"))
    }
}

configure<JacocoPluginExtension> {
    toolVersion = "0.8.6"
}

val thisProjects = kotlinProjects.map { project(":$name-$it") }

thisProjects.forEach {
    it.kotlinProject()
}

tasks {
    val mergeTestCoverageReport by creating(JacocoMerge::class) {
        group = "Reporting"
        val pTasks = Callable { thisProjects.map { it.tasks["jvmTest"] } }
        dependsOn(pTasks)
        executionData(pTasks)
    }
    val rootTestCoverageReport by creating(JacocoReport::class) {
        dependsOn(mergeTestCoverageReport)
        group = "Reporting"
        description = "Generate Jacoco coverage reports."
        val coverageSourceDirs = thisProjects.map {
            it.tasks.getByName<JacocoReport>("testCoverageReport").sourceDirectories
        }

        val classFiles = Callable {
            thisProjects.map {
                it.tasks.getByName<JacocoReport>("testCoverageReport").classDirectories
            }
        }

        classDirectories.setFrom(classFiles)
        sourceDirectories.setFrom(coverageSourceDirs)

        executionData.setFrom(mergeTestCoverageReport)

        reports {
            xml.isEnabled = true
            html.isEnabled = true
        }
    }
    val dokkaHtmlMultiModule by getting(DokkaMultiModuleTask::class)
    val pagesDest = File(projectDir, "public")
    val gitlabPagesCreateDocs by creating(Copy::class) {
        group = "Documentation"
        dependsOn(dokkaHtmlMultiModule)
        from(dokkaHtmlMultiModule)
        into(File(pagesDest, "docs"))
    }
    val gitlabPagesCreateCoverage by creating(Copy::class) {
        group = "Reporting"
        dependsOn(rootTestCoverageReport)
        from(rootTestCoverageReport)
        into(File(pagesDest, "coverage"))
    }
    val gitlabPagesCreate by creating(Copy::class) {
        group = "Reporting"
        dependsOn(gitlabPagesCreateDocs, gitlabPagesCreateCoverage)
        File(projectDir, "pages").listFiles()!!.forEach {
            from(it)
        }
        destinationDir = pagesDest
    }
    val gitlabPagesClear by creating(Delete::class) {
        delete = setOf(pagesDest)
    }
    val clean by getting {
        dependsOn(gitlabPagesClear)
    }
}
