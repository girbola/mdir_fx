# MDir_FX — Smart Media Organizer

MDir_FX is a media organization tool designed to help manage photos and videos on external storage devices. It provides features such as duplicate detection, batch copying, and date/time editing.

> **Warning**  
> This project is currently under development and is not yet ready for public use. Use it at your own risk. The author is not responsible for any data loss, file corruption, or other damage caused by using this software.

## Overview

MDir_FX helps organize pictures and videos, especially when transferring or maintaining media collections on external hard drives.

The project is developed using:

- OpenJDK 24
- OpenJFX 25.0.1

## Features

- Date and time editor
- Duplicate detection to help save storage space
- Batch copy functionality
- Support for common image, video, and RAW file formats

## License

This project is not currently ready for public release. The intended license for this project is **GPLv3**.

## Third-Party Repositories

| Library                | License            | Project URL                                      | Notes                               |
|------------------------|--------------------|--------------------------------------------------|-------------------------------------|
| Apache Commons Imaging | Apache License 2.0 | https://github.com/apache/commons-imaging        | Previously Sanselan                 |
| Ikonli                 | Apache License 2.0 | https://github.com/kordamp/ikonli                | —                                   |
| JavaCV                 | Apache License 2.0 | https://github.com/bytedeco/javacv               | Version not specified               |
| Metadata Extractor     | Apache License 2.0 | https://github.com/drewnoakes/metadata-extractor | —                                   |
| OpenJFX                | GPLv2              | https://github.com/openjdk/jfx                   | —                                   |
| OSHI                   | MIT License        | https://github.com/oshi/oshi                     | —                                   |
| TwelveMonkeys ImageIO  | BSD 3-Clause       | https://github.com/haraldk/TwelveMonkeys         | Modules: imageio-jpeg, imageio-tiff |
| VLCJ                   | GPLv3              | https://github.com/caprica/vlcj                  | —                                   |

## Supported File Formats

| Category | Formats                                             |
|----------|-----------------------------------------------------|
| Video    | 3GP, AVI, MKV, MOV, MP4, MPG                        |
| Image    | BMP, GIF, HEIC(not tested), JPG/JPEG, PNG, TIF/TIFF |
| RAW      | CR2, DNG, NEF(not tested)                           |

## Development Status

MDir_FX is still under active development and should be considered experimental.

Use this software carefully, especially when working with important media files. It is strongly recommended to keep backups before copying, editing, or organizing files with this application.

## Compiling the Project

### Required Versions

- GraalVM 22.0.2
- JavaFX 24.0.2

### IntelliJ IDEA Module Configuration

The IntelliJ IDEA `.iml` module file should look similar to the following:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<module version="4">
  <component name="AdditionalModuleElements">
    <content url="file://$MODULE_DIR$" dumb="true">
      <sourceFolder url="file://$MODULE_DIR$/src/main/resources/bundle" type="java-resource" />
      <sourceFolder url="file://$MODULE_DIR$/src/main/resources/fonts" type="java-resource" />
      <sourceFolder url="file://$MODULE_DIR$/src/main/resources/img" type="java-resource" />
      <sourceFolder url="file://$MODULE_DIR$/src/main/resources/sql" type="java-resource" />
      <sourceFolder url="file://$MODULE_DIR$/src/main/resources/themes" type="java-resource" />
    </content>
  </component>
</module>
```

### Release build scripts

If you want to recreate the distributable release folder on another computer, use:

- `scripts/build-release.ps1` on Windows
- `scripts/build-release.sh` on macOS/Linux

These scripts build the project, copy runtime dependencies, create the `release/` folder, and optionally zip it.

## JavaFX Scene Builder 24.0.1
Libraries need to be installed manually:
```
ikonli-bootstrapicons-pack-12.3.1.jar
org.kordamp.ikonli:ikonli-bootstrapicons-pack:12.4.0
org.kordamp.ikonli:ikonli-core:12.4.0
org.kordamp.ikonli:ikonli-ionicons4-pack:12.4.0
org.kordamp.ikonli:ikonli-javafx:12.4.0
org.kordamp.ikonli:ikonli-material2-pack:12.4.0
org.kordamp.ikonli:ikonli-materialdesign2-pack:12.4.0

```