plugins {
    `kotlin-dsl`
    `java-library`
}

repositories {
    google()
    mavenCentral()
}
dependencies {
    // compileOnly avoids shipping a second AGP copy into buildSrc's runtime classpath,
    // which causes ClassCastException (same class name, different classloaders).
    compileOnly("com.android.tools.build:gradle:9.1.1")
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.21")
    implementation("com.squareup:javapoet:1.13.0")
}
