# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

This is a Maven plugin for validating JSON and YAML files against JSON Schema. It uses the networknt/json-schema-validator library and supports multiple JSON Schema versions (Draft 4, 6, 7, 2019-09, 2020-12).

## Build and Test Commands

### Build
```bash
mvn clean install
```

### Run Tests
```bash
mvn test
```

### Run a Single Test
```bash
mvn test -Dtest=TestClassName
mvn test -Dtest=TestClassName#methodName
```

### Run Plugin
```bash
mvn json-yaml-validator:validate
```

### Skip Validation
```bash
mvn validate -Dschema.validator.skip=true
```

## Architecture

### Core Components

1. **JsonYamlValidatorMojo** (`src/main/java/com/dataliquid/maven/plugin/schema/validator/JsonYamlValidatorMojo.java`)
   - Main plugin implementation extending AbstractMojo
   - Handles file discovery using includes/excludes patterns
   - Validates JSON/YAML files against schema
   - Supports schema mappings for $ref resolution
   - Configurable failure behavior

### Key Features

- **Schema Version Support**: Configurable via `schemaVersion` parameter (V4, V6, V7, V201909, V202012)
- **File Pattern Matching**: Uses glob patterns for includes/excludes
- **Schema Mappings**: Maps schema IDs to local files/directories for $ref resolution
- **Validation Control**: `failOnError` and `failOnNoFilesFound` parameters

### Test Structure

Tests are organized by JSON Schema version:
- `JsonYamlValidatorMojoV4Test.java`
- `JsonYamlValidatorMojoV6Test.java`
- `JsonYamlValidatorMojoV7Test.java`
- `JsonYamlValidatorMojoV201909Test.java`
- `JsonYamlValidatorMojoV202012Test.java`
- `JsonYamlValidatorMojoFailOnNoFilesTest.java`

Test resources:
- `src/test/resources/schemas/` - Test schemas for each version
- `src/test/resources/test-data/` - Valid/invalid test files
- `src/test/resources/test-poms/` - Test POM configurations

### Maven Plugin Development

- Uses Maven Plugin API with annotations
- Goal prefix: `json-yaml-validator`
- Default phase: `validate`
- Test POMs are filtered during build to inject current version