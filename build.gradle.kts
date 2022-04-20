import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.20"
    id("org.jetbrains.compose") version "1.1.1"
    id("org.openjfx.javafxplugin") version "0.0.10"
}

// DO NOT CHANGE IT!
// THIS UUID IS USED FOR UPGRADING FEATURE AND SHOULD REMAIN CONSTANT
// MORE HERE: https://github.com/JetBrains/compose-jb/tree/master/tutorials/Native_distributions_and_local_execution#platform-specific-options
val DO_NOT_CHANGE_THIS_UUID = "71454f6a-55e9-44d8-830b-59ca8fc9f418"

group = "me.darefox"

// WHEN CHANGING VERSION, DON'T FORGET TO CHANGE VERSION
// IN UI COMPOSABLE
version = "1.0.0"

repositories {
    google()
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

val ktorVersion = "1.6.8"

dependencies {
    implementation(compose.desktop.currentOs)

    // ICONS
    implementation("br.com.devsrsouza.compose.icons.jetbrains:feather:1.0.0")

    // Kotlin Serialization. For caching
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")

    // Http-client Ktor. For downloading media from servers
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-serialization:$ktorVersion")

    // API for cmtt websites
    implementation("com.github.DareFox:kmttAPI:7a96f33161")

    // HTML Parser
    implementation("org.jsoup:jsoup:1.14.3")

    // Function rate limiter
    implementation("io.github.resilience4j:resilience4j-ratelimiter:1.7.1")
    implementation("io.github.resilience4j:resilience4j-kotlin:1.7.1")

    // Logger
    implementation("io.github.microutils:kotlin-logging-jvm:2.1.21")
    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("ch.qos.logback:logback-classic:1.2.11")
    implementation("ch.qos.logback:logback-core:1.2.11")

}

javafx {
    version = "17.0.1"
    modules = listOf("javafx.swing")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

val iconsRoot = project.file("src/main/resources/img")

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)

            packageName = "SaveDTF"
            packageVersion = version.toString()
            description = "SaveDTF"
            vendor = "DareFox"

            windows {
                iconFile.set(iconsRoot.resolve("DTF_logo.ico"))
                dirChooser = true
                upgradeUuid = DO_NOT_CHANGE_THIS_UUID;
                menuGroup = "Programs" // Make it searchable in Start menu
            }
            linux {
                iconFile.set(iconsRoot.resolve("DTF_logo.png"))
            }
            macOS {
                iconFile.set(iconsRoot.resolve("DTF_logo.icns"))
            }

            modules("java.instrument",
                "java.management",
                "java.naming",
                "java.prefs",
                "jdk.jfr",
                "jdk.unsupported",
                "jdk.unsupported.desktop")
        }

    }
}
