val kotlinInjectVersion: String by project
val kotlinJvmTarget: String by project
val kermitVersion: String by project

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
        val commonMain by getting {
            dependencies {
                implementation("co.touchlab:kermit:${kermitVersion}")
            }
        }
        val jvmMain by getting {
            kotlin.srcDir("build/generated/ksp/jvm/jvmMain/kotlin")
            dependencies {
                api(compose.desktop.currentOs)
                implementation("me.tatarka.inject:kotlin-inject-runtime:${kotlinInjectVersion}")
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