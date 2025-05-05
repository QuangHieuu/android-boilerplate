plugins {
	id("com.android.library")
	id("org.jetbrains.kotlin.android")
	id("com.google.devtools.ksp")
}

android {
	namespace = "android.empty.customview"

	defaultConfig {
		minSdk = 24
		compileSdk = 35
	}

	buildTypes {
		release {
			isMinifyEnabled = false
		}
		debug {
			isDefault = true
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

//noinspection UseTomlInstead
dependencies {
	implementation("androidx.appcompat:appcompat:1.7.0")
	implementation("androidx.core:core-ktx:1.16.0")

}