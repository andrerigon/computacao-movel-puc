import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    idea
    kotlin("jvm") version "1.9.10"
    id("org.jmailen.kotlinter") version "3.9.0"
    id("application")
}


apply(plugin = "org.jmailen.kotlinter")

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

buildscript {
    repositories {
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
    }
    dependencies {
        classpath("org.jmailen.gradle:kotlinter-gradle:3.7.0")
    }
}

kotlin.sourceSets.all {
    languageSettings.optIn("kotlin.RequiresOptIn")
}

tasks.compileKotlin {
    kotlinOptions {
        allWarningsAsErrors = true
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()
    kotlinOptions.javaParameters = true
}

repositories {
    maven {
        name = "LAC PUC-Rio"
        url = uri("https://bitbucket.org/endler/contextnet-dependencies/raw/master")
    }
    mavenCentral()
    mavenLocal()
}


dependencies {
    implementation("br.pucrio.inf.lac:contextnet:3.0")
    implementation("br.pucrio.inf.lac:ExchangeData:1.0")
    implementation("br.pucrio.inf.lac:clientLib:3.0")
    implementation("org.json:json:20230227")
    implementation("com.googlecode.json-simple:json-simple:1.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.10.0")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.10.0")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.10.0")
    implementation("org.slf4j:slf4j-api:2.0.0-alpha1")
    implementation("org.slf4j:slf4j-simple:1.8.0-beta4")
    implementation("org.apache.kafka:kafka-clients:2.8.0")
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.fasterxml.jackson.core:jackson-databind:2.10.0")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.10.0")
    implementation("com.uchuhimo:konf:1.1.2")
    implementation("com.espertech:esper:7.1.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.8")
}
