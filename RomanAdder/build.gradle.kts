import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
    application
}

group = "eu.rafael"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("ch.qos.logback:logback-classic:1.4.4")
    implementation("io.ktor:ktor-server-call-logging:2.1.2")
    implementation("io.ktor:ktor-server-core:2.1.2")
    implementation("io.ktor:ktor-server-netty:2.1.2")

    // TEST
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.0")
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}