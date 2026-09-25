dependencies {
    intellijPlatform {
        bundledModule("intellij.platform.frontend")
        bundledPlugin("org.jetbrains.plugins.terminal")
        bundledModule("intellij.terminal.frontend")
    }
    implementation(project(":shared"))
}
