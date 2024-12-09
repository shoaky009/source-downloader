import com.google.cloud.tools.jib.gradle.extension.layerfilter.Configuration

buildscript {
    dependencies {
        classpath(libs.jib.layer.filter)
    }
}

plugins {
    kotlin("jvm")
    alias(libs.plugins.jib)
    application
}

group = "io.github.shoaky"
version = "0.1.0-SNAPSHOT"

dependencies {
    implementation(libs.hikaricp)
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.jib.get().dependsOn("setupContainerDirs")
jib {
    from {
        image = "azul/zulu-openjdk-alpine:21-jre"
        platforms {
            platform {
                os = "linux"
                architecture = "amd64"
            }
            platform {
                os = "linux"
                architecture = "arm64"
            }
        }
    }
    to {
        image = "shoaky009/source-downloader"
        tags = setOf("test-minimize")
    }
    container {
        creationTime = "USE_CURRENT_TIMESTAMP"
        environment = mapOf(
            "TZ" to "Asia/Shanghai",
            "SOURCE_DOWNLOADER_DATA_LOCATION" to "/app/data/",
            "SOURCE_DOWNLOADER_PLUGIN_LOCATION" to "/app/plugins/",
        )
        appRoot = "/app"
        workingDirectory = "/app"
        ports = listOf("8080")
        volumes = listOf("/tmp")
        extraClasspath = listOf("/app/plugins/*", "/app/plugins/lib/*")
    }
    extraDirectories.setPaths(layout.buildDirectory.dir("generated/container"))

    pluginExtensions {
        pluginExtension {
            this.implementation = "com.google.cloud.tools.jib.gradle.extension.layerfilter.JibLayerFilterExtension"
            configuration(Action<Configuration> {
                filters {
                    filter {
                        glob = "**/*.jar"
                        toLayer = "application-dependencies"
                    }
                    filter {
                        glob = "**/*SNAPSHOT*.jar"
                        toLayer = "snapshot-dependencies"
                    }
                    filter {
                        glob = "**/source-downloader*.jar"
                        toLayer = "source-downloader-dependencies"
                    }
                    filter {
                        glob = "**/source-downloader-*-plugin-*.jar"
                        toLayer = "source-downloader-plugin"
                    }
                }
            })
        }
    }
}