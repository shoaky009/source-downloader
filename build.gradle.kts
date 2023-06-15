group = "io.github.shoaky"

plugins {
    alias(libs.plugins.kotlin.jvm)
    jacoco
    alias(libs.plugins.axion.release)
}

allprojects {
    repositories {
        mavenLocal()
        maven { url = uri("https://repo.huaweicloud.com/repository/maven/") }
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots") }
    }
}

scmVersion {
    useHighestVersion.set(true)
}

version = scmVersion.version

subprojects {
    version = rootProject.version
    group = rootProject.group

    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "java")
    apply(plugin = "jacoco")

    tasks.test {
        useJUnitPlatform()
    }

    tasks.jacocoTestReport {
        dependsOn(tasks.test)
        reports {
            xml.required.set(false)
            html.required.set(false)
            csv.required.set(true)
        }
    }

    tasks.compileKotlin {
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