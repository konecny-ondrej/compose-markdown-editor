import org.jetbrains.dokka.gradle.DokkaTask

val kotlinInjectVersion: String by project
val kotlinJvmTarget: String by project
val kermitVersion: String by project
val ktorVersion: String by project
val flexmarkVersion: String by project
val jewelVersion: String by project

plugins {
    `maven-publish`
    signing
    kotlin("multiplatform")
    id("org.jetbrains.dokka")
    id("org.jetbrains.compose")
    id("com.google.devtools.ksp")
    id("org.jetbrains.gradle.plugin.idea-ext")
}

group = "me.okonecny"
version = "0.1"

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
                implementation("org.jetbrains.jewel:jewel-int-ui-standalone:${jewelVersion}")
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

signing {
    useGpgCmd()
}

publishing.publications.withType<MavenPublication> {
    pom {
        name = "Compose Markdown Editor"
        description = "Markdown WYSIWYG editor component for Compose Multiplatform."
        url = "https://github.com/konecny-ondrej/compose-markdown-editor"
        licenses {
            license {
                name = "MIT License"
                url = "https://opensource.org/license/mit"
            }
        }
        developers {
            developer {
                name = "Ondřej Konečný"
                url = "https://github.com/konecny-ondrej"
                email = "konecny.ondrej@gmail.com"
            }
        }
        scm {
            connection = "scm:git:git://github.com:konecny-ondrej/compose-markdown-editor.git"
            developerConnection = "scm:git:ssh://github.com:konecny-ondrej/compose-markdown-editor.git"
            url = "https://github.com/konecny-ondrej/compose-markdown-editor/tree/main"
        }
        issueManagement {
            url = "https://github.com/konecny-ondrej/compose-markdown-editor/issues"
        }
    }
    // https://central.sonatype.org/publish/requirements/
}

tasks.named<Jar>("jvmJavadocJar") {
    dependsOn(tasks.dokkaHtml)
    from(tasks.dokkaHtml.flatMap(DokkaTask::outputDirectory))
}

tasks.named<Jar>("kotlinMultiplatformJavadocJar") {
    dependsOn(tasks.dokkaHtml)
    from(tasks.dokkaHtml.flatMap(DokkaTask::outputDirectory))
}