description = "source-downloader-sdk"

plugins {
    `java-library`
    `maven-publish`
}

dependencies {

    api("org.jetbrains.kotlin:kotlin-stdlib")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")

    api("com.fasterxml.jackson.core:jackson-core:2.14.2")
    api("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.2")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.14.2")

    api("org.slf4j:slf4j-api:2.0.6")
    api("ch.qos.logback:logback-classic:1.4.5")

    api("com.google.guava:guava:31.1-jre")
    api("org.jsoup:jsoup:1.15.3")
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