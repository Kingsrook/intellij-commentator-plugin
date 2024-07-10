plugins {
  id("java")
  id("org.jetbrains.kotlin.jvm") version "1.9.22"
  id("org.jetbrains.intellij") version "1.17.2"
}

group = "com.kingsrook"
version = "1.5.0-SNAPSHOT"

repositories {
  mavenCentral()
      maven {
          url = uri("https://repo.clojars.org/")
      }
}


// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
  version.set("2024.1")
  type.set("IC") // Target IDE Platform

  plugins.set(listOf("com.intellij.java"))
}

tasks {
  withType<JavaCompile> {
    sourceCompatibility = "17"
    targetCompatibility = "17"
  }

  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
  }

  patchPluginXml {
    sinceBuild.set("232")
    untilBuild.set("242.*")
  }

  signPlugin {
    certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
    privateKey.set(System.getenv("PRIVATE_KEY"))
    password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
  }

  publishPlugin {
    token.set(System.getenv("PUBLISH_TOKEN"))
  }

  test {
      useJUnitPlatform()
  }

  // this option makes it so settings are searchable in the settings dialog
  // we don't have any at this time, so we don't care.
  // we're just turing this off, because it's breaking builds with it on for "who knows why"
  buildSearchableOptions {
      enabled = false
  }
}

dependencies {
   implementation("clojure-interop:java.awt:1.0.5")
	testImplementation(platform("org.junit:junit-bom:5.10.2"))
	testImplementation("org.junit.jupiter:junit-jupiter")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}