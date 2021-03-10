import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktorVersion = "1.4.1"
val logback_version = "1.2.1"
val logback_contrib_version = "0.1.5"
val jacksonVersion = "2.10.3"
val prometheusVersion = "0.6.0"
val hikariVersion = "3.3.1"
val mainClass = "no.nav.helse.grensekomp.AppKt"
val junitJupiterVersion = "5.7.0"
val assertJVersion = "3.12.2"
val mockKVersion = "1.9.3"
val tokenSupportVersion = "1.3.1"
val mockOAuth2ServerVersion = "0.2.1"
val koinVersion = "2.0.1"
val valiktorVersion = "0.12.0"
val cxfVersion = "3.4.1"
val jaxwsVersion = "2.3.1"
val jaxwsToolsVersion = "2.3.3"
val apachePoiVersion = "4.1.2"
val influxVersion = "2.20"
val githubPassword: String by project


plugins {
    application
    kotlin("jvm") version "1.4.20"
    id("com.github.ben-manes.versions") version "0.27.0"
    id("com.autonomousapps.dependency-analysis") version "0.71.0"
    jacoco
}

application {
    mainClassName = "no.nav.helse.grensekomp.web.AppKt"
}

buildscript {
    dependencies {
        classpath("org.junit.platform:junit-platform-gradle-plugin:1.2.0")
    }
}

dependencies {
    // SNYK-fikser - Disse kan fjernes etterhver som våre avhengigheter oppdaterer sine versjoner
    // Forsøk å fjerne en og en og kjør snyk test --configuration-matching=runtimeClasspath
    constraints {
        implementation("commons-collections:commons-collections") {
            version {
                strictly("3.2.2")
            }
            because("snyk control")
        }
        implementation("org.apache.httpcomponents:httpclient") {
            version {
                strictly("4.5.13")
            }
            because("snyk control")
        }
        implementation("io.netty:netty-codec-http2") {
            version {
                strictly("4.1.59.Final")
            }
            because("snyk control")
        }
        implementation("org.yaml:snakeyaml") {
            version {
                strictly("1.27")
            }
            because("snyk control")
        }
        implementation("junit:junit") {
            version {
                strictly("4.13.1")
            }
            because("overstyrer transiente 4.12 gjennom koin-test")
        }
        implementation("io.netty:netty-transport-native-epoll") {
            version {
                strictly("4.1.59.Final")
            }
            because("snyk control")
        }
    }
    implementation("commons-codec:commons-codec:1.13") // overstyrer transiente 1.10
    implementation("io.netty:netty-codec:4.1.59.Final") // overstyrer transiente 4.1.44
    implementation("io.netty:netty-codec-http:4.1.59.Final") // overstyrer transiente 4.1.51.Final gjennom ktor-server-netty
    implementation("org.eclipse.jetty:jetty-server:9.4.37.v20210219")
    implementation("com.google.guava:guava:30.0-jre") //[Medium Severity][https://snyk.io/vuln/SNYK-JAVA-COMGOOGLEGUAVA-1015415] overstyrer versjon 29.0
    // -- end snyk fixes

    implementation("org.apache.poi:poi:$apachePoiVersion")
    implementation("org.apache.poi:poi-ooxml:$apachePoiVersion")
    implementation("org.influxdb:influxdb-java:${influxVersion}")

    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-jackson:$ktorVersion")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-auth:$ktorVersion")
    implementation("io.ktor:ktor-client-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-apache:$ktorVersion")
    implementation("io.ktor:ktor-client-json:$ktorVersion")
    implementation("io.ktor:ktor-client-jackson:$ktorVersion")
    implementation("io.ktor:ktor-locations:$ktorVersion")

    implementation("org.valiktor:valiktor-core:$valiktorVersion")
    implementation("org.valiktor:valiktor-javatime:$valiktorVersion")

    implementation("com.sun.activation:javax.activation:1.2.0")

    implementation("org.koin:koin-core:$koinVersion")
    implementation("org.koin:koin-ktor:$koinVersion")
    implementation("no.nav.security:token-client-core:$tokenSupportVersion")
    implementation("no.nav.security:token-validation-ktor:$tokenSupportVersion")

    implementation("javax.ws.rs:javax.ws.rs-api:2.1.1")
    implementation("no.nav.security:mock-oauth2-server:$mockOAuth2ServerVersion")
    implementation("no.nav.helsearbeidsgiver:helse-arbeidsgiver-felles-backend:2021.03.02-15-19-eb0ee")
    implementation("no.nav.common:log:2.2020.10.15_11.43-b1f02e7bd6ae")

    implementation(kotlin("stdlib"))

    implementation("org.slf4j:slf4j-api:1.7.30")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("ch.qos.logback.contrib:logback-jackson:$logback_contrib_version")
    implementation("ch.qos.logback.contrib:logback-json-classic:$logback_contrib_version")
    implementation("net.logstash.logback:logstash-logback-encoder:6.4")
    implementation("org.codehaus.janino:janino:3.0.6")
    implementation("org.flywaydb:flyway-core:7.3.0")
    implementation("org.apache.pdfbox:pdfbox:2.0.21")


    implementation("no.nav.tjenestespesifikasjoner:altinn-correspondence-agency-external-basic:1.2019.09.25-00.21-49b69f0625e0")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.11+")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")

    implementation("com.zaxxer:HikariCP:$hikariVersion")
    implementation("com.github.tomakehurst:wiremock-standalone:2.25.1")
    implementation("org.postgresql:postgresql:42.2.16")

    implementation("io.prometheus:simpleclient_common:$prometheusVersion")
    implementation("io.prometheus:simpleclient_hotspot:$prometheusVersion")

    implementation("javax.xml.ws:jaxws-api:$jaxwsVersion")
    implementation("org.apache.cxf:cxf-rt-frontend-jaxws:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-features-logging:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-transports-http:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-ws-security:$cxfVersion")
    implementation("com.sun.activation:javax.activation:1.2.0")
    implementation("com.sun.xml.ws:jaxws-tools:$jaxwsToolsVersion") {
        exclude(group = "com.sun.xml.ws", module = "policy")
    }
    testImplementation("org.koin:koin-test:$koinVersion")
    implementation("com.github.javafaker:javafaker:1.0.2") // flytt denne til test når generatorene ikke er nødvendige i prod-koden lenger
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.3.2")
    testImplementation("io.mockk:mockk:$mockKVersion")
    testImplementation("io.ktor:ktor-server-tests:$ktorVersion")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitJupiterVersion")
    testImplementation("org.assertj:assertj-core:$assertJVersion")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")
}

