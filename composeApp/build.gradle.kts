import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.room)
    alias(libs.plugins.ksp)
}

val versionProps = Properties().apply {
    load(File(rootDir, "version.properties").inputStream())
}
val customVersionName = versionProps.getProperty("VERSION_NAME") ?: "1.0.0"

// Supabase secrets live in local.properties (git-ignored). Base URL is public; the anon
// key is safe to ship in the client (RLS enforces access) but we still keep it out of git.
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
val supabaseUrl: String = localProps.getProperty("supabase.url") ?: ""
val supabaseAnonKey: String = localProps.getProperty("supabase.anonKey") ?: ""

kotlin {
    compilerOptions {
        // Room + our platform shims use expect/actual classes (still flagged Beta).
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            linkerOpts.add("-lsqlite3")
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.ktor.client.android)
            implementation(libs.ktor.client.okhttp)
        }
        nativeMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.navigation.compose)
            // Kotlinx
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.okio)
            // Ktor (raw client)
            implementation(libs.bundles.ktor)
            // Image loading
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
            // Koin
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            // DataStore (tokens + settings)
            implementation(libs.datastore)
            implementation(libs.datastore.preferences)
            // Room (local cart)
            implementation(libs.room.runtime)
            implementation(libs.sqlite.bundled)
            // Lottie splash + QR receipt
            implementation(libs.compottie)
            implementation(libs.qrose)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

android {
    namespace = "com.shopverse.cmp"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.shopverse.cmp"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionName = customVersionName
        versionCode = getVersionCode()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    signingConfigs {
        create("release") {
            storeFile = file(localProps.getProperty("store"))
            storePassword = localProps.getProperty("storePass")
            keyAlias = localProps.getProperty("keyAlias")
            keyPassword = localProps.getProperty("keyPass")
        }
    }
    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.getByName("release")
        }
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
    }
    buildFeatures {
        buildConfig = true
    }
    lint {
        // androidx.lifecycle's NullSafeMutableLiveData detector crashes under the
        // Kotlin 2.x analysis API (KaCallableMemberCall "class vs interface" error),
        // which kills lintVitalAnalyzeRelease and with it the whole release build.
        disable += "NullSafeMutableLiveData"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

// Supabase config surfaced to commonMain via BuildKonfig-style generated constants.
// We keep it simple: BuildConfig on Android, and a generated Kotlin file for all targets.
val generateSupabaseConfig by tasks.registering {
    val outputDir = layout.buildDirectory.dir("generated/supabase/kotlin")
    outputs.dir(outputDir)
    val url = supabaseUrl
    val key = supabaseAnonKey
    doLast {
        val pkgDir = outputDir.get().dir("com/shopverse/cmp/network/service").asFile
        pkgDir.mkdirs()
        File(pkgDir, "SupabaseSecrets.kt").writeText(
            """
            package com.shopverse.cmp.network.service

            // GENERATED — do not edit. Values come from local.properties at build time.
            object SupabaseSecrets {
                const val URL: String = "$url"
                const val ANON_KEY: String = "$key"
            }
            """.trimIndent() + "\n"
        )
    }
}

kotlin.sourceSets.commonMain {
    kotlin.srcDir(generateSupabaseConfig)
}

/**
 * Ported from the Android app's getVersionCode(): every build bumps VERSION_CODE in
 * version.properties and returns the new value (so each build gets a fresh build number).
 * Unlike Android this also mirrors the number into the iOS xcconfig — CURRENT_PROJECT_VERSION
 * feeds CFBundleVersion, which is what Platform.native.kt reports as buildNumber — keeping the
 * two apps on one shared counter. Runs at configuration time, so IDE syncs count as builds
 * too (same behavior as the Android repo).
 */
fun getVersionCode(): Int {
    val versionFile = File(rootDir, "version.properties")
    if (!versionFile.exists()) return 1
    val props = Properties().apply { versionFile.inputStream().use { load(it) } }
    val oldCode = props.getProperty("VERSION_CODE")?.toIntOrNull() ?: 1
    val newCode = oldCode + 1
    props.setProperty("VERSION_CODE", newCode.toString())
    versionFile.writer().use { props.store(it, null) }
    syncIosBuildNumber(newCode)
    return newCode
}

/** Rewrites CURRENT_PROJECT_VERSION in iosApp/Configuration/Config.xcconfig. Xcode reads the
 *  xcconfig at build start (before the Kotlin framework script phase), so the iOS app picks a
 *  bumped number up on its next build. */
fun syncIosBuildNumber(code: Int) {
    val xcconfig = File(rootDir, "iosApp/Configuration/Config.xcconfig")
    if (!xcconfig.exists()) return
    val lines = xcconfig.readLines().map { line ->
        if (line.trimStart().startsWith("CURRENT_PROJECT_VERSION")) {
            "CURRENT_PROJECT_VERSION=$code"
        } else {
            line
        }
    }
    xcconfig.writeText(lines.joinToString("\n") + "\n")
}

dependencies {
    ksp(libs.room.compiler)
}

room {
    schemaDirectory("$projectDir/schemas")
}
