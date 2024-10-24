plugins {
    `java-library`
}

dependencies {
    // basic
    api(project(":sdk"))
    implementation(libs.flyway.core)
    implementation(libs.sqlite.jdbc)
    // kotlin
    api("org.jetbrains.kotlin:kotlin-reflect")
    api("org.jetbrains.kotlin:kotlin-stdlib")
    api(libs.kotlinx.coroutines.core)
    runtimeOnly(libs.kotlinx.coroutines.core.jvm)
    runtimeOnly(libs.kotlinx.coroutines.reactor)

    // others
    implementation(libs.commons.collections4)
    implementation(libs.spring.retry)
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
    implementation(libs.bundles.cel)
    api(libs.bundles.exposed)
    implementation(libs.json.path)
    implementation(libs.spring.expression)

    // 后面移除，不支持spel
    compileOnly(libs.spring.context)
    implementation("com.cronutils:cron-utils:9.2.1")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.mockito.core)
    testImplementation(libs.jsonschema.generator)
    testImplementation(libs.spring.jdbc)
    testRuntimeOnly(project(":plugins:common-plugin"))
}
//
// tasks.testCodeCoverageReport {
//     this.reports {
//         html.required.set(false)
//         csv.required.set(false)
//         xml.required.set(true)
//         xml.outputLocation.set(
//             rootProject.layout.buildDirectory.file("reports/jacoco/testCodeCoverageReport.xml")
//         )
//     }
// }
