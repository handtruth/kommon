plugins {
    id("com.gladed.androidgitversion")
    `java-platform`
    `maven-publish`
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["javaPlatform"])
        }
    }
}

dependencies.constraints {
    val variants = sequenceOf(
        "wasm32", "linuxArm32Hfp", "linuxArm64", "linuxMips32", "linuxMipsel32", "linuxX64", "mingwX86",
        "mingwX64", "ios", "iosArm32", "iosArm64", "js", "android", "android-debug", "jvm", "metadata"
    ).map { it.toLowerCase() }
    fun module(name: String) {
        api("$group:kommon-$name:$version")
        variants.forEach {
            api("$group:kommon-$name-$it:$version")
        }
    }
    module("log")
}
