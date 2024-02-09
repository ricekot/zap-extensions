import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.zaproxy.gradle.addon.manifest.ManifestExtension


description = "A Proof of Concept add-on for potential ZAP web based UIs."

zapAddOn {
    addOnName.set("Web UI PoC")

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

    tasks.register("copyPocs") {
        val srcDir = File(project.projectDir, "src/main/pocs")
        val objectMapper = ObjectMapper()
        for (dir in srcDir.listFiles()!!) {
            val packageJson = File(dir, "package.json")
            if (packageJson.exists()) {
                val packageJsonObj = objectMapper.readValue(packageJson, JsonNode::class.java)
                if (packageJsonObj.has("zaproxy")) {
                    val zaproxyObj = packageJsonObj.get("zaproxy")
                    if (zaproxyObj.has("outputDirectory")) {
                        val pocOutputDir = File(dir, zaproxyObj.get("outputDirectory").asText())
                        copy {
                            from(pocOutputDir)
                            into(layout.buildDirectory.dir("pocs/webuipoc/${dir.name}"))
                        }
                        continue
                    }
                }
            }
            copy {
                from(dir)
                into(layout.buildDirectory.dir("pocs/webuipoc/${dir.name}"))
            }
        }
        (this@zapAddOn as ExtensionAware)
            .extensions
            .getByType(ManifestExtension::class.java)
            .files
            .from(layout.buildDirectory.dir("pocs"))
        val javaExtension =
            project.extensions.getByType(JavaPluginExtension::class.java)
        javaExtension
            .sourceSets
            .named(
                SourceSet.MAIN_SOURCE_SET_NAME,
            ) {
                this.resources.srcDir(layout.buildDirectory.dir("pocs"))
            }
    }

    project.tasks.build.get().dependsOn(tasks.getByName("copyPocs"))
}

dependencies {
    zapAddOn("commonlib")
    zapAddOn("network")

    val nettyVersion = "4.1.100.Final"
    implementation("io.netty:netty-codec:$nettyVersion")
    implementation("io.netty:netty-codec-http:$nettyVersion")
    implementation("io.netty:netty-handler:$nettyVersion")
    implementation("io.netty:netty-transport:$nettyVersion")

    compileOnly("jakarta.websocket:jakarta.websocket-api:2.1.1")
}
