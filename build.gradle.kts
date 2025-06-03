plugins {
    alias(libs.plugins.kotlin)
}

group = "evo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        name = "devOS"
        url = uri("https://mvn.devos.one/releases")
    }}

dependencies {
    testImplementation(kotlin("test"))
    setOf(
        libs.luau,
        libs.okio,
        libs.bundles.prettylog,
        libs.bundles.kotlinx,
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