val kotlinInjectVersion: String by project
val kotlinJvmTarget: String by project
val kermitVersion: String by project
val jewelVersion: String by project

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("com.google.devtools.ksp")
    id("org.jetbrains.gradle.plugin.idea-ext")
}

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://packages.jetbrains.team/maven/p/kpm/public/")
}

kotlin {
    jvm {
        withJava()
    }
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(kotlinJvmTarget)
        vendor = JvmVendorSpec.JETBRAINS
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("co.touchlab:kermit:${kermitVersion}") {
                    exclude("org.jetbrains.kotlin", "kotlin-test-junit")
                }
            }
        }
        val jvmMain by getting {
            dependencies {
                api(compose.desktop.currentOs)
                implementation("org.jetbrains.jewel:jewel-int-ui-standalone:${jewelVersion}")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit5"))
            }
            tasks.withType<Test> {
                useJUnitPlatform()
            }
        }
    }
}
