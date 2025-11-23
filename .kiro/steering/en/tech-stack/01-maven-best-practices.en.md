---
inclusion: manual
---

# Maven Best Practices

## Progressive Development Principle

### Core Principles

In multi-module Maven projects, adopt a progressive development strategy to avoid declaring modules that have not yet been created.

**Basic Principles**:
- Only declare created modules in the parent POM
- Update parent POM synchronously when creating new modules
- Follow the same principle for multi-level structures

### Why It's Important

- **Avoid build errors**: Pre-declaring non-existent modules will cause build failures
- **Keep configuration in sync with actual structure**: Ensure POM configuration reflects the real project structure
- **Support incremental development**: Project remains buildable at each stage

### Implementation Steps

1. **Confirm current state**: Check that parent POM only contains created modules
2. **Create new module**: Create module directory and pom.xml
3. **Update parent POM**: Add new module declaration in parent POM
4. **Verify build**: Run build command to ensure build succeeds

### Multi-level Module Handling

For multi-level module structures (such as application with application-api and application-impl), follow the same progressive principle:
- Parent module POM only declares created child modules
- Update parent module POM after creating new child modules

## Project Build Verification

### Key Requirements

**After completing each task, the entire project must be successfully buildable.**

### Why It's Important

- Project always remains in runnable state
- Discover integration issues promptly
- Support continuous integration and continuous delivery
- Reduce late-stage integration risks

### Verification Methods

1. **Basic build verification**: `mvn clean compile`
2. **Complete build verification**: `mvn clean package`
3. **Test verification**: `mvn clean test`

### Handling Build Failures

**Common Causes**:
- Module declarations inconsistent with actual structure
- Incorrect dependency configuration
- Version incompatibility
- Code syntax errors

**Response Strategies**:
- Check if module declarations in parent POM are correct
- Confirm dependency versions are compatible
- Check for code syntax errors
- Review build logs to locate specific issues

## Dependency Management

### Dependency Version Management Principles

- Use `<dependencyManagement>` in parent POM to unify dependency version management
- Child modules referencing other modules don't need to specify versions (managed by parent POM)
- Maintain consistency and control of dependency versions

### ⚠️ Dependency Change Confirmation Mechanism (Mandatory)

**Core Principle**: Prohibited from modifying inter-module dependencies without permission; must confirm with user when introducing new dependencies.

#### Situations Requiring Confirmation

The following situations **must** be confirmed with the user first and obtain explicit approval before execution:

1. **Introducing new external dependencies**
   - Adding new third-party libraries or frameworks
   - Introducing new Spring Boot Starters
   - Adding new utility libraries

2. **Modifying inter-module dependencies**
   - Adding new dependency from module A to module B
   - Removing existing inter-module dependencies
   - Adjusting dependency scope (compile, provided, test, etc.)

3. **Changing dependency versions**
   - Upgrading or downgrading dependency versions
   - Modifying version management in parent POM

4. **Introducing transitive dependencies**
   - Libraries that may bring many transitive dependencies
   - Libraries that may conflict with existing dependencies

#### Confirmation Process

**Step 1: Identify dependency change requirement**
```
I notice the need for [specific dependency needed], because [reason].
```

**Step 2: Describe dependency details**
```
Dependency information:
- GroupId: [groupId]
- ArtifactId: [artifactId]
- Version: [version]
- Scope: [compile/provided/test]

Impact scope:
- Affected modules: [list affected modules]
- Transitive dependencies: [whether it will introduce many transitive dependencies]
- Potential conflicts: [whether it may conflict with existing dependencies]
```

**Step 3: Wait for user confirmation**
```
Please confirm if this dependency can be introduced?
```

**Step 4: Execute changes**
- Only modify POM files after explicit user approval
- Verify build succeeds immediately after modification
- Report to user promptly if issues arise

#### Prohibited Behaviors

- ❌ Don't introduce new dependencies without user confirmation
- ❌ Don't modify inter-module dependencies without permission
- ❌ Don't assume user will agree to dependency changes
- ❌ Don't "conveniently" add dependencies during task execution

#### Exception Cases

The following situations don't require confirmation (but still need to inform user after task completion):
- Dependencies explicitly specified in design documents
- Dependencies explicitly required in task descriptions
- Dependencies already present in project (not new additions)

### Why Confirmation Mechanism Is Needed

1. **Architecture consistency**: Avoid introducing dependencies inconsistent with architecture design
2. **Version control**: Avoid dependency version conflicts and incompatibilities
3. **Security**: Avoid introducing dependencies with security vulnerabilities
4. **License compliance**: Avoid introducing dependencies with incompatible licenses
5. **Project maintainability**: Avoid project bloat from too many dependencies
6. **Team collaboration**: Ensure team has awareness of dependency changes

## Common Issues

### Issue 1: Module Not Found

**Error message**:
```
[ERROR] Child module xxx of yyy does not exist
```

**Cause**: Parent POM declares non-existent module

**Solution**:
- Check if module directory exists
- Check if module path is correct
- If module not yet created, remove declaration from parent POM

### Issue 2: Circular Dependency

**Error message**:
```
[ERROR] The projects in the reactor contain a cyclic reference
```

**Cause**: Circular dependencies exist between modules

**Solution**:
- Redesign module dependency relationships
- Extract common parts into independent module
- Use interfaces to isolate dependencies

### Issue 3: Version Conflict

**Error message**:
```
[WARNING] Some problems were encountered while building the effective model
```

**Cause**: Different modules introduce different versions of the same dependency

**Solution**:
- Unify versions in parent POM's `<dependencyManagement>`
- Use `mvn dependency:tree` to view dependency tree
- Use `<exclusions>` to exclude conflicting transitive dependencies

## Checklist

After completing module creation or modification, confirm:

- [ ] Module declarations in parent POM consistent with actual directory structure
- [ ] All declared modules have been created
- [ ] Project can build successfully (`mvn clean compile`)
- [ ] No build errors or warnings
- [ ] Dependency relationships configured correctly
- [ ] Version management unified in parent POM
- [ ] **All new or modified dependencies have obtained user confirmation**
- [ ] **Reasons and impacts of dependency changes have been explained to user**

## Summary

Core principles of Maven multi-module projects are **progressive development**, **continuous buildability**, and **dependency change confirmation**:

1. **Only declare created modules**
2. **Update configuration synchronously when creating modules**
3. **Verify build after each task completion**
4. **Keep project always in healthy state**
5. **⚠️ Dependency changes must be confirmed with user first**

Following these principles can:
- ✅ Avoid build errors
- ✅ Support incremental development
- ✅ Reduce integration risks
- ✅ Improve development efficiency
- ✅ Maintain architecture consistency
- ✅ Avoid dependency conflicts and security risks
