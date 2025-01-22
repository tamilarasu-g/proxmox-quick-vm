plugins {
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.serialization") version "2.1.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    testImplementation(kotlin("test"))
    val ktor_version: String by project

    dependencies {
        implementation("io.ktor:ktor-client-core:$ktor_version")
        implementation("io.ktor:ktor-client-cio:$ktor_version")
        implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
        implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
        implementation("com.github.dotenv-org:dotenv-vault-kotlin:0.0.2")
        implementation("org.jetbrains.exposed:exposed-core:0.43.0")
        implementation("org.jetbrains.exposed:exposed-dao:0.43.0")
        implementation("org.jetbrains.exposed:exposed-jdbc:0.43.0")
        implementation("org.xerial:sqlite-jdbc:3.42.0.0")
        implementation("org.jetbrains.exposed:exposed-java-time:0.43.0")
    }
    //...
    val logback_version: String by project

    dependencies {
        //...
        implementation("ch.qos.logback:logback-classic:$logback_version")
    }
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(19)
}