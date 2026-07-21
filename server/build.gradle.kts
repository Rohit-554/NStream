plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlinSerialization)
}

group = "io.jadu.nstream"
version = "1.0.0"
application {
    mainClass = "io.jadu.nstream.ApplicationKt"
}

dependencies {
    api(projects.core)
    implementation(libs.koin.core)
    implementation(libs.logback)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.contentNegotiation)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.serverContentNegotiation)
    implementation(libs.ktor.serverCors)
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    implementation(libs.ktor.serverStatusPages)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
    implementation(libs.koin.ktor)
    implementation(libs.ktor.serverCallId)
    implementation(libs.ktor.serverOpenapi)
    implementation(libs.ktor.serverSwagger)
    implementation(libs.postgresql)
    implementation(libs.lettuce.core)
    implementation(libs.flyway.core)
    implementation(libs.flyway.database.postgresql)
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.java.time)
    implementation(libs.java.jwt)
    implementation(libs.argon2.jvm)
    testImplementation(libs.testcontainers.junit)
    testImplementation(libs.testcontainers.postgresql)
    implementation(libs.ktor.serverAuth)
    implementation(libs.ktor.serverAuthJwt)
    implementation(libs.jakarta.mail)
    runtimeOnly(libs.angus.mail)
}
