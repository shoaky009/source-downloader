import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

description = "source-downloader-telegram4j-plugin"
plugins {
    `java-library`
    id("maven-publish")
}

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
    testImplementation("org.mockito:mockito-junit-jupiter:5.3.1")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")

    implementation("com.google.zxing:core:3.5.1")
    implementation("com.telegram4j:telegram4j-core:0.1.0-SNAPSHOT")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "17"
        }
    }
}

