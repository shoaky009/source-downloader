rootProject.name = "source-downloader"

include(":sdk")
include(":core")
include(":plugins:common-plugin")
include(":plugins:telegram4j-plugin")
include(":plugins:foreign-plugin")
include(":common")
include(":applications:spring")

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/bootstrap") }
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
