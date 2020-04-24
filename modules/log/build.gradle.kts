plugins {
    kotlin("multiplatform")
}

dependencies {
    "jvmCommonImplementation"(kotlin("reflect"))
    "jvmCommonImplementation"("org.slf4j:slf4j-api:1.7.30")
}
