plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.svwh.tools.noenvhook"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.svwh.tools.noenvhook.runtime"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = false
        }
    }

    buildFeatures {
        buildConfig = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.pine.core)
    compileOnly(libs.androidx.annotation)
}

listOf("debug", "release").forEach { variantName ->
    val capitalizedVariant = variantName.replaceFirstChar { it.uppercaseChar() }
    val apkDirectory = layout.buildDirectory.dir("intermediates/apk/$variantName")
    val outputDex = layout.buildDirectory.file("outputs/noenv-hook/stool-noenv-hook-$variantName.dex")

    tasks.register("packageNoEnvHookDex$capitalizedVariant") {
        group = "noenv hook"
        description = "Builds the no-env hook runtime and extracts classes.dex for $variantName."
        dependsOn("assemble$capitalizedVariant")
        outputs.file(outputDex)

        doLast {
            val apkFile = apkDirectory.get().asFile
                .listFiles { file -> file.extension == "apk" }
                ?.maxByOrNull { it.lastModified() }
                ?: error("No APK found in ${apkDirectory.get().asFile}")
            val dexFile = zipTree(apkFile)
                .matching { include("classes.dex") }
                .singleFile
            val targetFile = outputDex.get().asFile
            targetFile.parentFile.mkdirs()
            dexFile.copyTo(targetFile, overwrite = true)
        }
    }
}
