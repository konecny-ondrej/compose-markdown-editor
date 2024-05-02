pluginManagement {
    val kotlinVersion: String by settings
    val kspVersion: String by settings
    val composeVersion: String by settings
    val ideaExtVersion: String by settings
    val centralPortalPublisherVersion: String by settings
    val dokkaVersion: String by settings
    val gradleKeyringVersion: String by settings

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
        id("com.tddworks.central-portal-publisher") version centralPortalPublisherVersion apply false
        id("org.jetbrains.dokka") version dokkaVersion apply false
        id("me.okonecny.gradle-keyring") version gradleKeyringVersion apply false
    }
}

rootProject.name = "compose-markdown-editor"

include(":interactive-text")
include(":markdown-editor")