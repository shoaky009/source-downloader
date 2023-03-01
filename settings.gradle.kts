rootProject.name = "source-downloader"

include(":source-downloader-core")
include(":source-downloader-sdk")

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