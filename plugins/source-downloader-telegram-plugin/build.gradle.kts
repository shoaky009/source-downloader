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

    implementation("com.google.zxing:core:3.5.1")
    implementation(platform("it.tdlight:tdlight-java-bom:2.8.10.6"))
    implementation("it.tdlight:tdlight-java")

    // 不能拼接 有些平台没有依赖
    val os = OperatingSystem.current()
    // val familyName = os.familyName.replace(" ", "")
    val arch = System.getProperty("os.arch")
    // TODO 忽略不存在的依赖错误
    // implementation("it.tdlight:tdlight-natives-$familyName-$arch")
    if (os.isWindows && "amd64" == arch) {
        implementation("it.tdlight:tdlight-natives-windows-amd64")
    }
    if (os.isMacOsX && "amd64" == arch) {
        implementation("it.tdlight:tdlight-natives-osx-amd64")
    }
    if (os.isLinux && "amd64" == arch) {
        implementation("it.tdlight:tdlight-natives-linux-amd64")
    }
    if (os.isLinux && "aarch64" == arch) {
        implementation("it.tdlight:tdlight-natives-linux-aarch64")
    }
    if (os.isLinux && "x86" == arch) {
        implementation("it.tdlight:tdlight-natives-linux-x86")
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
