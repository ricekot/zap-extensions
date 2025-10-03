plugins {
    `kotlin-dsl`
    id("com.diffplug.spotless") version "6.25.0"
    id("org.zaproxy.common") version "0.5.0"
}

repositories {
    mavenCentral()
}

spotless {
    kotlin {
        ktlint()
    }

    kotlinGradle {
        ktlint()
    }
}

dependencies {
    implementation("commons-codec:commons-codec:1.19.0")
    implementation("io.github.bonigarcia:webdrivermanager:6.3.2") {
        exclude("com.fasterxml.jackson.core")
    }
    implementation("com.diffplug.spotless:spotless-plugin-gradle:6.25.0")
}
