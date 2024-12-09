plugins {
    id("org.springframework.boot") version "3.2.5" apply false
    id("io.spring.dependency-management") version "1.1.3" apply false
    id("java")
}

group = "org.twominds"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("mysql:mysql-connector-java:8.0.33")
    implementation("org.hibernate.orm:hibernate-core:6.5.0.Final")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa:3.2.5")

    implementation("org.springframework.boot:spring-boot-starter-web:3.2.5")

    implementation("com.fasterxml.jackson.core:jackson-core:2.14.2")

    implementation("com.google.guava:guava:31.0.1-jre")

    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")

    testCompileOnly("org.projectlombok:lombok:1.18.32")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.32")

    testImplementation("org.springframework.boot:spring-boot-starter-test:3.2.5")
    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation("uk.org.webcompere:system-stubs-jupiter:1.1.0")
    testImplementation("com.h2database:h2:2.2.224")

}

tasks.test {
    useJUnitPlatform()
}