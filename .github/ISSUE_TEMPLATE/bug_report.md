---
name: Bug report
about: Create a report to help us improve
title: '[BUG] '
labels: bug
assignees: ''

---

**Describe the bug**
A clear and concise description of what the bug is.

**To Reproduce**
Steps to reproduce the behavior:

1. Configure plugin with '...'
2. Run command '...'
3. See error

**Expected behavior**
A clear and concise description of what you expected to happen.

**Error messages**
If applicable, add error messages or stack traces.

```
Paste error output here
```

**Configuration**
Please provide your plugin configuration:

```xml
<plugin>
    <groupId>com.example</groupId>
    <artifactId>json-yaml-validator-maven-plugin</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <configuration>
        <!-- Your configuration here -->
    </configuration>
</plugin>
```

**Environment (please complete the following information):**
- OS: [e.g. Ubuntu 20.04, Windows 10, macOS 12]
- Java Version: [e.g. 11, 17]
- Maven Version: [e.g. 3.8.1]
- Plugin Version: [e.g. 1.0.0-SNAPSHOT]

**Sample Files**
If applicable, provide sample JSON/YAML files and schemas that reproduce the issue.

**JSON Schema:**
```json
{
  "type": "object",
  "properties": {
    ...
  }
}
```

**File being validated:**
```json
{
  ...
}
```

**Additional context**
Add any other context about the problem here.