plugins {
    `java-library`
    alias(libs.plugins.graalvm)
}

dependencies {
    implementation(project(":sdk"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    testImplementation(libs.mockito.junit.jupiter)
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.instancio:instancio-junit:3.3.0")

    implementation(libs.rssreader)
    implementation(libs.anitomyJ)
    implementation(libs.bundles.bt)
    implementation(libs.tika.core)
    implementation(libs.tika.langdetect.optimaize)
    implementation(libs.jsoup)
    implementation(libs.fuzzywuzzy)
    implementation(libs.commons.text)
    implementation(libs.kotlin.retry)
}

tasks.nativeCompile {
    this.enabled = false
}
tasks.nativeCompileClasspathJar {
    this.enabled = false
}
tasks.nativeBuild {
    this.enabled = false
}
graalvmNative {
    agent {
        this.defaultMode.set("standard")
        metadataCopy {
            inputTaskNames.add("test")
            outputDirectories.add("src/main/resources/META-INF/native-image")
            mergeWithExisting.set(true)
        }
    }
}
