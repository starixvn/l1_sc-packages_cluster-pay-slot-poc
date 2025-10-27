# How to Run the Slot Machine Configuration Exporter

This guide will help you set up and run the `ConfigExporter` class to export slot machine configuration data to JSON files.

## Prerequisites

### 1. Install Java Development Kit (JDK)

**For Windows:**

- Download JDK 8 or higher from [Oracle](https://www.oracle.com/java/technologies/downloads/) or [OpenJDK](https://adoptium.net/)
- Install and set up JAVA_HOME environment variable
- Add Java to your PATH

**For macOS:**

```bash
# Using Homebrew
brew install openjdk@8

# Or download from Oracle/OpenJDK website
```

**For Linux (Ubuntu/Debian):**

```bash
sudo apt update
sudo apt install openjdk-8-jdk
```

**Verify Java installation:**

```bash
java -version
javac -version
```

### 2. Install Apache Maven

**For Windows:**

- Download Maven from [Apache Maven website](https://maven.apache.org/download.cgi)
- Extract to a folder (e.g., `C:\Program Files\Apache\maven`)
- Add Maven bin directory to your PATH
- Set M2_HOME environment variable

**For macOS:**

```bash
# Using Homebrew
brew install maven
```

**For Linux (Ubuntu/Debian):**

```bash
sudo apt update
sudo apt install maven
```

**Verify Maven installation:**

```bash
mvn -version
```

## Project Setup

### 1. Clone or Download the Project

```bash
git clone git@git3.nexdev.net:l1/sc-packages/cluster-pay-slot-poc.git --branch GL-2658-export-reel-settings
cd cluster-pay-slot-poc
```

## Running the Configuration Exporter

### Method 1: Using Maven (Recommended)

This is the simplest method as the project is already configured with the exec-maven-plugin:

```bash
mvn exec:java -D exec.args="20"
```

> _Note_: first argument (20) is multiplier for odd.

### Method 2: Using Maven with Compilation

If you want to ensure everything is compiled first:

```bash
mvn compile exec:java -D exec.args="20"
```

## Generated Files

The exporter will create three JSON files in the project root:

1. **`configs.json`** - Main configuration file containing:

   - Odds configuration
   - Free game rounds configuration
   - Rage meter configuration

2. **`ng_reels.json`** - Normal game reels data

3. **`fg_reels.json`** - Free game reels data

## Troubleshooting

### Common Issues

**1. "mvn: command not found"**

- Ensure Maven is installed and added to your PATH
- Restart your terminal/command prompt after installation

**2. "java: command not found"**

- Ensure Java JDK is installed and added to your PATH
- Verify JAVA_HOME is set correctly

**3. "NoClassDefFoundError: com/fasterxml/jackson/databind/JsonNode"**

- Run `mvn dependency:copy-dependencies` first
- Or use `mvn exec:java` instead

**4. Compilation errors**

- Ensure you're using JDK 8 or higher
- Run `mvn clean compile` to clean and recompile

**5. Permission denied (Linux/macOS)**

- Ensure the project directory has proper read/write permissions
- Use `chmod` if necessary

### Getting Help

If you encounter issues:

1. Check that Java and Maven are properly installed
2. Verify you're in the correct project directory
3. Try running `mvn clean compile` first
4. Check the console output for specific error messages

## Project Dependencies

The project uses the following dependencies (automatically managed by Maven):

- Jackson Core 2.15.2
- Jackson Databind 2.15.2
- Jackson Annotations 2.15.2

These are defined in `pom.xml` and will be downloaded automatically when you run Maven commands.
