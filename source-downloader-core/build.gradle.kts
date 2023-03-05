import org.springframework.boot.gradle.tasks.bundling.BootBuildImage

description = "source-downloader-core"

plugins {
    id("maven-publish")
    id("org.springframework.boot")
    kotlin("plugin.spring")
//    id("org.graalvm.buildtools.native") version "0.9.18"
}

dependencies {
    //basic
    implementation(project(":source-downloader-sdk"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation("org.flywaydb:flyway-core")
    runtimeOnly("com.h2database:h2")
    //kotlin
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")

    //others
    implementation("org.apache.commons:commons-collections4:4.4")
    implementation("com.google.guava:guava:31.1-jre")
    implementation("com.vladmihalcea:hibernate-types-60:2.21.1")
    implementation("com.apptasticsoftware:rssreader:3.3.0")
    implementation("org.springframework.retry:spring-retry")

    implementation("org.graalvm.js:js:22.3.1")
    implementation("org.graalvm.js:js-scriptengine:22.3.1")

    implementation("com.github.atomashpolskiy:bt-core:1.10")
    implementation("com.dgtlrepublic:anitomyJ:0.0.7")

    //内置插件
    implementation("xyz.shoaky:source-downloader-mikan:1.0.0")
}

tasks.named<BootBuildImage>("bootBuildImage") {
    imageName.set("source-downloader")
}

tasks.withType<Test> {
    filter {
        excludeTestsMatching("*CommonTest*")
        excludeTestsMatching("xyz.shoaky.sourcedownloader.api.*")
    }
}