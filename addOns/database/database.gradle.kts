import org.rm3l.datanucleus.gradle.DataNucleusApi
import org.rm3l.datanucleus.gradle.extensions.enhance.EnhanceExtension
import org.zaproxy.gradle.addon.AddOnStatus

group = "org.zaproxy.addon"

description = "Provides data persistence capabilities."

zapAddOn {
    addOnName.set("Database")
    addOnStatus.set(AddOnStatus.ALPHA)
    zapVersion.set("2.11.1")

    manifest {
        author.set("ZAP Dev Team")
        url.set("https://www.zaproxy.org/docs/desktop/addons/database/")

        helpSet {
            baseName.set("help%LC%.helpset")
            localeToken.set("%LC%")
        }
    }

    apiClientGen {
        api.set("org.zaproxy.addon.database.DatabaseApi")
        messages.set(file("src/main/resources/org/zaproxy/addon/database/resources/Messages.properties"))
    }
}

crowdin {
    configuration {
        tokens.put("%messagesPath%", "org/zaproxy/addon/${zapAddOn.addOnId.get()}/resources/")
        tokens.put("%helpPath%", "")
    }
}

dependencies {
    implementation("org.datanucleus:datanucleus-accessplatform-jdo-rdbms:5.2.11")
    testImplementation(project(":testutils"))
}

datanucleus {
    enhance(closureOf<EnhanceExtension> {
        api(DataNucleusApi.JDO)
        persistenceUnitName("ZapDatabase")
    })
}
