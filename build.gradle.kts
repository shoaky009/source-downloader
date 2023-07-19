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
val isSnapshot = version.toString().endsWith("-SNAPSHOT", true)
val javaVersion = 17

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
        }
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(javaVersion))
        }
    }

    kotlin {
        jvmToolchain(javaVersion)
        // 打开k2 mockito有点问题
        // sourceSets.all {
        //     languageSettings {
        //         languageVersion = "2.0"
        //     }
        // }
    }
}

tasks.jar {
    enabled = false
}

tasks.register<DefaultTask>("versionTags") {
    val tags = mutableListOf<String>()
    if (isSnapshot) {
        tags.add("dev")
    } else {
        tags.add("latest")
        tags.add(version.toString())
    }
    print(tags.joinToString(","))
}