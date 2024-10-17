import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform

plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.kotlin.spring)
    id("jacoco-report-aggregation")
    alias(libs.plugins.graalvm)
    id("org.springdoc.openapi-gradle-plugin") version "1.8.0"
    alias(libs.plugins.gradle.git.properties)
}

dependencies {
    // basic
    implementation(project(":sdk"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.data:spring-data-commons")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.flywaydb:flyway-core")
    implementation(libs.sqlite.jdbc)
    // kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    implementation(libs.kotlinx.coroutines.core)
    runtimeOnly(libs.kotlinx.coroutines.core.jvm)
    runtimeOnly(libs.kotlinx.coroutines.reactor)

    // others
    implementation(libs.commons.collections4)
    implementation("org.springframework.retry:spring-retry")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
    implementation(enforcedPlatform(libs.cel.bom))
    implementation(libs.bundles.cel)
    implementation(libs.bundles.exposed)
    implementation(libs.json.path)

    resolveBuildInPlugins()
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    val isGenerateOpenApiDocs = gradle.startParameter.taskNames.contains("generateOpenApiDocs")
    if (isGenerateOpenApiDocs) {
        implementation(libs.therapi.runtime.javadoc)
        kapt(libs.therapi.javadoc.scribe)
        implementation(libs.springdocs.openapi.starter.webmvc.api)
    }
    testImplementation(libs.jsonschema.generator)
}

fun DependencyHandlerScope.resolveBuildInPlugins() {
    if (project.hasProperty("sdPlugins")) {
        val sdPlugins = project.property("sdPlugins").toString()
        println("Prepare built-in plugins: $sdPlugins")
        if (sdPlugins == "all") {
            project(":plugins").dependencyProject.childProjects.map { it.key }
        } else {
            sdPlugins.split(",")
                .filter { it.isNotBlank() }
                .map {
                    "$it-plugin"
                }
        }.forEach {
            println("Add built-in plugin $it")
            runtimeOnly(project(":plugins:$it"))
        }
    } else {
        // 这里为了平时开发方便，如果没有指定插件就默认加载所有插件
        runtimeOnly(project(":plugins:common-plugin"))
        runtimeOnly(project(":plugins:telegram4j-plugin"))
        runtimeOnly(project(":plugins:foreign-plugin"))
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
            intoLayer("snapshot-dependencies") {
                include("*:*:*SNAPSHOT")
            }
            intoLayer("dependencies")
        }
        layerOrder.addAll(
            "dependencies",
            "spring-boot-loader",
            "snapshot-dependencies",
            "source-downloader-plugins",
            "application"
        )
    }
}

tasks.processAot {
    enabled = project.hasProperty("native")
}

tasks.processTestAot {
    enabled = project.hasProperty("native")
}

tasks.testCodeCoverageReport {
    this.reports {
        html.required.set(false)
        csv.required.set(false)
        xml.required.set(true)
        xml.outputLocation.set(
            rootProject.layout.buildDirectory.file("reports/jacoco/testCodeCoverageReport.xml")
        )
    }
}

springBoot {
    buildInfo()
}

graalvmNative {
    agent {
        metadataCopy {
            inputTaskNames.add("test")
            outputDirectories.add("src/main/resources/META-INF/native-image")
            mergeWithExisting.set(true)
        }
    }

    binaries.all {
        // pgoInstrument = true
        imageName.set("source-downloader")
        buildArgs("-H:+UnlockExperimentalVMOptions", "-march=native")
        val os = DefaultNativePlatform.getCurrentOperatingSystem()
        if (os.isLinux) {
            buildArgs("--gc=G1")
        }
        resources.includedPatterns.add(".*.yaml")
        resources.includedPatterns.add(".*.yml")
        resources.autodetect()
        quickBuild.set(true)
    }

}