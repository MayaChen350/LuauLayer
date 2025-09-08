import org.gradle.internal.os.OperatingSystem

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.ksp)
}

group = rootProject.group
version = rootProject.version

dependencies {
    ksp(project(":processor"))

    implementation(libs.luau)
    implementation(libs.okio)
    implementation(libs.bundles.prettylog)
    implementation(libs.bundles.kotlinx)
    implementation(
        when (OperatingSystem.current()) {
            OperatingSystem.WINDOWS -> "dev.hollowcube:luau-natives-windows-x64:${libs.versions.luau.get()}"
            OperatingSystem.LINUX -> "dev.hollowcube:luau-natives-linux-x64:${libs.versions.luau.get()}"
            OperatingSystem.MAC_OS -> "dev.hollowcube:luau-natives-macos-arm64:${libs.versions.luau.get()}"
            else -> {
                throw GradleException("Operating system is not currently supported for luau.")
            }
        }
    )
    api(project(":annotations"))
}

kotlin {
    jvmToolchain(23)

    sourceSets.main {
        kotlin {
            srcDir("src/main/kotlin")
            srcDir(layout.buildDirectory.dir("generated/ksp/main/kotlin"))
        }
    }

    compilerOptions {
        freeCompilerArgs.addAll("-Xnon-local-break-continue", "-Xmulti-dollar-interpolation", "-Xcontext-parameters")
        //extraWarnings.set(true)
    }
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_23
    targetCompatibility = JavaVersion.VERSION_23
}

tasks {
    compileJava {
        options.release.set(23)
        options.encoding = "UTF-8"

        options.compilerArgs.addAll(
            listOf(
                "--enable-preview",
                "-Xlint:all",
                "-Xdiags:verbose"
            )
        )
    }
}
