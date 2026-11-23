pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // The samples use the released SDK from Maven Central, as a customer app does.
        // -PsophiMavenLocal=true takes com.usesophi from mavenLocal first, to try a local SDK
        // publish before it is released.
        if (providers.gradleProperty("sophiMavenLocal").orNull == "true") {
            mavenLocal { content { includeGroup("com.usesophi") } }
        }
        google()
        mavenCentral()
    }
}

rootProject.name = "Sophi Android Sample"
include(":sample-compose")
include(":sample-xml")
