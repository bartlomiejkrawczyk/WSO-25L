plugins {
    kotlin("jvm") version "2.0.20"
}

configure(allprojects) {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    kotlin {
        jvmToolchain(21)
    }
}

dependencies {
}
