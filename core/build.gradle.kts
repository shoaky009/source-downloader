plugins {
    alias(libs.plugins.springboot)
    alias(libs.plugins.kotlin.spring)
    id("jacoco-report-aggregation")
    // alias(libs.plugins.graalvm)
}

dependencies {
    // basic
    implementation(project(":sdk"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
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
    implementation(libs.hibernate.types)
    implementation("org.springframework.retry:spring-retry")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
    implementation(enforcedPlatform(libs.cel.bom))
    implementation(libs.bundles.cel)
    // implementation("org.openjdk.nashorn:nashorn-core:15.4")
    implementation(libs.bundles.exposed)
//    implementation("org.springdoc:springdoc-openapi-starter-webmvc-api:2.2.0")
//    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0")

    resolveBuildInPlugins()
    implementation("org.springframework.boot:spring-boot-starter-actuator")
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
    }
}

tasks.bootBuildImage {
    imageName.set("source-downloader")
    runImage.set("azul/zulu-openjdk-alpine:20-jre")
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

// graalvmNative {
//     binaries.all {
//         resources.includedPatterns.add(".*.yaml")
//         resources.includedPatterns.add(".*.yml")
//         resources.autodetect()
//     }
// }