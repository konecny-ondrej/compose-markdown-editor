val kotlinInjectVersion: String by project
val kotlinJvmTarget: String by project
val kermitVersion: String by project
val ktorVersion: String by project
val flexmarkVersion: String by project

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
                implementation("co.touchlab:kermit:$kermitVersion") {
                    exclude("org.jetbrains.kotlin", "kotlin-test-junit")
                }
            }
        }
        val jvmMain by getting {
            kotlin.srcDir("build/generated/ksp/jvm/jvmMain/kotlin")
            dependencies {
                api(compose.desktop.currentOs)
                api(project(":interactive-text"))
                implementation("me.tatarka.inject:kotlin-inject-runtime:$kotlinInjectVersion")
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-cio:$ktorVersion")
                implementation("io.ktor:ktor-client-logging:$ktorVersion")
                implementation("com.vladsch.flexmark:flexmark-all:$flexmarkVersion")
            }
        }
        val jvmTest by getting {
            kotlin.srcDir("build/generated/ksp/jvm/jvmTest/kotlin")
            dependencies {
                implementation(kotlin("test-junit5"))
            }
            tasks.withType<Test> {
                useJUnitPlatform()
            }
        }
    }
}

dependencies {
    add("kspJvm", "me.tatarka.inject:kotlin-inject-compiler-ksp:${kotlinInjectVersion}")
    add("kspJvmTest", "me.tatarka.inject:kotlin-inject-compiler-ksp:${kotlinInjectVersion}")
}