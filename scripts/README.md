# Release Build Scripts

Use these scripts to build a distributable release folder on the computer where you're running the build.

## Windows

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\build-release.ps1 -SkipTests
```

## macOS / Linux

```bash
chmod +x scripts/build-release.sh
./scripts/build-release.sh
```

## Debug run scripts

Use these when you want to launch the modular app without relying on Maven's exec plugin.

### Windows

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run-debug.ps1 -SkipTests
```

or:

```bat
scripts\run-debug.bat -SkipTests
```

### macOS / Linux

```bash
chmod +x scripts/run-debug.sh
./scripts/run-debug.sh -SkipTests
```

## What they do

- run `mvn -Prelease clean package` (activates the `release` Maven profile)
- the `release` profile binds `dependency:copy-dependencies` to the `prepare-package` phase → libs land in `target/release-lib/` **only during a release build** (normal `mvn javafx:run` is unaffected)
- copy the built JAR into `release/MDir_vX.Y.Z/app/mdir.jar`
- copy runtime dependencies from `target/release-lib/` into `release/MDir_vX.Y.Z/app/lib/`
- generate launcher scripts in `release/MDir_vX.Y.Z/bin/`
- write a small release `README.md`
- optionally create a ZIP archive

## Notes

- The release output folder is ignored by git (`/release/` is in `.gitignore`).
- The generated package is OS-specific because JavaFX native dependencies are platform-specific.
- If you want to build for macOS, run the script on macOS so Maven resolves the mac JavaFX artifacts.
- The debug scripts run `mvn -Pdebug-modulepath package`, then start `com.girbola/com.girbola.Launcher` with `java --module-path target/classes + target/module-deps`.

