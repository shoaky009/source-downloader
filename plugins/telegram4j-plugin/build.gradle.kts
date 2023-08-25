plugins {
    `java-library`
}

dependencies {
    implementation(project(":sdk"))
    implementation(project(":common"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.mockito:mockito-junit-jupiter")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")

    implementation(libs.zxing.core)
    implementation(libs.telegram4j)
}