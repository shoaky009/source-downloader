tasks.jar {
    enabled = false
}

subprojects {
    apply(plugin = "com.gorylenko.gradle-git-properties")
    apply(plugin = "jacoco-report-aggregation")
    // apply(plugin = "com.google.cloud.tools.jib")

    dependencies {
        testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
        implementation(project(":core"))
        resolveBuildInPlugins()
    }
    tasks.create("setupContainerDirs", Copy::class) {
        project.mkdir(
            layout.buildDirectory.dir("generated/container/app/data")
        )
        project.mkdir(
            layout.buildDirectory.dir("generated/container/app/plugins")
        )
    }
}

fun DependencyHandlerScope.resolveBuildInPlugins() {
    if (project.hasProperty("sdPlugins")) {
        val sdPlugins = project.property("sdPlugins").toString()
        println("Prepare built-in plugins: $sdPlugins")
        if (sdPlugins == "all") {
            listOf("common", "telegram4j", "foreign")
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