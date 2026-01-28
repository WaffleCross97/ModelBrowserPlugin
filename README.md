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

### Gradle Build 
git pull https://github.com/WaffleCross97/ModelBrowserPlugin.git
./gradlew build
./gradlew runserver to test the plugin
to update make a pull request 
