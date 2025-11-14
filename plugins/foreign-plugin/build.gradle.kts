plugins {
    `java-library`
}

dependencies {
    implementation(project(":sdk"))
    implementation(project(":common"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation(libs.mockito.junit.jupiter)
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
}