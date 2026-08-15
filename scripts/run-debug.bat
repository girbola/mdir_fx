@echo off
REM MDir Image ^& Video Organizer - Debug launcher (module-path)
setlocal
set "SCRIPT_DIR=%~dp0"
for %%I in ("%SCRIPT_DIR%..") do set "REPO_ROOT=%%~fI"
set "TARGET_DIR=%REPO_ROOT%\target"
set "CLASSES_DIR=%TARGET_DIR%\classes"
set "MODULE_DEPS_DIR=%TARGET_DIR%\module-deps"
set "SKIP_TESTS="
if /I "%~1"=="-SkipTests" set "SKIP_TESTS=-DskipTests"

echo [debug] Building project with Maven (debug-modulepath profile)
call mvn -Pdebug-modulepath package %SKIP_TESTS%
if errorlevel 1 (
    echo.
    echo Error: Maven build failed.
    exit /b 1
)

if not exist "%CLASSES_DIR%" (
    echo Error: Compiled classes directory not found: "%CLASSES_DIR%"
    exit /b 1
)
if not exist "%MODULE_DEPS_DIR%" (
    echo Error: Module dependency directory not found: "%MODULE_DEPS_DIR%"
    exit /b 1
)

echo [debug] Launching module com.girbola/com.girbola.Launcher
java --enable-native-access=javafx.graphics --enable-native-access=ALL-UNNAMED --module-path "%CLASSES_DIR%;%MODULE_DEPS_DIR%" -m com.girbola/com.girbola.Launcher
set "EXIT_CODE=%ERRORLEVEL%"
endlocal & exit /b %EXIT_CODE%

