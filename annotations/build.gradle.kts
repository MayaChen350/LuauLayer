plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.ksp)
}

group = rootProject.group
version = rootProject.version

dependencies {
    implementation(kotlin("stdlib"))
}

kotlin {
    jvmToolchain(23)
}

java {
    sourceCompatibility = JavaVersion.VERSION_23
    targetCompatibility = JavaVersion.VERSION_23
}
