import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    java
    `java-library`
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.spring") version "1.9.22"
}

group = "me.letsdev"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    compileOnly("org.springframework.boot:spring-boot-autoconfigure:3.4.4")
    api("com.github.merge-simpson:letsdev-time:0.1.0-rc1")

    testImplementation("org.springframework.boot:spring-boot-starter-web:3.4.4")

    // test tool
    testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
    testImplementation("io.mockk:mockk:1.13.12")
    testImplementation(kotlin("script-runtime"))
    testImplementation("io.kotest.extensions:kotest-extensions-spring:1.1.3")
}

kotlin{
    sourceSets {
        test {
            kotlin.srcDirs(listOf("src/test/kotlin"))
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs("--enable-preview")
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_21
    }
}