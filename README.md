**This project is not fully completed. Any harm caused by this program is at your own risk**


# MDir_FX
MDir_FX helps organize pictures and videos to an external hard drive.
The project has been written with OpenJDK 14 and OpenJFX 14.

** Features **
* Date and time editor
* Avoid duplicates to save space
* Batch copy


External libraries used in this project

##### Metadata-extractor 2.14.0
https://github.com/drewnoakes/metadata-extractor

##### JCodec 0.2.3
https://github.com/jcodec/jcodec

##### VLCJ 4.5.2
https://github.com/caprica/vlcj

##### TwelveMonkeys
###### imageio-jpeg 3.5
###### imageio-tiff 3.5
https://github.com/haraldk/TwelveMonkeys

Supported file formats for thumbnail creation

__Video__
- 3GP
- AVI
- MKV
- MOV
- MP4
- MPG

 __Picture__
- BMP
- GIF
- JPG
- JPEG
- PNG
- TIF
- TIFF

__Raw__
- CR2
- NEF

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
