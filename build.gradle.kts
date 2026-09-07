buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.13.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.Kotlin.core}")
        classpath("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:2.0.21-1.0.28")
        classpath("com.google.dagger:hilt-android-gradle-plugin:${Versions.Dagger.core}")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:${Versions.AndroidX.Navigation.core}")
        classpath("com.google.gms:google-services:4.5.0")
        classpath("com.google.firebase:firebase-crashlytics-gradle:3.0.6")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

tasks.register("clean").configure {
    delete("build")
}
