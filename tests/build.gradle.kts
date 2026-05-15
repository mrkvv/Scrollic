plugins {
    id("java")
    id("io.gatling.gradle") version "3.15.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.gatling.highcharts:gatling-charts-highcharts:3.15.0")

    gatlingImplementation("io.gatling.highcharts:gatling-charts-highcharts:3.15.0")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}