plugins {
    id("org.springframework.boot") version "3.4.4"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("plugin.spring") version "2.1.20"
}

dependencies {
    implementation(project(":heartbeat"))

    implementation("org.libvirt:libvirt:0.5.2")
}
