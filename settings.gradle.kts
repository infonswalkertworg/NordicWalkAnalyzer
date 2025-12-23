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
        maven(url = "https://jitpack.io")
    }
}

rootProject.name = "NordicWalkAnalyzer"
include(":app")
include(":core:data")
include(":core:domain")
include(":core:ui")
include(":feature:student-management")
include(":feature:video-analysis")
include(":feature:pose-engine")
include(":feature:reporting")