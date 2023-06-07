import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

description = "source-downloader-common-plugin"
plugins {
    `java-library`
    id("maven-publish")
}

group = "xyz.shoaky"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":source-downloader-sdk"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.mockito:mockito-junit-jupiter:5.3.1")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")

    implementation("com.apptasticsoftware:rssreader:3.3.0")
    implementation("com.dgtlrepublic:anitomyJ:0.0.7")
    implementation("com.github.atomashpolskiy:bt-core:1.10")
    implementation("com.github.atomashpolskiy:bt-dht:1.10")
    compileOnly("org.springframework:spring-web:6.0.8")
    implementation("org.apache.tika:tika-core:2.7.0")
    implementation("org.jsoup:jsoup:1.15.4")
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
