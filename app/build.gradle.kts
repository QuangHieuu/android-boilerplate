plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.jetbrains.kotlin.kapt)
}

android {
    namespace = "boilerplate"
    compileSdk = 34
    applicationVariants.all {
        resValue("string", "versionName", versionName)
    }
    defaultConfig {
        applicationId = "android.boilerplate"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isDebuggable = false
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isDebuggable = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(fileTree("dir" to "libs", "include" to listOf("*.jar", "*.aar")))

    implementation(files("libs/jsoup-1.16.1.jar"))
    implementation(files("libs/signalr-client.aar"))

    implementation(project(":permission"))
    implementation(project(":excel-to-pdf"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.multidex)
    implementation(libs.androidx.runtime)
    implementation(libs.androidx.startup)
    implementation(libs.androidx.window)
    implementation(libs.androidx.splash)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle)

    implementation(libs.material)
    implementation(libs.recyclerview)

    implementation(libs.retrofit)
    implementation(libs.retrofit.rxjava)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.gson)

    implementation(platform(libs.okhttp.bom))
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging)

    implementation(libs.koin)
    implementation(libs.rxjava)
    implementation(libs.rxjava.android)
    implementation(libs.glide)

}

apply {
    from("../autodimension.gradle")
}
