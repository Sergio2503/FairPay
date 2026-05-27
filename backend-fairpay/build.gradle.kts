plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
    application
}

group = "com.fairpay"
version = "0.0.1"

application {
    mainClass.set("com.fairpay.ApplicationKt")
}

kotlin {
    jvmToolchain(17)
}

val ktorVersion = "2.3.7"
val exposedVersion = "0.50.1"

repositories {
    mavenCentral()
}

dependencies {
    // KTOR
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-cors-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages-jvm:$ktorVersion")

    // SERIALIZACION
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    // EXPOSED
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")

    // DATABASE
    implementation("org.postgresql:postgresql:42.7.3")
    implementation("com.zaxxer:HikariCP:5.1.0")

    // PASSWORD HASH
    implementation("org.mindrot:jbcrypt:0.4")

    // LOGGING
    implementation("ch.qos.logback:logback-classic:1.4.14")

    // TEST
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation(kotlin("test"))
}

tasks.processResources {
    from("src/main/resources") {
        include("**/*.conf")
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes["Main-Class"] = "com.fairpay.ApplicationKt"
    }

    from({
        configurations.runtimeClasspath.get().map { zipTree(it) }
    })
}