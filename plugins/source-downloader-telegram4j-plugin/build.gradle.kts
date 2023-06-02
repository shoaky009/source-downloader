import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

description = "source-downloader-telegram4j-plugin"
plugins {
    `java-library`
    id("maven-publish")
}

group = "xyz.shoaky"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
    maven { url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots") }
    maven { url = uri("https://mvn.mchv.eu/repository/mchv") }
}

dependencies {
    implementation(project(":source-downloader-sdk"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.2")
    testImplementation("org.mockito:mockito-junit-jupiter:5.3.1")

    implementation("com.google.zxing:core:3.5.1")
    api("com.telegram4j:telegram4j-core:0.1.0-SNAPSHOT")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
