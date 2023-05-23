import org.gradle.internal.os.OperatingSystem
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

description = "source-downloader-telegram-plugin"
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
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    implementation("com.google.zxing:core:3.5.1")
    implementation(platform("it.tdlight:tdlight-java-bom:3.0.8+td.1.8.14"))
    implementation("it.tdlight:tdlight-java")

    val os = OperatingSystem.current()
    // val familyName = os.familyName.replace(" ", "")
    val arch = System.getProperty("os.arch")
    // TODO 忽略不存在的依赖错误
    // implementation("it.tdlight:tdlight-natives-$familyName-$arch")
    if (os.isWindows && "amd64" == arch) {
        implementation(group = "it.tdlight", name = "tdlight-natives", classifier = "windows_amd64")
    }
    if (os.isMacOsX && "amd64" == arch) {
        implementation(group = "it.tdlight", name = "tdlight-natives", classifier = "macos_amd64")
    }
    if (os.isLinux && "amd64" == arch) {
        implementation(group = "it.tdlight", name = "tdlight-natives", classifier = "linux_amd64_ssl3")
    }
    if (os.isLinux && "aarch64" == arch) {
        implementation(group = "it.tdlight", name = "tdlight-natives", classifier = "linux_arm64_ssl3")
    }
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
