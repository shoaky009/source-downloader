description = "source-downloader-sdk"

plugins {
    `java-library`
    `maven-publish`
    signing
}

version = "0.0.1-SNAPSHOT"

dependencies {

    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")

    api(platform("com.fasterxml.jackson:jackson-bom:2.15.2"))
    api(platform("org.springframework.boot:spring-boot-dependencies:3.1.0"))
    api("com.fasterxml.jackson.core:jackson-core")
    api("com.fasterxml.jackson.module:jackson-module-kotlin")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    api("org.slf4j:slf4j-api")
    api("ch.qos.logback:logback-classic")

    api("com.google.guava:guava:32.0.0-jre")
    api("org.apache.commons:commons-lang3:3.12.0")
}

java {
    withSourcesJar()
    withJavadocJar()
}

val isSnapshot = version.toString().endsWith("-SNAPSHOT", true)

publishing {
    publications {
        repositories {
            maven {
                name = "sonatype"
                url = if (isSnapshot) {
                    uri("https://s01.oss.sonatype.org/content/repositories/snapshots")
                } else {
                    uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                }

                val sonatypeUsername: String? by project
                val sonatypePassword: String? by project

                credentials {
                    username = sonatypeUsername ?: System.getenv("SONATYPE_USERNAME")
                    password = sonatypePassword ?: System.getenv("SONATYPE_PASSWORD")
                }
            }
        }

        create<MavenPublication>("library") {
            from(components["java"])
            groupId = "io.github.shoaky009"
            artifactId = project.name
            version = version.toString()

            pom {
                name.set(project.name)
                url.set("https://github.com/shoaky009/source-downloader")
                inceptionYear.set("2023")
                description.set("A SDK for SourceDownloader component development")
                licenses {
                    license {
                        name.set("GPL-3.0")
                        url.set("https://github.com/shoaky009/source-downloader/LICENSE")
                    }
                }
                developers {
                    developer {
                        id.set("shoaky009")
                        name.set("shoaky009")
                    }
                }
                scm {
                    url.set("https://github.com/shoaky009/source-downloader")
                    connection.set("scm:git:git://github.com/shoaky009/source-downloader.git")
                    developerConnection.set("scm:git:ssh://git@github.com:shoaky009/source-downloader.git")
                }
            }
        }
    }
}
if (isSnapshot.not()) {
    signing {
        val signingKey: String? by project
        val signingPassword: String? by project
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications["library"])
    }
}