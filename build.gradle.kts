import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.gradle.ext.settings
import org.jetbrains.gradle.ext.taskTriggers

val kspVersion: String by project
val kotlinInjectVersion: String by project

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("com.google.devtools.ksp")
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.7"
}

group = "me.okonecny"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
        withJava()
    }
    sourceSets {
        val jvmMain by getting {
            kotlin.srcDir("build/generated/ksp/jvm/jvmMain/kotlin")
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation("me.tatarka.inject:kotlin-inject-runtime:${kotlinInjectVersion}")
            }
        }
        val jvmTest by getting {
            kotlin.srcDir("build/generated/ksp/jvm/jvmTest/kotlin")
        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "whodoes"
            packageVersion = "1.0.0"
        }
    }
}

dependencies {
    add("kspJvm", "me.tatarka.inject:kotlin-inject-compiler-ksp:${kotlinInjectVersion}")
    add("kspJvmTest", "me.tatarka.inject:kotlin-inject-compiler-ksp:${kotlinInjectVersion}")
}

idea.project.settings.taskTriggers {
    afterSync("kspKotlinJvm", "kspTestKotlinJvm")
}