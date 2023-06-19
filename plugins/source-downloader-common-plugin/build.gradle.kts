description = "source-downloader-common-plugin"
plugins {
    `java-library`
}

dependencies {
    implementation(project(":source-downloader-sdk"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    testImplementation("org.mockito:mockito-junit-jupiter")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    compileOnly("org.springframework:spring-web")

    implementation(libs.rssreader)
    implementation(libs.anitomyJ)
    implementation(libs.bundles.bt)
    implementation(libs.tika.core)
    implementation(libs.jsoup)
    implementation("it.skrape:skrapeit:1.2.2")
}