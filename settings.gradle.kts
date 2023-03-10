rootProject.name = "source-downloader"

include(":source-downloader-sdk")
include(":source-downloader-core")

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