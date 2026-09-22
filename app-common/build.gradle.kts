import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    // KSP needs KGP, so AGP's built-in Kotlin is off (android.builtInKotlin=false).
    id("com.android.library")
    kotlin("android")
    id("com.google.devtools.ksp")
    id("kotlin-parcelize")
}
apply(plugin = "dagger.hilt.android.plugin")

android {
    compileSdk = ProjectConfig.compileSdk
    namespace = "${ProjectConfig.packageName}.common"

    defaultConfig {
        minSdk = ProjectConfig.minSdk

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        buildConfigField("Long", "VERSION_CODE", "${ProjectConfig.versionFrom(rootProject.file("version.properties")).code}L")
        buildConfigField("String", "VERSION_NAME", "\"${ProjectConfig.versionFrom(rootProject.file("version.properties")).name}\"")
        buildConfigField("String", "APPLICATION_ID", "\"${ProjectConfig.packageName}\"")
        buildConfigField("String", "GITSHA", "\"${lastCommitHash()}\"")
        buildConfigField("String", "BUILDTIME", "\"${buildTime()}\"")
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    flavorDimensions.add("version")
    productFlavors {
        create("foss") {
            dimension = "version"
        }
        create("gplay") {
            dimension = "version"
        }
    }

    buildTypes {
        val customProguardRules = fileTree(File("../proguard")) {
            include("*.pro")
        }
        debug {
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            proguardFiles(*customProguardRules.toList().toTypedArray())
            proguardFiles("proguard-rules-debug.pro")
        }
        create("beta") {
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            proguardFiles(*customProguardRules.toList().toTypedArray())
        }
        release {
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            proguardFiles(*customProguardRules.toList().toTypedArray())
        }
    }

//    testOptions {
//        unitTests {
//            isIncludeAndroidResources = true
//        }
//        tasks.withType<Test> {
//            useJUnitPlatform()
//        }
//    }
}

// AGP 9 dropped the android.kotlinOptions DSL, these live on the Kotlin extension now.
kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        freeCompilerArgs.addAll(
            "-opt-in=kotlin.ExperimentalStdlibApi",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=kotlin.time.ExperimentalTime",
            "-opt-in=kotlin.ExperimentalUnsignedTypes",
        )
    }
}

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")

    addBaseAndroid()
    addBaseAndroidUi()
    addBaseKotlin()
    addDagger()
    addMoshi()
    addBaseWorkManager()
    addNavigation()

    addTesting()

    add("gplayImplementation", platform("com.google.firebase:firebase-bom:34.18.0"))
    add("gplayImplementation", "com.google.firebase:firebase-crashlytics")
}