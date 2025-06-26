plugins {
    kotlin("jvm") version "2.1.10"
    kotlin("plugin.serialization") version "1.8.0"
    id("io.ktor.plugin") version "3.1.0"

}

ktor {
    docker {
//        jreVersion.set(JavaVersion.VERSION_22)
        jreVersion.set(JavaVersion.VERSION_21)
        localImageName.set("ktor-api")
        imageTag.set("latest")
        portMappings.set(
            listOf(
                io.ktor.plugin.features.DockerPortMapping(8080, 8080, io.ktor.plugin.features.DockerPortMappingProtocol.TCP)
            )
        )
    }
    application {
        mainClass.set("com.example.ApplicationKt")
    }
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("io.ktor:ktor-client-core:2.3.4")
    implementation("io.ktor:ktor-server-netty:2.3.4")
    implementation("io.ktor:ktor-server-core:2.3.4")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.4")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.4")
    implementation("ch.qos.logback:logback-classic:1.4.11")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
    implementation("org.jetbrains.exposed:exposed-core:0.43.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.43.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.43.0")
    implementation("org.postgresql:postgresql:42.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("com.sun.mail:javax.mail:1.6.2")
    implementation("org.jetbrains.exposed:exposed-java-time:0.41.1")
    implementation("at.favre.lib:bcrypt:0.10.2")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.4")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.4")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    implementation("com.auth0:java-jwt:4.4.0")
    implementation("io.ktor:ktor-server-auth:2.3.4")
    implementation("io.ktor:ktor-server-auth-jwt:2.3.4")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
//    jvmToolchain(22)
    jvmToolchain(21)
}