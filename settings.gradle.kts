pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "wso"

buildscript {
    repositories.addAll(settings.pluginManagement.repositories)
}

@Suppress("UnstableApiUsage")
settings.dependencyResolutionManagement {
    repositories.addAll(settings.pluginManagement.repositories)
}

settings.gradle.allprojects {
    repositories.addAll(settings.pluginManagement.repositories)
}

include("heartbeat")
include("stateless")
include("manager")
include("loadbalancer")