tasks.named<KotlinCompile>("compileKotlin")

tasks.named<KotlinCompile>("compileKotlin") {
    kotlinOptions.jvmTarget = "11"
}

tasks.named<KotlinCompile>("compileTestKotlin") {
    kotlinOptions.jvmTarget = "11"
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

repositories {
    maven {
        credentials {
            username = "x-access-token"
            password = githubPassword
        }
        setUrl("https://maven.pkg.github.com/navikt/helse-arbeidsgiver-felles-backend")
    }

    jcenter{
        content {
            excludeGroup("no.nav.helsearbeidsgiver")
        }
    }
    mavenCentral{
        content {
            excludeGroup("no.nav.helsearbeidsgiver")
        }
    }
    maven("https://kotlin.bintray.com/ktor")
    maven(url = "https://packages.confluent.io/maven/")

    maven(url = "https://jitpack.io") {
        content {
            excludeGroup("no.nav.helsearbeidsgiver")
        }
    }
}
tasks.named<Jar>("jar") {
    baseName = ("app")

    manifest {
        attributes["Main-Class"] = mainClass
        attributes["Class-Path"] = configurations.runtimeClasspath.get().joinToString(separator = " ") {
            it.name
        }
    }

    doLast {
        configurations.runtimeClasspath.get().forEach {
            val file = File("$buildDir/libs/${it.name}")
            if (!file.exists())
                it.copyTo(file)
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
        showStackTraces = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

tasks.named<Test>("test") {
    include("no/nav/helse/**")
    exclude("no/nav/helse/slowtests/**")
}

task<Test>("slowTests") {
    include("no/nav/helse/slowtests/**")
    outputs.upToDateWhen { false }
    group = "verification"

}

tasks.withType<Wrapper> {
    gradleVersion = "6.8.2"
}
