plugins {
    java
    `java-library`
    checkstyle
    id("com.vanniktech.maven.publish") version "0.37.0"
}

group = "io.github.pavelicii"
version = "2.0.0"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}
tasks.compileJava {
    options.release.set(8)
}
tasks.compileTestJava {
    options.release.set(21)
}

checkstyle {
    toolVersion = "14.1.0"
    sourceSets = listOf() // Don't check anything with Checkstyle during 'check' task
}
tasks.register("checkstyle", Checkstyle::class) {
    source = fileTree("$rootDir/src")
    classpath = files()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.1.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.assertj:assertj-core:3.27.7")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.withType<Jar>().configureEach {
    from(files("LICENSE", "NOTICE")) {
        into("META-INF")
    }
}

tasks.assemble {
    dependsOn("plainJavadocJar")
}

tasks.withType<Javadoc> {
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
}

mavenPublishing {
    coordinates(project.group.toString(), "allpairs4j", project.version.toString())
    publishToMavenCentral(automaticRelease = false)
    signAllPublications()

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
