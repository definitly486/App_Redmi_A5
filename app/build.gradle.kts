import java.text.SimpleDateFormat
import java.util.Date

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}




android {
    namespace = "com.example.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.app"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        buildConfigField("long", "BUILD_TIME", "${System.currentTimeMillis()}L")

        val gitBranch = gitBranch()
        val gitCommitShort = gitCommitShort()
        val gitCommitFull = gitCommitFull()

        versionNameSuffix = "-$gitBranch"

        buildConfigField("String", "GIT_BRANCH", "\"$gitBranch\"")
        buildConfigField("String", "GIT_COMMIT_SHORT", "\"$gitCommitShort\"")
        buildConfigField("String", "GIT_COMMIT_FULL", "\"$gitCommitFull\"")
        buildConfigField("String", "VERSION_NAME_SUFFIX", "\"-$gitBranch\"")
        buildConfigField("String", "FULL_VERSION_NAME", "\"$versionName-$gitBranch\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isDebuggable = true
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            signingConfig = signingConfigs.getByName("debug")   // ← обязательно!
            applicationIdSuffix = null
            versionNameSuffix = null

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

android.applicationVariants.all {
    outputs.all {

        val date = getDate()
        val commit = gitCommitShort()
        val buildTypeName = buildType.name
        val version = versionName

        (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName =
            "app_redmi_a5_${buildTypeName}_v${version}_${date}_${commit}.apk"
    }
}

fun getDate(): String =
    SimpleDateFormat("yyyyMMdd_HHmm").format(Date())



// === Git-функции ===
private fun gitBranch() = runGitCommand("git rev-parse --abbrev-ref HEAD") ?: "unknown"
private fun gitCommitShort() = runGitCommand("git rev-parse --short HEAD") ?: "unknown"
private fun gitCommitFull() = runGitCommand("git rev-parse HEAD") ?: "unknown"

private fun runGitCommand(command: String): String? = try {
    providers.exec {
        commandLine(command.split(" "))
        workingDir = project.rootProject.projectDir
    }.standardOutput.asText.get().trim().takeIf { it.isNotEmpty() && it != "HEAD" } ?: "unknown"
} catch (e: Exception) {
    "unknown"
}

tasks.register("checkAssets") {
    doLast {
        val assetsDir = File("$projectDir/src/main/assets")
        val fileName = "KernelSU_v1.0.5_12081-release.apk"
        val file = File(assetsDir, fileName)

        if (!file.exists()) {
            throw GradleException("ERROR: Missing asset file: $fileName\nПоложи его в src/main/assets/")
        }
    }
}

tasks.named("preBuild").configure {
    dependsOn("checkAssets")
}



// ←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←
// ВОТ ЭТОТ БЛОК ОБЯЗАТЕЛЬНО ДОЛЖЕН БЫТЬ В КОНЦЕ ФАЙЛА!
dependencies {
    implementation("org.bouncycastle:bcprov-jdk18on:1.78.1")
    implementation("org.bouncycastle:bcpg-jdk18on:1.78.1")
    implementation("org.bouncycastle:bcpkix-jdk18on:1.78.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("com.google.android.material:material:1.11.0")

    // Если используешь Version Catalog (libs.xxx) — оставь так:
    implementation(libs.material)
    implementation(libs.org.eclipse.jgit)
    implementation(libs.androidx.viewpager2)
    implementation(libs.commons.compress)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.xz)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.runtime.saved.instance.state)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
// ←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←
