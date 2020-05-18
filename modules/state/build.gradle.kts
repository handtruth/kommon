kotlin {
    sourceSets.all {
        with(languageSettings) {
            useExperimentalAnnotation("kotlin.ExperimentalStdlibApi")
        }
    }
}
