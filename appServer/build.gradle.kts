plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ktor)
    application
}

application {
    mainClass.set("com.mediasage.appserver.ApplicationKt")
}

// Briefing eval: on-demand scenarios run against the real Claude API (see docs/MS-765-briefing-eval.md).
// Its own source set, so `test`, allTests and CI never compile or run it, and it never ships in the server.
sourceSets {
    create("eval") {
        compileClasspath += sourceSets["main"].output
        runtimeClasspath += sourceSets["main"].output
    }
}
configurations["evalImplementation"].extendsFrom(configurations["implementation"])
configurations["evalRuntimeOnly"].extendsFrom(configurations["runtimeOnly"])

dependencies {
    // Ktor Server
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.serialization.json)
    implementation(libs.logback)

    // Ktor Client (for calling Claude API, News API, Scripture API)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)

    // Koin
    implementation(libs.koin.core)
    implementation(libs.koin.ktor)

    // HTML scraping
    implementation(libs.jsoup)

    // Exposed + DB drivers (SQLite for local dev, Postgres for production)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.sqlite.jdbc)
    implementation(libs.postgresql.jdbc)

    // Serialization
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)

    // Testing
    testImplementation(libs.kotlin.test)
    testImplementation(libs.ktor.server.tests)
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.koin.test)

    // Briefing eval
    "evalImplementation"(libs.kotlin.testJunit)
}

tasks.register<Test>("briefingEval") {
    group = "verification"
    description = "Runs every briefing scenario (or -Pscenario=<name>) against the real Claude API. Needs CLAUDE_API_KEY."
    testClassesDirs = sourceSets["eval"].output.classesDirs
    classpath = sourceSets["eval"].runtimeClasspath
    useJUnit()
    project.findProperty("scenario")?.let { systemProperty("briefingEval.scenario", it) }
    project.findProperty("week")?.let { systemProperty("briefingEval.week", it) }
    testLogging {
        showStandardStreams = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
    outputs.upToDateWhen { false }
}

// Kover instruments every Test task and compiles every source set for its reports, so CI's koverXmlReport
// would otherwise run (or at least build) the eval. Detekt only scans main/test by default.
configure<kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension> {
    currentProject {
        instrumentation { disabledForTestTasks.add("briefingEval") }
        sources { excludedSourceSets.add("eval") }
    }
}
configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
    source.from("src/eval/kotlin")
}
