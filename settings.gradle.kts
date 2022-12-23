pluginManagement {
    val kotlinVersion: String by settings
    val kspVersion: String by settings
    val composeVersion: String by settings
    val ideaExtVersion: String by settings
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }

    plugins {
        id("com.google.devtools.ksp") version kspVersion apply false
        kotlin("multiplatform") version kotlinVersion apply false
        id("org.jetbrains.compose") version composeVersion apply false
        id("org.jetbrains.gradle.plugin.idea-ext") version ideaExtVersion apply false
    }
}

rootProject.name = "whodoes"

include(":markdown-editor")