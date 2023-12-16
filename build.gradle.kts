plugins {
    java
    `java-library`
    `maven-publish`
    signing
    checkstyle
}

group = "io.github.pavelicii"
version = "1.0.1"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    withJavadocJar()
    withSourcesJar()
}

checkstyle {
    toolVersion = "9.3" // Latest version compatible with Java 8
    sourceSets = listOf() // Don't check anything with Checkstyle during 'check' task
}
tasks.register("checkstyle", Checkstyle::class) {
    source = fileTree("$rootDir/src")
    classpath = files()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.assertj:assertj-core:3.24.2")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.withType<Javadoc> {
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "allpairs4j"
            from(components["java"])
            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
            pom {
                name.set("AllPairs4J")
                description.set("Pairwise combinations generator with constraints for Java")
                url.set("https://github.com/pavelicii/allpairs4j")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("pavelicii")
                        name.set("Pavel Nazimok")
                        email.set("pavelnazimok@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/pavelicii/allpairs4j.git")
                    developerConnection.set("scm:git:ssh://github.com/pavelicii/allpairs4j.git")
                    url.set("https://github.com/pavelicii/allpairs4j")
                }
                issueManagement {
                    system.set("GitHub Issues")
                    url.set("https://github.com/pavelicii/allpairs4j/issues")
                }
            }
        }
    }
    repositories {
        maven {
            val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            url = if (version.toString().endsWith("-SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
            val sonatypeUsername: String? by project
            val sonatypePassword: String? by project
            credentials {
                username = sonatypeUsername
                password = sonatypePassword
            }
        }
    }
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications["mavenJava"])
}
