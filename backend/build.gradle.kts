import org.jetbrains.intellij.platform.gradle.TestFrameworkType

dependencies {
    intellijPlatform {
        bundledModule("intellij.platform.backend")
        bundledModule("intellij.platform.kernel.backend")
        bundledModule("intellij.platform.rpc.backend")
        testFramework(TestFrameworkType.Platform)
    }
    implementation(project(":shared"))
    testImplementation("junit:junit:4.13.2")
}
