rootProject.name = "source-downloader"

include(":sdk")
include(":core")
include(":plugins:common-plugin")
include(":plugins:telegram4j-plugin")
include(":common")

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/bootstrap") }
    }
}