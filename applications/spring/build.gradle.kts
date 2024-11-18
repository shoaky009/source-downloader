import org.springframework.boot.gradle.tasks.aot.ProcessAot
import org.springframework.boot.gradle.tasks.aot.ProcessTestAot

buildscript {
    dependencies {
        classpath(libs.jib.spring.boot)
    }
}

plugins {
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.graalvm)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.jib)
}

group = "io.github.shoaky"
version = "0.1.0-SNAPSHOT"

dependencies {
    api(platform(libs.spring.boot.dependencies))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.data:spring-data-commons")
    implementation(libs.exposed.spring.boot.stater)
    implementation(libs.spring.boot.admin.client)
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    runtimeOnly(libs.kotlinx.coroutines.reactor)
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}

tasks.bootJar {
    exclude("config.yaml")
    layered {
        application {
            intoLayer("spring-boot-loader") {
                include("org/springframework/boot/loader/**")
            }
            intoLayer("application")
        }
        dependencies {
            intoLayer("source-downloader-plugins") {
                include("io.github.shoaky:*-plugin:*")
            }
            intoLayer("source-downloader-core") {
                include("io.github.shoaky:core:*")
                include("io.github.shoaky:sdk:*")
                include("io.github.shoaky:common:*")
            }
            intoLayer("snapshot-dependencies") {
                include("*:*:*SNAPSHOT")
            }
            intoLayer("dependencies")
        }
        layerOrder.addAll(
            "dependencies",
            "spring-boot-loader",
            "snapshot-dependencies",
            "source-downloader-core",
            "source-downloader-plugins",
            "application"
        )
    }
}

tasks.bootBuildImage {
    imageName.set("source-downloader")
    runImage.set("azul/zulu-openjdk-alpine:21-jre")
    environment.put("TZ", "Asia/Shanghai")
    environment.put("SOURCE_DOWNLOADER_DATA_LOCATION", "/app/data/")
    environment.put("SOURCE_DOWNLOADER_PLUGIN_LOCATION", "/app/plugin/")
    tags.add("dev")
}

tasks.withType<ProcessAot> {
    enabled = project.hasProperty("native")
}

tasks.withType<ProcessTestAot> {
    enabled = project.hasProperty("native")
}

springBoot {
    buildInfo()
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
}

// graalvmNative {
//     agent {
//         metadataCopy {
//             inputTaskNames.add("test")
//             outputDirectories.add("src/main/resources/META-INF/native-image")
//             mergeWithExisting.set(true)
//         }
//     }
//
//     binaries.all {
//         // pgoInstrument = true
//         imageName.set("source-downloader")
//         buildArgs("-H:+UnlockExperimentalVMOptions", "-march=native")
//         val os = DefaultNativePlatform.getCurrentOperatingSystem()
//         if (os.isLinux) {
//             buildArgs("--gc=G1")
//         }
//         resources.includedPatterns.add(".*.yaml")
//         resources.includedPatterns.add(".*.yml")
//         resources.autodetect()
//         quickBuild.set(true)
//     }
//
// }