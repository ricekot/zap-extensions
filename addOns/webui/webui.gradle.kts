import com.github.gradle.node.npm.task.NpmTask
import org.zaproxy.gradle.addon.manifest.ManifestExtension

description = "A web UI for ZAP."

plugins {
    id("com.github.node-gradle.node") version "7.0.2"
}

node {
    version = "20.13.0"
    download = true
    nodeProjectDir.set(file("src/main/webui"))
}

tasks.register<NpmTask>("assembleWebUi") {
    description = "Build the web UI project files with npm and package them with the ZAP add-on."
    dependsOn("npmInstall")
    args.set(arrayListOf("run", "build"))

    (zapAddOn as ExtensionAware)
        .extensions
        .getByType(ManifestExtension::class.java)
        .files
        .setFrom(node.nodeProjectDir.dir("dist"))
    val javaExtension =
        project.extensions.getByType(JavaPluginExtension::class.java)
    javaExtension
        .sourceSets
        .named(
            SourceSet.MAIN_SOURCE_SET_NAME,
        ) {
            this.resources.srcDir(node.nodeProjectDir.dir("dist"))
        }
}

project.tasks.assemble.get().dependsOn(tasks.getByName("assembleWebUi"))

zapAddOn {
    addOnName.set("Web UI")

    manifest {
        author.set("ZAP Dev Team")

        dependencies {
            addOns {
                register("commonlib")
                register("network") {
                    version.set(">=0.13.0")
                }
            }
        }
    }
}

dependencies {
    zapAddOn("commonlib")
    zapAddOn("network")

    implementation("org.thymeleaf:thymeleaf:3.1.2.RELEASE")
    implementation(libs.log4j.slf4j2) {
        // Provided by ZAP.
        exclude(group = "org.apache.logging.log4j")
    }

//    val nettyVersion = "4.1.100.Final"
//    implementation("io.netty:netty-codec:$nettyVersion")
//    implementation("io.netty:netty-codec-http:$nettyVersion")
//    implementation("io.netty:netty-handler:$nettyVersion")
//    implementation("io.netty:netty-transport:$nettyVersion")

    compileOnly("jakarta.websocket:jakarta.websocket-api:2.1.1")
}
