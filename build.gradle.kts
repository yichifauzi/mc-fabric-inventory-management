plugins {
  id("fabric-loom") version "1.1-SNAPSHOT"
  id("maven-publish")
  id("com.github.johnrengelman.shadow") version "7.1.2"
}

val targetJavaVersion = 17
val fullVersion = project.property("mod_version").toString() + "+" + project.property("minecraft_version").toString()

val zipped by configurations.creating {
  isCanBeConsumed = true
  isCanBeResolved = true
}

configurations.configureEach {
  version = project.property("mod_version").toString()
  group = project.property("maven_group").toString()
}

repositories {
  maven("https://maven.terraformersmc.com/releases/")
  maven("https://maven.rnda.dev/releases")
}

dependencies {
  minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
  mappings("net.fabricmc:yarn:${project.property("yarn_mappings")}:v2")
  modImplementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")
  modImplementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_version")}")

  modImplementation("com.terraformersmc:modmenu:${project.property("mod_menu_version")}")

  modImplementation("me.roundaround:roundalib:${project.property("roundalib_version")}")
  shadow("me.roundaround:roundalib:${project.property("roundalib_version")}")
  zipped(
    group = "me.roundaround",
    name = "roundalib",
    version = project.property("roundalib_version").toString(),
    classifier = "resources",
    ext = "zip"
  )
}

java {
  sourceCompatibility = JavaVersion.toVersion(targetJavaVersion)
  targetCompatibility = JavaVersion.toVersion(targetJavaVersion)

  withSourcesJar()
}

tasks.withType<JavaCompile> {
  options.encoding = "UTF-8"
  options.release.set(targetJavaVersion)
}

tasks.jar {
  from("LICENSE") {
    rename { "${it}_${project.property("archive_base_name")}" }
  }
}

tasks.processResources {
  inputs.property("version", project.version)

  filesMatching("fabric.mod.json") {
    expand("version" to project.version)
  }

  from(zipTree(zipped.singleFile.absolutePath).asPath) {
    include("*/**")
    into(layout.buildDirectory.dir("resources/imported"))
  }
}

tasks.shadowJar {
  configurations = listOf(project.configurations.shadow.get())

  archiveBaseName.set(project.property("archive_base_name").toString())
  archiveVersion.set(fullVersion)
  archiveClassifier.set("shaded")

  relocate("me.roundaround.roundalib", "me.roundaround.inventorymanagement.roundalib")
}

tasks.remapJar {
  dependsOn(tasks.shadowJar)
  inputFile.set(tasks.shadowJar.get().archiveFile)

  archiveBaseName.set(project.property("archive_base_name").toString())
  archiveVersion.set(fullVersion)
  archiveClassifier.set("")
}

tasks.remapSourcesJar {
  archiveBaseName.set(project.property("archive_base_name").toString())
  archiveVersion.set(fullVersion)
  archiveClassifier.set("sources")
}

publishing {
  publications {
    create<MavenPublication>("maven") {
      from(components["java"])

      artifactId = tasks.remapJar.get().archiveBaseName.get()
      version = tasks.remapJar.get().archiveVersion.get()

      repositories {
        maven {
          url = uri(property("selfHostedMavenUrl").toString() + "/releases")
          credentials(PasswordCredentials::class) {
            username = property("selfHostedMavenUser").toString()
            password = property("selfHostedMavenPass").toString()
          }
        }
      }
    }
  }
}
