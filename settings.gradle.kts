rootProject.name = "source-downloader"

include(":source-downloader-sdk")
include(":source-downloader-core")
include(":plugins:source-downloader-common-plugin")
// include(":plugins:source-downloader-telegram-plugin")
include(":plugins:source-downloader-telegram4j-plugin")

pluginManagement {
    val kotlinVersion: String by settings
    val springbootVersion: String by settings
    plugins {
        id("io.spring.dependency-management") version "1.1.0"
        id("org.springframework.boot") version springbootVersion
        kotlin("jvm") version kotlinVersion
        kotlin("plugin.spring") version kotlinVersion
    }
}