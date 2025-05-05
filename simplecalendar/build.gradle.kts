plugins {
	id("com.android.library")
	id("org.jetbrains.kotlin.android")
}

android {
	namespace = "android.empty.calendar"
	compileSdk = 35
	defaultConfig {
		minSdk = 24
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
	}
}

//noinspection UseTomlInstead
dependencies {
	implementation("androidx.appcompat:appcompat:1.7.0")
	implementation("androidx.core:core-ktx:1.15.0")
}