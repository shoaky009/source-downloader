plugins {
    `java-library`
}

// repositories {
//     rootProject.repositories.add(
//         maven {
//             url = uri("https://mvn.mchv.eu/repository/mchv/")
//         }
//     )
// }

dependencies {
    implementation(project(":sdk"))
    implementation(project(":common"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation(libs.mockito.junit.jupiter)
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")

    implementation(libs.zxing.core)
    implementation(libs.telegram4j)
    // implementation(platform("it.tdlight:tdlight-java-bom:3.4.0+td.1.8.26"))
    // implementation("it.tdlight:tdlight-java")
    // implementation(group = "it.tdlight", name = "tdlight-natives", classifier = "macos_arm64")
}