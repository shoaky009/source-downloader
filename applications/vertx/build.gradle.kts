plugins {
    kotlin("jvm")
}

group = "io.github.shoaky"
version = "0.1.0-SNAPSHOT"

dependencies {
    api(platform(libs.vertx.stack.depchain))
    implementation(libs.vertx.web)
    implementation(libs.vertx.micrometer.metrics)
    // implementation(libs.vertx.config.yaml)
    implementation(libs.vertx.kotlin.coroutines)
    implementation(libs.vertx.kotlin)
    implementation(libs.hikaricp)
    implementation(libs.micrometer.registry.jmx)
    implementation("io.micrometer:micrometer-registry-prometheus:1.13.4")
    testImplementation(kotlin("test"))
}


tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}