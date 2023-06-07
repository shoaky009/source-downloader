description = "source-downloader-sdk"

plugins {
    `java-library`
    `maven-publish`
}

version = "0.0.1-SNAPSHOT"

dependencies {

    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")

    api(platform("com.fasterxml.jackson:jackson-bom:2.15.2"))
    api("com.fasterxml.jackson.core:jackson-core")
    api("com.fasterxml.jackson.module:jackson-module-kotlin")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    // api("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")

    api("org.slf4j:slf4j-api:2.0.7")
    api("ch.qos.logback:logback-classic:1.4.7")

    api("com.google.guava:guava:32.0.0-jre")
    api("org.apache.commons:commons-lang3:3.12.0")
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

java {
    withSourcesJar()
    withJavadocJar()
}