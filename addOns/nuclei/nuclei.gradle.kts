description = "Allows you to run nuclei scripts within ZAP."

zapAddOn {
    addOnName.set("Nuclei")
    zapVersion.set("2.11.0")

    manifest {
        author.set("ZAP Dev Team")
        url.set("https://www.zaproxy.org/docs/desktop/addons/nuclei/")

        dependencies {
            addOns {
                register("scripts")
            }
        }

//        extensions {
//            register("org.zaproxy.addon.nuclei.scripts.ExtensionNucleiScripts") {
//                classnames {
//                    allowed.set(listOf("org.zaproxy.addon.nuclei.scripts"))
//                }
//                dependencies {
//                    addOns {
//                        register("scripts")
//                        register("graaljs")
//                    }
//                }
//            }
//        }
    }

//    apiClientGen {
//        api.set("org.zaproxy.addon.nuclei.NucleiApi")
//        messages.set(file("src/main/resources/org/zaproxy/addon/nuclei/resources/Messages.properties"))
//    }
}

crowdin {
    configuration {
        val resourcesPath = "org/zaproxy/addon/${zapAddOn.addOnId.get()}/resources/"
        tokens.put("%messagesPath%", resourcesPath)
        tokens.put("%helpPath%", resourcesPath)
    }
}

dependencies {
//    compileOnly(parent!!.childProjects["graaljs"]!!)

    testImplementation(project(":testutils"))
}
