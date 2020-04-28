plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("android.extensions")
    id("com.gladed.androidgitversion")
}

androidGitVersion {
    prefix = "v"
}

android {
    compileSdkVersion(29)
    buildToolsVersion("29.0.3")

    defaultConfig {
        applicationId = "com.handtruth.kommon.testapp"
        minSdkVersion(16)
        targetSdkVersion(29)
        versionCode = androidGitVersion.code().let { if (it == 0) 1 else it }
        versionName = androidGitVersion.name()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        @Suppress("UNUSED_VARIABLE")
        val release by getting {
            isMinifyEnabled = true
            isShrinkResources = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    val platformVersion: String by project
    val platform = platform("com.handtruth.internal:platform:$platformVersion")
    implementation(platform)

    //implementation("com.handtruth.kommon:kommon-log")

    implementation(project(":kommon-log"))

    implementation(kotlin("stdlib-jdk8"))
    implementation("com.google.android.material:material")
    testImplementation(kotlin("test-junit"))

    androidTestImplementation(kotlin("test-junit"))
    androidTestImplementation("androidx.test:core")
    androidTestImplementation("androidx.test:runner")
    androidTestImplementation("androidx.test.ext:junit")
}
