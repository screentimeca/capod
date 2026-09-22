import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    // KSP needs KGP, so AGP's built-in Kotlin is off (android.builtInKotlin=false).
    id("com.android.application")
    kotlin("android")
    id("com.google.devtools.ksp")
    id("kotlin-parcelize")
}
apply(plugin = "dagger.hilt.android.plugin")
apply(plugin = "androidx.navigation.safeargs.kotlin")

android {
    compileSdk = ProjectConfig.compileSdk
    namespace = "${ProjectConfig.packageName}"

    defaultConfig {
        applicationId = ProjectConfig.packageName

        minSdk = ProjectConfig.minSdk
        targetSdk = ProjectConfig.targetSdk

        val appVersion = ProjectConfig.versionFrom(rootProject.file("version.properties"))
        versionCode = appVersion.code
        versionName = appVersion.name
        println("AirPod gplay versionName=${appVersion.name} versionCode=${appVersion.code}")

        testInstrumentationRunner = "com.screentime.airpod.HiltTestRunner"
    }

    signingConfigs {
        val basePath = File(System.getProperty("user.home"), ".appconfig/${ProjectConfig.packageName}")
        create("releaseFoss") {
            setupCredentials(File(basePath, "signing-foss.properties"))
        }
        create("releaseGplay") {
            setupCredentials(File(basePath, "signing-gplay-upload.properties"))
        }
    }

    flavorDimensions.add("version")
    productFlavors {
        create("foss") {
            dimension = "version"
            signingConfig = signingConfigs["releaseFoss"]
        }
        create("gplay") {
            dimension = "version"
            signingConfig = signingConfigs["releaseGplay"]
        }
    }

    buildTypes {
        val customProguardRules = fileTree(File(projectDir, "proguard")) {
            include("*.pro")
        }
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            proguardFiles(*customProguardRules.toList().toTypedArray())
            proguardFiles("proguard-rules-debug.pro")
        }
        create("beta") {
            lint {
                abortOnError = true
                fatal.add("StopShip")
            }
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            proguardFiles(*customProguardRules.toList().toTypedArray())
        }
        release {
            lint {
                abortOnError = true
                fatal.add("StopShip")
            }
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            proguardFiles(*customProguardRules.toList().toTypedArray())
        }
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

//    testOptions {
//        unitTests {
//            isIncludeAndroidResources = true
//        }
//        tasks.withType<Test> {
//            useJUnitPlatform()
//        }
//    }
}

androidComponents {
    // The dependency info block is encrypted and can only be read by Google, so keep it out of FOSS builds.
    // Since AGP 9 this is no longer configurable per product flavor in the DSL.
    beforeVariants(selector().withFlavor("version" to "foss")) { variantBuilder ->
        variantBuilder.dependenciesInfo.includeInApk = false
        variantBuilder.dependenciesInfo.includeInBundle = false
    }
}

// AGP 9 dropped the android.kotlinOptions DSL, these live on the Kotlin extension now.
kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        freeCompilerArgs.addAll(
            "-opt-in=kotlin.ExperimentalStdlibApi",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=kotlinx.coroutines.FlowPreview",
            "-opt-in=kotlin.time.ExperimentalTime",
            "-opt-in=kotlin.RequiresOptIn",
        )
    }
}

dependencies {
    implementation(project(":app-common"))

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")

    addBaseKotlin()

    addDagger()

    addMoshi()

    addOkio()

    addBaseAndroid()
    addBaseAndroidUi()

    implementation("androidx.core:core-splashscreen:1.2.0")

    addNavigation()
    addBaseWorkManager()

    addTesting()

    implementation("com.android.billingclient:billing:9.1.0")

    add("gplayImplementation", platform("com.google.firebase:firebase-bom:34.18.0"))
    add("gplayImplementation", "com.google.firebase:firebase-analytics")
    add("gplayImplementation", "com.google.firebase:firebase-crashlytics")
}

apply(plugin = "com.google.gms.google-services")
apply(plugin = "com.google.firebase.crashlytics")