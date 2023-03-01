import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "xyz.shoaky"
version = "0.0.1-SNAPSHOT"

plugins {
    id("io.spring.dependency-management") version "1.1.0"
    kotlin("jvm")
}

allprojects {
    repositories {
        mavenLocal()
        maven {
            url = uri("https://repo.huaweicloud.com/repository/maven/")
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

subprojects {
    version = rootProject.version
    group = rootProject.group

    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "java")
    apply(plugin = "io.spring.dependency-management")

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "17"
        }
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}