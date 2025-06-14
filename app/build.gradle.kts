plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.jetbrains.kotlin.android)
	alias(libs.plugins.android.ksp)
//  alias(libs.plugins.firebase)
}

android {
	namespace = "boilerplate"
	compileSdk = 36
	applicationVariants.all {
		resValue("string", "versionName", versionName)
	}
	defaultConfig {
		applicationId = "android.boilerplate"
		minSdk = 24
		targetSdk = 36
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
			signingConfig = signingConfigs.getByName("debug")
		}
		debug {
			isDebuggable = true
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

dependencies {
	implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))

	implementation(projects.compactShape)
	implementation(projects.refreshRecyclerView)
	implementation(projects.simpleRoundedImage)
	implementation(projects.simpleCalendar)

	implementation(libs.androidx.activity)
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

//    implementation(platform(libs.firebase.bom))
//    implementation(libs.firebase.crashlytics)
//    implementation(libs.firebase.database)
//    implementation(libs.firebase.messaging)
//    implementation(libs.firebase.analytics)
}

apply {
	from("../autodimension.gradle")
}