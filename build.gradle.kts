plugins {
    kotlin("jvm") version "2.1.20"
}

group = "evo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(kotlin("reflect"))
    implementation(libs.luau)
    implementation(files("libs/luau-natives-windows-x64-dev.jar"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}