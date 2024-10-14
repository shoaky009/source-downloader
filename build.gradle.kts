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
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/exposed/release") }
        maven { url = uri("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/bootstrap") }
        maven { url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots") }
        maven { url = uri("https://jitpack.io") }
    }
}

scmVersion {
    versionCreator("simple")
    useHighestVersion.set(true)
}

version = scmVersion.version
val isSnapshot = version.toString().endsWith("-SNAPSHOT", true)
val javaVersion = 21

subprojects {
    version = rootProject.version
    group = rootProject.group

    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "java")
    apply(plugin = "jacoco")
    project(":core").apply(plugin = "org.jetbrains.kotlin.kapt")

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
        compilerOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
        }
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(javaVersion))
        }
    }

    val archiveName = "source-downloader-$name"
    tasks.jar {
        manifest {
            attributes["Implementation-Version"] = project.version
            attributes["Implementation-Title"] = archiveName
        }
    }
    tasks.withType<AbstractArchiveTask> {
        archiveBaseName.set(archiveName)
    }

    kotlin {
        jvmToolchain(javaVersion)
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