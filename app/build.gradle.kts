plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.android.ksp)
    alias(libs.plugins.firebase)
}

android {
    namespace = "boilerplate"
    compileSdk = 34
    applicationVariants.all {
        resValue("string", "versionName", versionName)
    }
    defaultConfig {
        applicationId = "com.greenglobal.eoffice"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        release {
            isDebuggable = false
            isMinifyEnabled = true
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
    implementation(platform(libs.kotlin.bom))

    implementation(fileTree("dir" to "libs", "include" to listOf("*.jar", "*.aar")))

    implementation(files("libs/jsoup-1.16.1.jar"))

    implementation(project(":excel-to-pdf"))
    implementation(project(":signalr-client-sdk"))

    implementation(libs.androidx.annotation)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.multidex)
    implementation(libs.androidx.runtime)
    implementation(libs.androidx.startup)
    implementation(libs.androidx.window)
    implementation(libs.androidx.splash)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle)
    implementation(libs.androidx.swiperefreshlayout)

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
    implementation(libs.glide.integration)
    ksp(libs.glide.compiler)

    implementation(libs.swipelayout)

    implementation(platform(libs.firebase.bom))
//    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.database)
    implementation(libs.firebase.messaging)
//    implementation(libs.firebase.analytics)

    implementation(libs.autoimageslider)
}

apply {
    from("../autodimension.gradle")
}
