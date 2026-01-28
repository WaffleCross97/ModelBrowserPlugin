# Model Browser Plugin (Java Edition)

<img src="https://img.shields.io/badge/Minecraft-Java%20Edition-7CFC00" alt="Minecraft Java"> <img src="https://img.shields.io/badge/Plugin-Spigot%2FBukkit-yellow" alt="Spigot/Bukkit"> <img src="https://img.shields.io/badge/Java-17%2B-orange" alt="Java 17+"> <img src="https://img.shields.io/badge/Build-Gradle-02303A" alt="Gradle Build">

A comprehensive Minecraft Java Edition plugin that provides an intuitive GUI interface for browsing, previewing, and managing custom resource pack items directly in-game.

## ✨ Features

### 🎨 Visual Interface
- **Multi-page GUI** with organized item categories
- **Custom item rendering** with proper model display
- **Search functionality** with real-time filtering
- **Item details view** showing names, descriptions, and metadata
- **Material icons** matching each item type

### 🔧 Smart Categorization
- **Automatic sorting** by item type (Music Discs, Tools, Weapons, Food, etc.)
- **Custom categories** support via configuration
- **Resource pack detection** - automatically scans your resource pack
- **Dynamic updating** when new items are added

### 💼 Administration Tools
- **Permission-based commands**
- **Live reload** without server restart
- **Database management** tools
- **Export/Import** functionality for item lists

### 🛠️ Integration
- **Compatible with all major resource pack formats**
- **Works with CustomModelData items**
- **Supports OptiFine CIT and custom item textures**
- **Multi-version support** (1.16.5 - 1.20+)

## 📥 Installation

### Prerequisites
- **Minecraft Server**: Spigot, Paper, or any Bukkit-compatible server (1.16.5+)
- **Java**: JDK 17 or higher
- **Resource Pack**: Custom items in your resource pack

### Quick Installation
1. **Download** the latest `ModelBrowserPlugin.jar` from [Releases](https://github.com/WaffleCross97/ModelBrowserPlugin/releases)
2. **Place** the JAR file in your server's `plugins` folder
3. **Start/Restart** your server
4. **Configure** the plugin (see Configuration section below)
5. **Enjoy!** Use `/modelbrowser` in-game

## 🛠️ Development

### Gradle Build Configuration

**build.gradle.kts** (Kotlin DSL):
```kotlin
plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.waffle"
version = "1.0.0"
description = "Model Browser Plugin for Minecraft"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://jitpack.io")
}

dependencies {
    // Spigot/Paper API
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    
    // Adventure API for text components
    implementation("net.kyori:adventure-api:4.15.0")
    implementation("net.kyori:adventure-platform-bukkit:4.3.2")
    
    // GUI Framework
    implementation("dev.triumphteam:triumph-gui:3.1.7")
    
    // Configuration
    implementation("dev.dejvokep:boosted-yaml:1.3.4")
    
    // Database
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.xerial:sqlite-jdbc:3.45.1.0")
    implementation("com.h2database:h2:2.2.224")
    
    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.1")
    testImplementation("org.mockito:mockito-core:5.7.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.7.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.1")
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
    }
    
    test {
        useJUnitPlatform()
    }
    
    shadowJar {
        archiveClassifier.set("")
        archiveVersion.set("")
        
        // Relocate dependencies to avoid conflicts
        relocate("net.kyori", "com.waffle.modelbrowser.libs.kyori")
        relocate("dev.triumphteam", "com.waffle.modelbrowser.libs.triumphgui")
        relocate("dev.dejvokep", "com.waffle.modelbrowser.libs.boostedyaml")
        
        minimize()
        
        manifest {
            attributes(
                "Main-Class" to "com.waffle.modelbrowser.ModelBrowserPlugin",
                "Implementation-Version" to version,
                "Built-By" to System.getProperty("user.name"),
                "Build-Timestamp" to Instant.now().toString()
            )
        }
    }
    
    build {
        dependsOn(shadowJar)
    }
}
