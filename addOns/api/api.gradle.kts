import com.google.protobuf.gradle.builtins
import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.ofSourceSet
import com.google.protobuf.gradle.plugins
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc
import net.ltgt.gradle.errorprone.errorprone
import org.zaproxy.gradle.addon.AddOnStatus

description = "Provides ZAP gRPC API."

plugins {
    id("com.google.protobuf") version "0.8.18"
}

zapAddOn {
    addOnName.set("API")
    addOnStatus.set(AddOnStatus.ALPHA)
    zapVersion.set("2.11.1")

    manifest {
        author.set("ZAP Dev Team")
        url.set("https://www.zaproxy.org/docs/desktop/addons/api/")

        helpSet {
            baseName.set("help%LC%.helpset")
            localeToken.set("%LC%")
        }
    }
}

crowdin {
    configuration {
        tokens.put("%messagesPath%", "org/zaproxy/addon/${zapAddOn.addOnId.get()}/resources/")
        tokens.put("%helpPath%", "")
    }
}

dependencies {
    api("io.grpc:grpc-protobuf:1.43.2")
    api("io.grpc:grpc-stub:1.43.2")
    api("io.grpc:grpc-netty:1.43.2")
    api("javax.annotation:javax.annotation-api:1.3.2")

    testImplementation(project(":testutils"))
    testImplementation("org.apache.logging.log4j:log4j-core:2.17.1")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.19.2"
    }
    plugins {
        id("grpc_java") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.43.2"
        }
    }
    generateProtoTasks {
        ofSourceSet("main").forEach {
            it.plugins {
                id("grpc_java")
            }
            it.builtins {
                java
            }
        }
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.errorprone.excludedPaths.set(".*/build/generated/.*")
}

spotless {
    project.plugins.withType(JavaPlugin::class) {
        java {
            targetExclude("build/generated/")
        }
    }
}

sourceSets {
    main {
        java {
            srcDirs(
                "build/generated/source/proto/main/java",
                "build/generated/source/proto/main/grpc_java"
            )
        }
    }
}
