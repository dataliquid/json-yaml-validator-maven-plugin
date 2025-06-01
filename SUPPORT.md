# Support

## How to Get Help

Thank you for using the JSON/YAML Validator Maven Plugin! This document explains how to get help with the project.

## Documentation

Before seeking help, please check:

1. [README.md](README.md) - Basic usage and configuration
2. [CONTRIBUTING.md](CONTRIBUTING.md) - Development setup and guidelines
3. [Maven Plugin Documentation](https://maven.apache.org/plugin-developers/) - General Maven plugin information

## Getting Help

### 🐛 Bug Reports

If you've found a bug:

1. **Check existing issues** to see if it's already reported
2. **Create a new issue** with:
   - Clear description of the problem
   - Steps to reproduce
   - Expected vs actual behavior
   - Plugin version
   - Maven version
   - Java version
   - Sample configuration and files (if applicable)

### 💡 Feature Requests

For new features:

1. **Check existing issues** for similar requests
2. **Create a feature request** with:
   - Use case description
   - Proposed solution
   - Alternative solutions considered
   - Example configuration

### ❓ Questions

For general questions:

1. **Check the FAQ** section below
2. **Search closed issues** - your question might be answered
3. **Create a discussion** or issue with the `question` label

## Frequently Asked Questions (FAQ)

### Q: Which JSON Schema versions are supported?
A: The plugin supports Draft 4, 6, 7, 2019-09, and 2020-12. Default is 2020-12.

### Q: Can I validate multiple schemas in one build?
A: Currently, the plugin supports one schema per execution. You can configure multiple executions for multiple schemas.

### Q: Why is my YAML file not being validated?
A: Check that:
- The file extension is `.yaml` or `.yml`
- The file path matches your include patterns
- The file is not excluded by your exclude patterns

### Q: How do I validate files in multiple directories?
A: Use multiple executions with different `sourceDirectory` configurations, or use include patterns like `**/data/**/*.json`.

### Q: Can I use schema references ($ref)?
A: Yes, the plugin supports JSON Schema references, including external file references.

## Response Times

- **Critical bugs**: We aim to respond within 48 hours
- **Feature requests**: Review within 1 week
- **Questions**: Best effort, typically within 1 week

## Commercial Support

Currently, this is a community-supported project. For critical business needs, consider:

1. Contributing fixes directly
2. Sponsoring specific features
3. Hiring consultants familiar with the project

## Contributing

The best way to get issues resolved is to contribute! See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

## Security Issues

For security vulnerabilities, please **DO NOT** create public issues. Instead:

1. Email the maintainers directly (if contact info is available)
2. Use GitHub's private vulnerability reporting (if enabled)
3. Include detailed information about the vulnerability

## Community

- **GitHub Discussions**: For general discussions and questions
- **Issues**: For bugs and feature requests
- **Pull Requests**: For contributing code

## Helpful Resources

- [JSON Schema Documentation](https://json-schema.org/)
- [Maven Plugin Development](https://maven.apache.org/plugin-developers/)
- [networknt/json-schema-validator](https://github.com/networknt/json-schema-validator)

Thank you for using the JSON/YAML Validator Maven Plugin!