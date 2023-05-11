import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.gradle.ext.settings
import org.jetbrains.gradle.ext.taskTriggers

val kspVersion: String by project
val kotlinInjectVersion: String by project
val kotlinJvmTarget: String by project
val kermitVersion: String by project

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("com.google.devtools.ksp")
    id("org.jetbrains.gradle.plugin.idea-ext")
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
            kotlinOptions.jvmTarget = kotlinJvmTarget
        }
        tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
            kotlinOptions {
                jvmTarget = kotlinJvmTarget
            }
        }
        withJava()
    }
    sourceSets {
        val jvmMain by getting {
            kotlin.srcDir("build/generated/ksp/jvm/jvmMain/kotlin")
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation("co.touchlab:kermit:${kermitVersion}")
                implementation("me.tatarka.inject:kotlin-inject-runtime:${kotlinInjectVersion}")
                implementation(project("markdown-editor"))
            }
        }
        val jvmTest by getting {
            kotlin.srcDir("build/generated/ksp/jvm/jvmTest/kotlin")
        }
    }
}

compose.desktop {
    application {
        mainClass = "me.okonecny.whodoes.MainKt"
        jvmArgs(
            "--add-exports=java.desktop/sun.java2d=ALL-UNNAMED", // For access to SunGraphicsEnvironment.
            "-Djava.net.useSystemProxies=true"
        )
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