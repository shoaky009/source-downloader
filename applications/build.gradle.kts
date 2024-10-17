import org.springframework.boot.gradle.tasks.bundling.BootJar

tasks.jar {
    enabled = false
}

subprojects {
    apply(plugin = "com.gorylenko.gradle-git-properties")
    apply(plugin = "jacoco-report-aggregation")
    // 比其他的好使可以自定义分层, 所有应用层都用这个打包
    apply(plugin = "org.springframework.boot")

    dependencies {
        testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
        implementation(project(":core"))
        resolveBuildInPlugins()
    }

    tasks.withType<BootJar> {
        exclude("config.yaml")
        layered {
            application {
                intoLayer("spring-boot-loader") {
                    include("org/springframework/boot/loader/**")
                }
                intoLayer("application")
            }
            dependencies {
                intoLayer("source-downloader-plugins") {
                    include("io.github.shoaky:*-plugin:*")
                }
                intoLayer("snapshot-dependencies") {
                    include("*:*:*SNAPSHOT")
                }
                intoLayer("dependencies")
            }
            layerOrder.addAll(
                "dependencies",
                "spring-boot-loader",
                "snapshot-dependencies",
                "source-downloader-plugins",
                "application"
            )
        }
    }
}

fun DependencyHandlerScope.resolveBuildInPlugins() {
    if (project.hasProperty("sdPlugins")) {
        val sdPlugins = project.property("sdPlugins").toString()
        println("Prepare built-in plugins: $sdPlugins")
        if (sdPlugins == "all") {
            project(":plugins").dependencyProject.childProjects.map { it.key }
        } else {
            sdPlugins.split(",")
                .filter { it.isNotBlank() }
                .map {
                    "$it-plugin"
                }
        }.forEach {
            println("Add built-in plugin $it")
            runtimeOnly(project(":plugins:$it"))
        }
    } else {
        // 这里为了平时开发方便，如果没有指定插件就默认加载所有插件
        runtimeOnly(project(":plugins:common-plugin"))
        runtimeOnly(project(":plugins:telegram4j-plugin"))
        runtimeOnly(project(":plugins:foreign-plugin"))
    }
}