plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.ksp)
}

group = rootProject.group
version = rootProject.version

dependencies {
    implementation(kotlin("stdlib"))

    api("com.google.devtools.ksp:symbol-processing-api:${libs.versions.ksp.get()}")
    implementation(libs.bundles.kotlinpoet)
    implementation(libs.luau)
}

kotlin {
    jvmToolchain(21)
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}