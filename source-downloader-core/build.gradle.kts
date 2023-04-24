import org.springframework.boot.gradle.tasks.bundling.BootBuildImage
import org.springframework.boot.gradle.tasks.bundling.BootJar

description = "source-downloader-core"

plugins {
    id("maven-publish")
    id("org.springframework.boot")
    kotlin("plugin.spring")
    id("org.graalvm.buildtools.native") version "0.9.20"
}

dependencies {
    // basic
    implementation(project(":source-downloader-sdk"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.flywaydb:flyway-core")
    runtimeOnly("com.h2database:h2")
    // kotlin
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.6.4")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")

    // others
    implementation("org.apache.commons:commons-collections4:4.4")
    implementation("com.google.guava:guava:31.1-jre")
    implementation("com.vladmihalcea:hibernate-types-60:2.21.1")
    implementation("org.springframework.retry:spring-retry")

    // 可分离只有rss/jackett source组件需要
    implementation("com.apptasticsoftware:rssreader:3.3.0")

    implementation("com.github.atomashpolskiy:bt-core:1.10")
    implementation("com.dgtlrepublic:anitomyJ:0.0.7")

    // TODO 不用这个库一个简单的请求引用了各种乱七八糟重复功能的依赖
    implementation("com.github.plexpt:chatgpt:4.0.5")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")

    // 内置插件，单纯为了方便
    implementation("xyz.shoaky:source-downloader-mikan-plugin:1.0.0")
    implementation(project(":plugins:source-downloader-dlsite-plugin"))
    implementation(project(":plugins:source-downloader-tagger-plugin"))

}

tasks.withType<BootBuildImage> {
    imageName.set("source-downloader")
}

tasks.withType<BootJar> {
    layered {
        application {
            intoLayer("spring-boot-loader") {
                include("org/springframework/boot/loader/**")
            }
            intoLayer("application")
        }
        dependencies {
            intoLayer("source-downloader-plugins") {
                include("xyz.shoaky:source-downloader-*-plugin:*")
            }
            intoLayer("snapshot-dependencies") {
                include("*:*:*SNAPSHOT")
            }
            intoLayer("dependencies")
        }
        layerOrder.addAll("dependencies", "spring-boot-loader", "snapshot-dependencies", "source-downloader-plugins", "application")
    }
}

graalvmNative {
    binaries.all {
        resources.includedPatterns.add(".*.yaml")
        resources.includedPatterns.add(".*.yml")
        resources.autodetect()
    }
}