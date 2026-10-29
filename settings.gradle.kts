pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "CallGuard"
include(":app")
include(":core")

// Build outputs live outside the OneDrive-synced project folder: OneDrive's
// sync agent locks files under build/ while they're being written, which
// causes intermittent "Unable to delete directory" failures on Windows.
// Must stay on the SAME drive as the project - some Kotlin/KSP codegen paths
// call File.relativeTo() between the build dir and the project dir, which
// throws on Windows when the two paths are on different drive letters.
val localBuildRoot = java.io.File("G:/tmp/gradle-builds/CallGuard")
gradle.beforeProject {
    layout.buildDirectory.set(java.io.File(localBuildRoot, project.path.removePrefix(":").ifEmpty { "_root" }))
}

