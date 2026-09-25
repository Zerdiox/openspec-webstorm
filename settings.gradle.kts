import org.jetbrains.intellij.platform.gradle.extensions.intellijPlatform

rootProject.name = "openspec-webstorm"

pluginManagement {
    repositories {
        gradlePluginPortal()
        // JetBrains' rpc compiler plugin is published only here.
        maven("https://packages.jetbrains.team/maven/p/ij/intellij-dependencies/")
    }
    plugins {
        id("org.jetbrains.kotlin.jvm") version "2.3.20"
        id("org.jetbrains.kotlin.plugin.serialization") version "2.3.20"
        // The version the split-mode docs pair with 2026.1 and Kotlin 2.3.20; it moves with Kotlin.
        id("rpc") version "2.3.20-RC2-0.1"
    }
}

plugins {
    id("org.jetbrains.intellij.platform.settings") version "2.19.0"
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        intellijPlatform {
            defaultRepositories()
        }
    }
}

include("shared", "backend", "frontend")
