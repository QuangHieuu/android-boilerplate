// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.jetbrains.kotlin.android) apply false
  alias(libs.plugins.android.ksp) apply false

  alias(libs.plugins.firebase) apply false
}
buildscript {
  extra.apply {
    set("minSdkVersion", 24)
    set("compileSdkVersion", 34)
    set("targetSdkVersion", get("compileSdkVersion"))
    set("rxJava", "io.reactivex.rxjava3:rxjava:3.1.5")
    set("androidXFragment", "androidx.fragment:fragment:1.6.2")
    set("androidXAnnotation", "androidx.annotation:annotation:1.6.0")
    set("androidXAppcompat", "androidx.appcompat:appcompat:1.6.1")
  }
}