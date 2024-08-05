import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    `maven-publish`
    signing
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.jetbrains.dokka)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.jetbrains.ideaExt)
}

group = "me.okonecny"
version = libs.versions.markdownEditorVersion.get()

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://packages.jetbrains.team/maven/p/kpm/public/")
}

kotlin {
    jvm()
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(libs.versions.javaTargetVersion.get())
        vendor = JvmVendorSpec.JETBRAINS
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kermit)
            }
        }
        val jvmMain by getting {
            kotlin.srcDir("build/generated/ksp/jvm/jvmMain/kotlin")
            dependencies {
                api(compose.desktop.currentOs)
                api(project(":interactive-text"))
                implementation(libs.kotlin.inject.runtime)
                implementation(libs.jetbrains.jewel.ui)
                implementation(libs.bundles.ktor.client)
                implementation(libs.flexmark)
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
    add("kspJvm", libs.kotlin.inject.compiler)
    add("kspJvmTest", libs.kotlin.inject.compiler)
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