import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.tasks.aware.SplitModeAware
import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.intellij.platform")
    id("org.jetbrains.kotlin.plugin.serialization") apply false
    id("rpc") apply false
}

group = "dev.derwa"
version = "0.1.0"

allprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    configure<KotlinJvmProjectExtension> {
        jvmToolchain(21)
        compilerOptions {
            // Inherit the platform interfaces' default methods instead of generating bridges that override them.
            jvmDefault = JvmDefaultMode.NO_COMPATIBILITY
        }
    }
}

// Each subproject is one content module of the plugin (see plugin.xml).
subprojects {
    apply(plugin = "org.jetbrains.intellij.platform.module")
    // The modules talk to each other over the platform's RPC: @Serializable types and @Rpc interfaces.
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
    apply(plugin = "rpc")

    dependencies {
        // Bundled with the IDE.
        "compileOnly"("org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:1.9.0")
    }

    // The IDE finds a content module by its name: dev.derwa.openspec.backend lives in lib/modules/dev.derwa.openspec.backend.jar.
    tasks.named<org.gradle.jvm.tasks.Jar>("composedJar") {
        archiveBaseName = "dev.derwa.openspec.${project.name}"
    }
}

dependencies {
    intellijPlatform {
        // Build against the oldest supported release so sinceBuild 261 holds by construction.
        webstorm("2026.1.5")
        pluginModule(implementation(project(":shared")))
        pluginModule(implementation(project(":backend")))
        pluginModule(implementation(project(":frontend")))
    }
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
        // A backend and a client, the way remote development runs the plugin.
        register("runSplitMode") {
            splitMode = true
            pluginInstallationTarget = SplitModeAware.PluginInstallationTarget.BOTH
        }
    }
}
