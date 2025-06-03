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
    //implementation(kotlin("reflect"))
    setOf(
        libs.luau,
        files("libs/luau-natives-windows-x64-dev.jar")
    ).forEach(::implementation)
}

tasks {
    compileJava {
        options.release.set(21)
        options.encoding = "UTF-8"
    }
    test {
        useJUnitPlatform()
    }
}

kotlin {
    jvmToolchain(21)

    compilerOptions {
        freeCompilerArgs.addAll("-Xnon-local-break-continue", "-Xmulti-dollar-interpolation")
        extraWarnings.set(true)
    }
}

java {
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}