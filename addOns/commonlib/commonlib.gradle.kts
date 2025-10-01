import org.zaproxy.gradle.addon.AddOnStatus

description = "A common library, for use by other add-ons."

zapAddOn {
    addOnName.set("Common Library")
    addOnStatus.set(AddOnStatus.RELEASE)

    manifest {
        author.set("ZAP Dev Team")
        url.set("https://www.zaproxy.org/docs/desktop/addons/common-library/")

        helpSet {
            baseName.set("help%LC%.helpset")
            localeToken.set("%LC%")
        }
    }
}

crowdin {
    configuration {
        file.set(file("$projectDir/gradle/crowdin.yml"))
        tokens.put("%helpPath%", "")
    }
}

dependencies {
    api(platform(libs.jackson.bom))
    api(libs.jackson.databind)
    api(libs.jackson.dataformat.xml)
    api(libs.jackson.dataformat.yaml)
    api(libs.jackson.datatype.jdk8)
    api(libs.jackson.datatype.jsr310)

    implementation(libs.apache.commons.io)
    implementation(libs.apache.commons.csv)
    implementation(libs.apache.commons.collections4)

    testImplementation(project(":testutils"))
}
