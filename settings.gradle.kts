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
        google()
        mavenLocal()
        mavenCentral()
    }
}

rootProject.name = "Sophi Android Sample"
include(":sample-compose")
include(":sample-xml")

// Build the SDK from the sibling source checkout. This is REQUIRED for now: the demo uses
// Sophi.configure / SophiEnvironment / the awaited onAddToCart(items), which the published
// com.usesophi:sophi-android:0.0.1-alpha01 does not have. Clone Sophi-Android next to this repo.
// Once a release carrying that API is published, the block can go and the catalog version applies.
includeBuild("../Sophi-Android") {
    dependencySubstitution {
        substitute(module("com.usesophi:sophi-android")).using(project(":sophi"))
    }
}
