# Compilation & Build Verification Guidelines

## 1. Mandatory Compilation Checks
- **Command**: Always verify code changes by running:
  ```powershell
  .\gradlew compileDebugKotlin --no-daemon
  ```
- **Execution Rule**: Never mark a task as completed without validating that the Kotlin compiler finishes with `BUILD SUCCESSFUL` (exit code 0).

## 2. Windows Daemon & File Lock Handling
- On Windows systems, Gradle background daemons can intermittently hold open file handles to intermediate build artifacts (e.g., `R.jar`, KSP cache).
- **Resolution**:
  - Always pass `--no-daemon` when invoking gradle commands.
  - If a file lock occurs (`java.io.IOException: Couldn't delete ... R.jar`), stop any running Gradle daemons with:
    ```powershell
    .\gradlew --stop
    ```
  - If necessary, remove the locked directory and re-run:
    ```powershell
    Remove-Item -Force -Recurse 'app\build\intermediates\compile_and_runtime_r_class_jar' -ErrorAction SilentlyContinue
    ```

## 3. Testing & Regression Prevention
- Run unit tests after major refactors:
  ```powershell
  .\gradlew testDebugUnitTest --no-daemon
  ```
- Ensure zero breaking changes to existing ViewModels, Room database entities, and BLE state management.

## 4. Compose API & Dependency Integrity
- Always use API versions compatible with the current Jetpack Compose version in the project.
- For animations triggered by state changes (e.g. filters), prefer `Animatable` with `LaunchedEffect` over unsupported or deprecated parameters.
- Verify that all Canvas extension functions (such as `clipRect`) are properly imported from `androidx.compose.ui.graphics.drawscope.*`.
