import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.gradle.ext.settings
import org.jetbrains.gradle.ext.taskTriggers

plugins {
    kotlin("multiplatform") version libs.versions.kotlinVersion
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.jetbrains.ideaExt)
    alias(libs.plugins.centralPortalPublisher)
    alias(libs.plugins.keyring)
}

group = "me.okonecny"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://packages.jetbrains.team/maven/p/kpm/public/")
}

keyring {
    secret("maven_central_token_user")
    secret("maven_central_token_password")
}

kotlin {
    jvm {
        withJava()
    }
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(libs.versions.javaTargetVersion.get())
        vendor = JvmVendorSpec.JETBRAINS
    }
    sourceSets {
        val jvmMain by getting {
            kotlin.srcDir("build/generated/ksp/jvm/jvmMain/kotlin")
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kermit)
                implementation(libs.kotlin.inject.runtime)
                implementation(libs.jetbrains.jewel.ui)
                implementation(libs.jetbrains.jewel.decoratedWindow)
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
        mainClass = "me.okonecny.markdowneditor.demo.MainKt"
        jvmArgs(
            "--add-exports=java.desktop/sun.java2d=ALL-UNNAMED", // For access to SunGraphicsEnvironment.
            "-Djava.net.useSystemProxies=true"
        )
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "markdowneditordemo"
            packageVersion = "1.0.0"
        }
    }
}

dependencies {
    add("kspJvm", libs.kotlin.inject.compiler)
    add("kspJvmTest", libs.kotlin.inject.compiler)
}

idea.project.settings.taskTriggers {
    afterSync("kspKotlinJvm", "kspTestKotlinJvm")
}

sonatypePortalPublisher {
    authentication {
        username = keyring.secrets["maven_central_token_user"]
        password = keyring.secrets["maven_central_token_password"]
    }

    settings {
        autoPublish = false
        aggregation = true
    }
}

val patchReadme: Task by tasks.creating {
    val readmeFile = File("README.md")

    inputs.files(readmeFile)
    outputs.files(readmeFile)
    subprojects.forEach { proj ->
        inputs.property(proj.path, proj.version)
    }

    doLast {
        var readmeContents = readmeFile.readText()
        subprojects.forEach { proj ->
            readmeContents = readmeContents.replace(
                Regex("${proj.group}:${proj.name}:\\d+\\.\\d+\\.\\d+"),
                "${proj.group}:${proj.name}:${proj.version}"
            )
        }
        readmeFile.writeText(readmeContents)
    }
}

tasks.build.configure {
    dependsOn(patchReadme)
}