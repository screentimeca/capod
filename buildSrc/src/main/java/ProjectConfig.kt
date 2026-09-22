import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.dsl.Packaging
import org.gradle.api.JavaVersion
import java.io.File
import java.io.FileInputStream
import java.time.Instant
import java.util.*

object ProjectConfig {
    const val packageName = "com.screentime.airpod"

    const val minSdk = 26
    const val compileSdk = 37
    const val targetSdk = 37

    data class VersionInfo(
        val major: Int,
        val minor: Int,
        val patch: Int,
        val build: Int,
    ) {
        val name: String = "${major}.${minor}.${patch}-rc${build}"
        val code: Int = major * 10000000 + minor * 100000 + patch * 1000 + build * 10
    }

    fun versionFrom(propertiesFile: File): VersionInfo {
        val versionProperties = Properties().apply {
            propertiesFile.inputStream().use { load(it) }
        }
        return VersionInfo(
            major = versionProperties.getProperty("project.versioning.major").toInt(),
            minor = versionProperties.getProperty("project.versioning.minor").toInt(),
            patch = versionProperties.getProperty("project.versioning.patch").toInt(),
            build = versionProperties.getProperty("project.versioning.build").toInt(),
        )
    }

    object Version {
        private val info = versionFrom(
            listOf(
                File("version.properties"),
                File(System.getProperty("user.dir"), "version.properties"),
            ).first { it.isFile }
        )
        val major = info.major
        val minor = info.minor
        val patch = info.patch
        val build = info.build
        val name = info.name
        val code = info.code
    }
}

fun lastCommitHash(): String = Runtime.getRuntime().exec("git rev-parse --short HEAD").let { process ->
    process.waitFor()
    val output = process.inputStream.use { input ->
        input.bufferedReader().use {
            it.readText()
        }
    }
    process.destroy()
    output.trim()
}

fun buildTime(): Instant = Instant.now()

fun LibraryExtension.setupLibraryDefaults() {
    compileSdk = ProjectConfig.compileSdk

    defaultConfig {
        minSdk = ProjectConfig.minSdk

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    fun Packaging.() {
        resources.excludes += "DebugProbesKt.bin"
    }
}

fun com.android.build.api.dsl.SigningConfig.setupCredentials(
    signingPropsPath: File? = null
) {

    val keyStoreFromEnv = System.getenv("STORE_PATH")?.let { File(it) }

    if (keyStoreFromEnv?.exists() == true) {
        println("Using signing data from environment variables.")
        storeFile = keyStoreFromEnv
        storePassword = System.getenv("STORE_PASSWORD")
        keyAlias = System.getenv("KEY_ALIAS")
        keyPassword = System.getenv("KEY_PASSWORD")
    } else {
        println("Using signing data from properties file.")
        val props = Properties().apply {
            signingPropsPath?.takeIf { it.canRead() }?.let { load(FileInputStream(it)) }
        }

        val keyStorePath = props.getProperty("release.storePath")?.let { File(it) }

        if (keyStorePath?.exists() == true) {
            storeFile = keyStorePath
            storePassword = props.getProperty("release.storePassword")
            keyAlias = props.getProperty("release.keyAlias")
            keyPassword = props.getProperty("release.keyPassword")
        }
    }
}
