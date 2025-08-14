# MDir_FX — Smart Media Organizer

Organize photos and videos effortlessly with deduplication, batch copy, and timestamp editing.

## **This project is not yet complete. Use at your own risk; you are responsible for any damage caused.**
# MDir_FX
MDir_FX helps organize pictures and videos to an external hard drive.
The project has been written with OpenJDK 14 and OpenJFX 14.

** Features **
* Date and time editor
* Avoid duplicates to save space
* Batch copy

### License
This program is not ready for public use, but  GPLv3 will be used.

### 📦 Third-Party repositories

| Library               | Version    | License                                  | Project URL                                           | Notes                          |
|-----------------------|------------|-------------------------------------------|--------------------------------------------------------|---------------------------------|
| Metadata-extractor    | 2.14.0     | Apache License 2.0                        | https://github.com/drewnoakes/metadata-extractor       | —                               |
| JavaCV                | 1.5.10     | Apache License 2.0                        | https://github.com/bytedeco/javacv                     | Version not specified           |
| VLCJ                  | 4.5.2      | GPLv3                                     | https://github.com/caprica/vlcj                        | —                               |
| TwelveMonkeys ImageIO | 3.5        | BSD 3-Clause                              | https://github.com/haraldk/TwelveMonkeys               | Modules: imageio-jpeg, imageio-tiff |

</br>
</br>

## Supported file formats

| Category  | Formats                               |
|-----------|----------------------------------------|
| 🎬 Video  | 3GP • AVI • MKV • MOV • MP4 • MPG      |
| 🖼️ Picture | BMP • GIF • JPG/JPEG • PNG • TIF/TIFF  |
| 📷 Raw    | CR2 • NEF                              |

This project is not yet ready for public use.
Any inconvenience caused by this project is in your responsibility.

## Compiling the project
### Required versions
GraalVM 22.0.2
JavaFX 24.0.2

``` IntelliJ Module .iml file should look something like this
<?xml version="1.0" encoding="UTF-8"?>
<module version="4">
  <component name="AdditionalModuleElements">
    <content url="file://$MODULE_DIR$" dumb="true">
      <sourceFolder url="file://$MODULE_DIR$/src/main/java/com/girbola/fxml" type="java-resource" />
      <sourceFolder url="file://$MODULE_DIR$/src/main/resources/bundle" type="java-resource" />
      <sourceFolder url="file://$MODULE_DIR$/src/main/resources/fonts" type="java-resource" />
      <sourceFolder url="file://$MODULE_DIR$/src/main/resources/img" type="java-resource" />
      <sourceFolder url="file://$MODULE_DIR$/src/main/resources/themes" type="java-resource" />
      <sourceFolder url="file://$MODULE_DIR$/src/test" isTestSource="true" />
      <excludeFolder url="file://$MODULE_DIR$/.ideaold" />
    </content>
  </component>
</module>
```

### Compiling
#### JVM options
```
--module-path C:\Programs\javafx-sdk-24.0.2\lib --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base,javafx.media,javafx.swing
```
