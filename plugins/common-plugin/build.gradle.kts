plugins {
    `java-library`
}

dependencies {
    implementation(project(":sdk"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    testImplementation("org.mockito:mockito-junit-jupiter")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.instancio:instancio-junit:3.3.0")
    compileOnly("org.springframework:spring-web")

    implementation(libs.rssreader)
    implementation(libs.anitomyJ)
    implementation(libs.bundles.bt)
    implementation(libs.tika.core)
    implementation(libs.jsoup)
}