plugins {
    `java-library`
    // 可以移除只是为了看打包成fat jar的大小
    // alias(libs.plugins.spring.boot)
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

    // others
    implementation(libs.commons.collections4)
    implementation(libs.jackson.dataformat.yaml)
    implementation(libs.jackson.kotlin.module)
    implementation(libs.bundles.cel)
    api(libs.bundles.exposed)
    implementation(libs.json.path)
    implementation(libs.cron.utils)
    implementation(libs.kotlin.retry)

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.mockito.core)
    testImplementation(libs.jsonschema.generator)
    testImplementation(libs.spring.jdbc)
    testRuntimeOnly(project(":plugins:common-plugin"))
}
