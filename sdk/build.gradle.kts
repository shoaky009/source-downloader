description = "A SDK for SourceDownloader component development"

plugins {
    `java-library`
    `maven-publish`
    signing
    alias(libs.plugins.dokka)
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")

    api(libs.jackson.core)
    api(libs.jackson.datatype.jsr310)
    api(libs.jackson.kotlin.module)

    api(libs.slf4j.api)
    api(libs.logback.classic)

    api(libs.guava)
    api(libs.commons.lang3)
    compileOnlyApi(libs.graalvm.sdk)
}

java {
    withSourcesJar()
    withJavadocJar()
}

val isSnapshot = version.toString().endsWith("-SNAPSHOT", true)

publishing {
    publications {
        create<MavenPublication>("library") {
            from(components["java"])
            groupId = "io.github.shoaky009"
            artifactId = "${rootProject.name}-${project.name}"
            version = version.toString()

            pom {
                name.set(project.name)
                url.set("https://github.com/shoaky009/source-downloader")
                inceptionYear.set("2023")
                description.set(project.description)
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