# Include/Exclude patterns not working correctly in file discovery

## Description
The file discovery mechanism in `JsonYamlValidatorMojo` has a bug where include/exclude patterns are not applied correctly. This results in files being incorrectly included or excluded during validation.

## Current Behavior
- Files that should be excluded (e.g., in `draft/` or `temp/` directories) are being included
- Files that should be included are sometimes missing
- The pattern matching logic does not correctly handle Ant-style glob patterns like `**/*.json` or `**/draft/**`

## Expected Behavior
- Include patterns like `**/*.json`, `**/*.yaml`, `**/*.yml` should match files at any directory level
- Exclude patterns like `**/draft/**`, `**/temp/**`, `**/*.backup.json` should exclude matching files
- The file discovery should follow standard Ant-style pattern matching conventions

## Root Cause
The `matchesPattern()` method in `JsonYamlValidatorMojo` has flawed regex conversion logic:
- The order of string replacements causes `**` to be incorrectly converted
- The regex patterns don't properly handle directory traversal
- Pattern matching doesn't correctly distinguish between `*` (single level) and `**` (multiple levels)

## Test Case
The `JsonYamlValidatorMojoIncludeExcludeTest` demonstrates this issue. With the test data structure:
```
include-exclude-test/
├── config.json
├── data.backup.json
├── data.yaml
├── draft/
│   └── draft-config.json
├── level1/
│   ├── config.json
│   ├── level2/
│   │   ├── config.backup.json
│   │   ├── data.json
│   │   ├── draft/
│   │   │   └── draft.json
│   │   ├── level3/
│   │   │   ├── config.yml
│   │   │   └── deep.json
│   │   └── temp/
│   │       └── temp.yaml
│   └── normal/
│       └── normal.yaml
└── temp/
    └── temp-data.yml
```

With configuration:
- Includes: `**/*.json`, `**/*.yaml`, `**/*.yml`
- Excludes: `**/draft/**`, `**/temp/**`, `**/*.backup.json`

The test expects 7 files but the current implementation returns incorrect results.

## Impact
Users cannot reliably use include/exclude patterns to control which files are validated, making it difficult to:
- Exclude temporary or draft files from validation
- Exclude backup files
- Target specific file patterns for validation

## Proposed Solution
Implement proper Ant-style pattern matching using Apache Commons IO's file filtering capabilities with a custom `IOFileFilter` that correctly handles glob patterns.