# JSON/YAML Validator Maven Plugin

[![CI Build](https://github.com/dataliquid/json-yaml-validator-maven-plugin/actions/workflows/ci.yml/badge.svg)](https://github.com/dataliquid/json-yaml-validator-maven-plugin/actions/workflows/ci.yml)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.dataliquid.maven/json-yaml-validator-maven-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.dataliquid.maven/json-yaml-validator-maven-plugin)

A Maven plugin for validating JSON and YAML files against a JSON Schema using the [networknt/json-schema-validator](https://github.com/networknt/json-schema-validator) library.

## Requirements

- Java 11 or higher
- Maven 3.6.0 or higher

## Features

- Validate JSON and YAML files against a single JSON Schema
- Support for multiple JSON Schema versions (Draft 4, 6, 7, 2019-09, 2020-12)
- Flexible file filtering with include/exclude patterns
- Fail build on validation errors (configurable)
- Detailed error reporting

## Usage

Add the plugin to your project's `pom.xml`:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.dataliquid.maven</groupId>
            <artifactId>json-yaml-validator-maven-plugin</artifactId>
            <version>1.0.0</version>
            <executions>
                <execution>
                    <id>validate-json-yaml</id>
                    <phase>validate</phase>
                    <goals>
                        <goal>validate</goal>
                    </goals>
                    <configuration>
                        <schemaFile>src/main/resources/schemas/schema.json</schemaFile>
                        <sourceDirectory>src/main/resources/data</sourceDirectory>
                        <failOnError>true</failOnError>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

## Configuration Parameters

| Parameter | Description | Default | Required |
|-----------|-------------|---------|----------|
| `schemaFile` | Path to the JSON Schema file | - | Yes |
| `sourceDirectory` | Directory containing files to validate | `${project.basedir}/src/main/resources` | No |
| `includes` | File patterns to include | `**/*.json,**/*.yaml,**/*.yml` | No |
| `excludes` | File patterns to exclude | - | No |
| `failOnError` | Fail build on validation error | `true` | No |
| `schemaVersion` | JSON Schema version (V4, V6, V7, V201909, V202012) | `V202012` | No |
| `skip` | Skip validation execution | `false` | No |
| `schemaMappings` | Map schema IDs to local files/directories for $ref resolution | - | No |
| `failOnNoFilesFound` | Fail build when no files match the include patterns | `true` | No |

## File Pattern Matching

The `includes` and `excludes` parameters use glob-style patterns:
- `*` matches any character except `/`
- `**` matches any character including `/`
- `?` matches any single character

Examples:
- `**/*.json` - all JSON files in any subdirectory
- `config/*.yaml` - YAML files in the config directory
- `**/test/**` - all files in any test directory

## Example Configuration

```xml
<configuration>
    <schemaFile>src/main/resources/schemas/config-schema.json</schemaFile>
    
    <sourceDirectory>src/main/resources</sourceDirectory>
    
    <!-- Only validate specific file types -->
    <includes>
        <include>**/*.json</include>
        <include>**/*.yaml</include>
        <include>**/*.yml</include>
    </includes>
    
    <!-- Exclude certain files -->
    <excludes>
        <exclude>**/draft/**</exclude>
        <exclude>**/temp/**</exclude>
        <exclude>**/*.backup.json</exclude>
    </excludes>
    
    <failOnError>true</failOnError>
    
    <!-- Optional: Map schema IDs to local files for $ref resolution -->
    <schemaMappings>
        <schemaMapping>http://example.com/schemas/=src/main/resources/schemas/</schemaMapping>
        <schemaMapping>http://example.com/user.json=src/main/resources/schemas/user.json</schemaMapping>
    </schemaMappings>
</configuration>
```

## Schema Mappings

The `schemaMappings` parameter allows you to map schema IDs to local files or directories for resolving `$ref` references in your JSON Schema. This is useful when your schema references external schemas by URL.

### Mapping Types

1. **Directory Mapping**: Map a URL prefix to a local directory
   ```xml
   <schemaMapping>http://example.com/schemas/=src/main/resources/schemas/</schemaMapping>
   ```
   This maps `http://example.com/schemas/user.json` to `src/main/resources/schemas/user.json`

2. **File Mapping**: Map a specific schema ID to a local file
   ```xml
   <schemaMapping>http://example.com/user.json=src/main/resources/schemas/user.json</schemaMapping>
   ```

### Example with Schema Mappings

```xml
<configuration>
    <schemaFile>src/main/resources/schemas/main-schema.json</schemaFile>
    <sourceDirectory>src/main/resources/data</sourceDirectory>
    <schemaMappings>
        <!-- Map a URL prefix to a local directory -->
        <schemaMapping>http://example.com/schemas/=src/main/resources/schemas/</schemaMapping>
        <!-- Map specific schema IDs to local files -->
        <schemaMapping>http://example.com/common.json=src/main/resources/schemas/common-definitions.json</schemaMapping>
    </schemaMappings>
</configuration>
```

## Building

```bash
mvn clean install
```

## Complete Example POM

Here's a complete example POM file showing how to configure the plugin:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.dataliquid.maven</groupId>
    <artifactId>example-project</artifactId>
    <version>1.0.0</version>

    <build>
        <plugins>
            <plugin>
                <groupId>com.dataliquid.maven</groupId>
                <artifactId>json-yaml-validator-maven-plugin</artifactId>
                <version>1.0.0</version>
                <executions>
                    <execution>
                        <id>validate-json-yaml</id>
                        <phase>validate</phase>
                        <goals>
                            <goal>validate</goal>
                        </goals>
                        <configuration>
                            <!-- Path to JSON Schema file -->
                            <schemaFile>src/main/resources/schemas/person-schema.json</schemaFile>
                            
                            <!-- Directory containing files to validate -->
                            <sourceDirectory>src/main/resources/data</sourceDirectory>
                            
                            <!-- File patterns to include (default: **/*.json,**/*.yaml,**/*.yml) -->
                            <includes>
                                <include>**/*.json</include>
                                <include>**/*.yaml</include>
                                <include>**/*.yml</include>
                            </includes>
                            
                            <!-- File patterns to exclude (optional) -->
                            <excludes>
                                <exclude>**/draft/**</exclude>
                                <exclude>**/temp/**</exclude>
                            </excludes>
                            
                            <!-- Fail build on validation error (default: true) -->
                            <failOnError>true</failOnError>
                            
                            <!-- JSON Schema version (default: V202012) -->
                            <!-- Options: V4, V6, V7, V201909, V202012 -->
                            <schemaVersion>V202012</schemaVersion>
                            
                            <!-- Optional: Map schema IDs to local files for $ref resolution -->
                            <schemaMappings>
                                <schemaMapping>http://example.com/schemas/=src/main/resources/schemas/</schemaMapping>
                            </schemaMappings>
                            
                            <!-- Fail when no files found (default: true) -->
                            <failOnNoFilesFound>true</failOnNoFilesFound>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

## Running the Plugin

```bash
mvn json-yaml-validator:validate
```

Or as part of the build lifecycle:

```bash
mvn validate
```

### Skipping Validation

You can skip validation in two ways:

1. Via command line:
```bash
mvn validate -Dschema.validator.skip=true
```

2. Via configuration:
```xml
<configuration>
    <skip>true</skip>
    <!-- other configuration -->
</configuration>
```

### Controlling Behavior When No Files Are Found

By default, the plugin will fail the build if no files match the include patterns. This helps catch configuration errors early. You can control this behavior with the `failOnNoFilesFound` parameter:

```xml
<configuration>
    <!-- Set to false to allow validation to pass when no files are found -->
    <failOnNoFilesFound>false</failOnNoFilesFound>
    <!-- other configuration -->
</configuration>
```

This is useful in scenarios where:
- Files might not exist in certain environments
- You're using the plugin in a multi-module project where some modules might not have files to validate
- You want validation to be optional based on file availability