import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.3.20"
    id("org.jetbrains.intellij.platform") version "2.19.0"
}

group = "dev.derwa"
version = "0.1.0"

kotlin {
    jvmToolchain(21)
    compilerOptions {
        // Inherit the platform interfaces' default methods instead of generating bridges that override them.
        jvmDefault = JvmDefaultMode.NO_COMPATIBILITY
    }
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        // Build against the oldest supported release so sinceBuild 261 holds by construction.
        webstorm("2026.1.5")
        bundledPlugin("org.jetbrains.plugins.terminal")
        bundledModule("intellij.terminal.frontend")
        testFramework(TestFrameworkType.Platform)
    }
    testImplementation("junit:junit:4.13.2")
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "261"
            untilBuild = provider { null }
        }
    }

    pluginVerification {
        ides {
            create(IntelliJPlatformType.WebStorm, "2026.1.5")
            create(IntelliJPlatformType.WebStorm, "2026.2.3")
            create(IntelliJPlatformType.PhpStorm, "2026.1.5")
            create(IntelliJPlatformType.PhpStorm, "2026.2.3")
        }
    }
}

intellijPlatformTesting {
    runIde {
        register("runPhpStorm") {
            type = IntelliJPlatformType.PhpStorm
            version = "2026.2.3"
        }
    }
}
