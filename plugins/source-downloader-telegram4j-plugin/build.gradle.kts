description = "source-downloader-telegram4j-plugin"
plugins {
    `java-library`
}

dependencies {
    implementation(project(":source-downloader-sdk"))
    implementation(project(":source-downloader-common"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.mockito:mockito-junit-jupiter")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")

    implementation(libs.zxing.core)
    implementation(libs.telegram4j)
}