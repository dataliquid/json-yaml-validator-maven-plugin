# Contributing to JSON/YAML Validator Maven Plugin

Thank you for your interest in contributing to the JSON/YAML Validator Maven Plugin! This document provides guidelines and instructions for contributing.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [Making Changes](#making-changes)
- [Submitting Changes](#submitting-changes)
- [Coding Standards](#coding-standards)
- [Testing Guidelines](#testing-guidelines)
- [Documentation](#documentation)

## Code of Conduct

Please read and follow our [Code of Conduct](CODE_OF_CONDUCT.md) to ensure a welcoming environment for all contributors.

## Getting Started

1. Fork the repository on GitHub
2. Clone your fork locally
3. Create a new branch for your feature or bugfix
4. Make your changes
5. Submit a pull request

## Development Setup

### Prerequisites

- Java 11 or higher
- Maven 3.6.0 or higher
- Git

### Building the Project

```bash
# Clone your fork
git clone https://github.com/YOUR_USERNAME/json-yaml-validator-maven-plugin.git
cd json-yaml-validator-maven-plugin

# Build the project
mvn clean install

# Run tests
mvn test
```

## Making Changes

### Branch Naming Convention

- Feature branches: `feature/description-of-feature`
- Bugfix branches: `fix/description-of-bug`
- Documentation: `docs/description-of-change`

### Commit Messages

Follow the conventional commits specification:

- `feat:` - New features
- `fix:` - Bug fixes
- `docs:` - Documentation changes
- `test:` - Test additions or modifications
- `refactor:` - Code refactoring
- `chore:` - Maintenance tasks

Example:
```
feat: add support for JSON Schema draft 2020-12
fix: handle empty YAML files correctly
docs: update README with new configuration options
```

## Submitting Changes

### Pull Request Process

1. Ensure all tests pass: `mvn test`
2. Update documentation if needed
3. Add tests for new functionality
4. Update CHANGELOG.md with your changes
5. Create a pull request with a clear description

### Pull Request Template

When creating a PR, please include:

- **Description**: What does this PR do?
- **Type**: Bug fix, feature, documentation, etc.
- **Breaking Changes**: Yes/No
- **Testing**: How has this been tested?
- **Checklist**:
  - [ ] Tests pass
  - [ ] Documentation updated
  - [ ] CHANGELOG.md updated
  - [ ] Code follows project style

## Coding Standards

### Java Code Style

- Use 4 spaces for indentation (no tabs)
- Maximum line length: 120 characters
- Use meaningful variable and method names
- Add Javadoc for all public methods
- Follow standard Java naming conventions

### Example Code Style

```java
/**
 * Validates JSON/YAML files against a schema.
 * 
 * @param file the file to validate
 * @param schema the JSON schema
 * @return validation result
 * @throws ValidationException if validation fails
 */
public ValidationResult validateFile(File file, JsonSchema schema) 
        throws ValidationException {
    // Implementation
}
```

## Testing Guidelines

### Unit Tests

- Write unit tests for all new functionality
- Maintain test coverage above 80%
- Use descriptive test method names
- Test both success and failure cases

### Test Structure

```java
@Test
public void validateFile_withValidJson_shouldReturnSuccess() {
    // Given
    File validFile = new File("valid.json");
    
    // When
    ValidationResult result = validator.validateFile(validFile, schema);
    
    // Then
    assertTrue(result.isValid());
}
```

### Integration Tests

- Test plugin execution with different configurations
- Test various JSON Schema versions
- Test error scenarios

## Documentation

### Code Documentation

- Add Javadoc to all public classes and methods
- Include @param, @return, and @throws tags
- Provide examples in complex methods

### User Documentation

- Update README.md for new features
- Add examples for new configuration options
- Document breaking changes clearly

## Questions and Support

If you have questions or need help:

1. Check existing issues on GitHub
2. Read the documentation
3. Create a new issue with the question label
4. Join our discussions (if available)

## Recognition

Contributors will be recognized in:
- The project's contributors list
- Release notes for their contributions
- Special mentions for significant contributions

Thank you for contributing to the JSON/YAML Validator Maven Plugin!