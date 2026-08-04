# Third-Party Licenses

This file tracks dependency license metadata for `mdir_fx_2025`.

- Project license: **GNU GPL v3 or later** (`LICENSE`)
- License inventory source: Maven metadata (`pom.xml` + transitive dependencies)
- Generated on: 2026-07-30

## How to regenerate

```powershell
Set-Location "C:\Users\marko\git\mdir_fx_2\mdir_fx"
mvn -DskipTests org.codehaus.mojo:license-maven-plugin:2.7.0:add-third-party
```

The raw generated report is created at:

- `target/generated-sources/license/THIRD-PARTY.txt`

## Compatibility notes for this project

- GPLv3-or-later is compatible with permissive licenses used here (Apache-2.0, MIT, BSD-3-Clause).
- OpenJFX artifacts are GPLv2 with Classpath Exception, which is typically compatible for this use.
- JavaCV/JavaCPP preset artifacts publish dual metadata (Apache-2.0 and GPLv2+CE) and are commonly consumed with GPL-compatible distributions.
- VLCJ artifacts are GPLv3, which is aligned with this project's GPLv3-or-later license.

## Snapshot (from generated report)

```text
Lists of 82 third-party dependencies.
     (The BSD 3-Clause License (BSD3)) Adobe XMPCore (com.adobe.xmp:xmpcore:6.1.11 - https://www.adobe.com/devnet/xmp/library/eula-xmp-library-java.html)
     (The Apache Software License, Version 2.0) com.drewnoakes:metadata-extractor (com.drewnoakes:metadata-extractor:2.19.0 - https://drewnoakes.com/code/exif/)
     (MIT) oshi-core (com.github.oshi:oshi-core:6.9.2 - https://github.com/oshi/oshi/oshi-core)
     (The BSD License) TwelveMonkeys :: Common :: Image (com.twelvemonkeys.common:common-image:3.12.0 - https://github.com/haraldk/TwelveMonkeys/common/common-image)
     (The BSD License) TwelveMonkeys :: Common :: IO (com.twelvemonkeys.common:common-io:3.12.0 - https://github.com/haraldk/TwelveMonkeys/common/common-io)
     (The BSD License) TwelveMonkeys :: Common :: Language support (com.twelvemonkeys.common:common-lang:3.12.0 - https://github.com/haraldk/TwelveMonkeys/common/common-lang)
     (The BSD License) TwelveMonkeys :: ImageIO :: Core (com.twelvemonkeys.imageio:imageio-core:3.12.0 - https://github.com/haraldk/TwelveMonkeys/tree/master/imageio/imageio-core)
     (The BSD License) TwelveMonkeys :: ImageIO :: JPEG plugin (com.twelvemonkeys.imageio:imageio-jpeg:3.12.0 - https://github.com/haraldk/TwelveMonkeys/tree/master/imageio/imageio-jpeg)
     (The BSD License) TwelveMonkeys :: ImageIO :: Metadata (com.twelvemonkeys.imageio:imageio-metadata:3.12.0 - https://github.com/haraldk/TwelveMonkeys/tree/master/imageio/imageio-metadata)
     (The BSD License) TwelveMonkeys :: ImageIO :: TIFF plugin (com.twelvemonkeys.imageio:imageio-tiff:3.12.0 - https://github.com/haraldk/TwelveMonkeys/tree/master/imageio/imageio-tiff)
     (Apache-2.0) Apache Commons IO (commons-io:commons-io:2.19.0 - https://commons.apache.org/proper/commons-io/)
     (Apache License, Version 2.0) Byte Buddy (without dependencies) (net.bytebuddy:byte-buddy:1.17.7 - https://bytebuddy.net/byte-buddy)
     (Apache License, Version 2.0) Byte Buddy agent (net.bytebuddy:byte-buddy-agent:1.17.7 - https://bytebuddy.net/byte-buddy-agent)
     (MIT License) thumbnailator (net.coobird:thumbnailator:0.4.21 - https://github.com/coobird/thumbnailator)
     (Apache-2.0) (LGPL-2.1-or-later) Java Native Access (net.java.dev.jna:jna:5.18.1 - https://github.com/java-native-access/jna)
     (Apache-2.0) (LGPL-2.1-or-later) Java Native Access Platform (net.java.dev.jna:jna-platform:5.18.1 - https://github.com/java-native-access/jna)
     (Apache-2.0) Apache Commons Imaging (org.apache.commons:commons-imaging:1.0.0-alpha6 - https://commons.apache.org/proper/commons-imaging/)
     (Apache-2.0) Apache Commons Lang (org.apache.commons:commons-lang3:3.17.0 - https://commons.apache.org/proper/commons-lang/)
     (Apache-2.0) Apache Log4j API (org.apache.logging.log4j:log4j-api:2.23.1 - https://logging.apache.org/log4j/2.x/log4j/log4j-api/)
     (Apache-2.0) Apache Log4j Core (org.apache.logging.log4j:log4j-core:2.23.1 - https://logging.apache.org/log4j/2.x/log4j/log4j-core/)
     (Apache-2.0) Apache Log4j SLF4J 2.0 Binding (org.apache.logging.log4j:log4j-slf4j2-impl:2.23.1 - https://logging.apache.org/log4j/2.x/log4j/log4j-slf4j2-impl/)
     (The Apache License, Version 2.0) org.apiguardian:apiguardian-api (org.apiguardian:apiguardian-api:1.1.2 - https://github.com/apiguardian-team/apiguardian)
     (Apache License, Version 2.0) (GNU General Public License (GPL) version 2, or any later version) (GPLv2 with Classpath exception) JavaCPP Presets for ARToolKitPlus (org.bytedeco:artoolkitplus:2.3.1-1.5.9 - http://bytedeco.org/javacpp-presets/artoolkitplus/)
     (Apache License, Version 2.0) (GNU General Public License (GPL) version 2, or any later version) (GPLv2 with Classpath exception) JavaCPP Presets Platform for ARToolKitPlus (org.bytedeco:artoolkitplus-platform:2.3.1-1.5.9 - http://bytedeco.org/javacpp-presets/artoolkitplus-platform/)
     (Apache License, Version 2.0) (GNU General Public License (GPL) version 2, or any later version) (GPLv2 with Classpath exception) JavaCPP Presets for FFmpeg (org.bytedeco:ffmpeg:8.0.1-1.5.13 - http://bytedeco.org/javacpp-presets/ffmpeg/)
     (Apache License, Version 2.0) (GNU General Public License (GPL) version 2, or any later version) (GPLv2 with Classpath exception) JavaCPP Presets Platform for FFmpeg (org.bytedeco:ffmpeg-platform:8.0.1-1.5.13 - http://bytedeco.org/javacpp-presets/ffmpeg-platform/)
     (Apache License, Version 2.0) (GNU General Public License (GPL) version 2, or any later version) (GPLv2 with Classpath exception) JavaCPP Presets for FlyCapture (org.bytedeco:flycapture:2.13.3.31-1.5.9 - http://bytedeco.org/javacpp-presets/flycapture/)
     (Apache License, Version 2.0) (GNU General Public License (GPL) version 2, or any later version) (GPLv2 with Classpath exception) JavaCPP Presets Platform for FlyCapture (org.bytedeco:flycapture-platform:2.13.3.31-1.5.9 - http://bytedeco.org/javacpp-presets/flycapture-platform/)
     (Apache License, Version 2.0) (GNU General Public License (GPL) version 2, or any later version) (GPLv2 with Classpath exception) JavaCPP (org.bytedeco:javacpp:1.5.13 - http://bytedeco.org/javacpp/)
     (Apache License, Version 2.0) (GNU General Public License (GPL) version 2, or any later version) (GPLv2 with Classpath exception) JavaCPP Platform (org.bytedeco:javacpp-platform:1.5.13 - http://bytedeco.org/javacpp/)
     (Apache License, Version 2.0) (GNU General Public License (GPL) version 2, or any later version) (GPLv2 with Classpath exception) JavaCV (org.bytedeco:javacv:1.5.13 - http://bytedeco.org/javacv/)
     (Apache License, Version 2.0) (GNU General Public License (GPL) version 2, or any later version) (GPLv2 with Classpath exception) JavaCV Platform (org.bytedeco:javacv-platform:1.5.13 - http://bytedeco.org/javacpp-presets/javacv-platform/)
     (Apache License, Version 2.0) (GNU General Public License (GPL) version 2, or any later version) (GPLv2 with Classpath exception) JavaCPP Presets for Leptonica (org.bytedeco:leptonica:1.87.0-1.5.13 - http://bytedeco.org/javacpp-presets/leptonica/)
     (Apache License, Version 2.0) (GNU General Public License (GPL) version 2, or any later version) (GPLv2 with Classpath exception) JavaCPP Presets Platform for Leptonica (org.bytedeco:leptonica-platform:1.87.0-1.5.13 - http://bytedeco.org/javacpp-presets/leptonica-platform/)
     (Apache License, Version 2.0) (GNU General Public License (GPL) version 2, or any later version) (GPLv2 with Classpath exception) JavaCPP Presets for libdc1394 (org.bytedeco:libdc1394:2.2.6-1.5.9 - http://bytedeco.org/javacpp-presets/libdc1394/)
     (Apache License, Version 2.0) (GNU General Public License (GPL) version 2, or any later version) (GPLv2 with Classpath exception) JavaCPP Presets Platform for libdc1394 (org.bytedeco:libdc1394-platform:2.2.6-1.5.9 - http://bytedeco.org/javacpp-presets/libdc1394-platform/)
     (Apache License, Version 2.0) (GNU General Public License (GPL) version 2, or any later version) (GPLv2 with Classpath exception) JavaCPP Presets for libfreenect (org.bytedeco:libfreenect:0.5.7-1.5.9 - http://bytedeco.org/javacpp-presets/libfreenect/)
     (Apache License, Version 2.0) (GNU General Public License (GPL) version 2, or any later version) (GPLv2 with Classpath exception) JavaCPP Presets Platform for libfreenect (org.bytedeco:libfreenect-platform:0.5.7-1.5.9 - http://bytedeco.org/javacpp-presets/libfreenect-platform/)
     (Apache License, Version 2.0) (GNU General Public License (GPL) version 2, or any later version) (GPLv2 with Classpath exception) JavaCPP Presets for libfreenect2 (org.bytedeco:libfreenect2:0.2.0-1.5.9 - http://bytedeco.org/javacpp-presets/libfreenect2/)
     (Apache License, Version 2.0) (GNU General Public License (GPL) version 2, or any later version) (GPLv2 with Classpath exception) JavaCPP Presets Platform for libfreenect2 (org.bytedeco:libfreenect2-platform:0.2.0-1.5.9 - http://bytedeco.org/javacpp-presets/libfreenect2-platform/)
     (Apache License, Version 2.0) (GNU General Public License (GPL) version 2, or any later version) (GPLv2 with Classpath exception) JavaCPP Presets for librealsense (org.bytedeco:librealsense:1.12.4-1.5.9 - http://bytedeco.org/javacpp-presets/librealsense/)
     (Apache License, Version 2.0) (GNU General Public License (GPL) version 2, or any later version) (GPLv2 with Classpath exception) JavaCPP Presets Platform for librealsense (org.bytedeco:librealsense-platform:1.12.4-1.5.9 - http://bytedeco.org/javacpp-presets/librealsense-platform/)
     (Apache License, Version 2.0) (GNU General Public License (GPL) version 2, or any later version) (GPLv2 with Classpath exception) JavaCPP Presets for librealsense2 (org.bytedeco:librealsense2:2.53.1-1.5.9 - http://bytedeco.org/javacpp-presets/librealsense2/)
     (Apache License, Version 2.0) (GNU General Public License (GPL) version 2, or any later version) (GPLv2 with Classpath exception) JavaCPP Presets Platform for librealsense2 (org.bytedeco:librealsense2-platform:2.53.1-1.5.9 - http://bytedeco.org/javacpp-presets/librealsense2-platform/)
     (Apache License, Version 2.0) (GNU General Public License (GPL) version 2, or any later version) (GPLv2 with Classpath exception) JavaCPP Presets for OpenBLAS (org.bytedeco:openblas:0.3.31-1.5.13 - http://bytedeco.org/javacpp-presets/openblas/)
     (Apache License, Version 2.0) (GNU General Public License (GPL) version 2, or any later version) (GPLv2 with Classpath exception) JavaCPP Presets Platform for OpenBLAS (org.bytedeco:openblas-platform:0.3.31-1.5.13 - http://bytedeco.org/javacpp-presets/openblas-platform/)
     (Apache License, Version 2.0) (GNU General Public License (GPL) version 2, or any later version) (GPLv2 with Classpath exception) JavaCPP Presets for OpenCV (org.bytedeco:opencv:4.13.0-1.5.13 - http://bytedeco.org/javacpp-presets/opencv/)
     (Apache License, Version 2.0) (GNU General Public License (GPL) version 2, or any later version) (GPLv2 with Classpath exception) JavaCPP Presets Platform for OpenCV (org.bytedeco:opencv-platform:4.13.0-1.5.13 - http://bytedeco.org/javacpp-presets/opencv-platform/)
     (Apache License, Version 2.0) (GNU General Public License (GPL) version 2, or any later version) (GPLv2 with Classpath exception) JavaCPP Presets for Tesseract (org.bytedeco:tesseract:5.5.2-1.5.13 - http://bytedeco.org/javacpp-presets/tesseract/)
     (Apache License, Version 2.0) (GNU General Public License (GPL) version 2, or any later version) (GPLv2 with Classpath exception) JavaCPP Presets Platform for Tesseract (org.bytedeco:tesseract-platform:5.5.2-1.5.13 - http://bytedeco.org/javacpp-presets/tesseract-platform/)
     (Apache License, Version 2.0) (GNU General Public License (GPL) version 2, or any later version) (GPLv2 with Classpath exception) JavaCPP Presets for videoInput (org.bytedeco:videoinput:0.200-1.5.9 - http://bytedeco.org/javacpp-presets/videoinput/)
     (Apache License, Version 2.0) (GNU General Public License (GPL) version 2, or any later version) (GPLv2 with Classpath exception) JavaCPP Presets Platform for videoInput (org.bytedeco:videoinput-platform:0.200-1.5.9 - http://bytedeco.org/javacpp-presets/videoinput-platform/)
     (Eclipse Public License v2.0) JUnit Jupiter (Aggregator) (org.junit.jupiter:junit-jupiter:5.13.2 - https://junit.org/)
     (Eclipse Public License v2.0) JUnit Jupiter API (org.junit.jupiter:junit-jupiter-api:5.13.2 - https://junit.org/)
     (Eclipse Public License v2.0) JUnit Jupiter Engine (org.junit.jupiter:junit-jupiter-engine:5.13.2 - https://junit.org/)
     (Eclipse Public License v2.0) JUnit Jupiter Params (org.junit.jupiter:junit-jupiter-params:5.13.2 - https://junit.org/)
     (Eclipse Public License v2.0) JUnit Platform Commons (org.junit.platform:junit-platform-commons:1.13.2 - https://junit.org/)
     (Eclipse Public License v2.0) JUnit Platform Engine API (org.junit.platform:junit-platform-engine:1.13.2 - https://junit.org/)
     (MIT) bootstrapfx (org.kordamp.bootstrapfx:bootstrapfx-core:0.4.0 - https://github.com/kordamp/bootstrapfx)
     (Apache-2.0) Ikonli (org.kordamp.ikonli:ikonli-bootstrapicons-pack:12.4.0 - https://github.com/kordamp/ikonli)
     (Apache-2.0) Ikonli (org.kordamp.ikonli:ikonli-core:12.4.0 - https://github.com/kordamp/ikonli)
     (Apache-2.0) Ikonli (org.kordamp.ikonli:ikonli-fluentui-pack:12.4.0 - https://github.com/kordamp/ikonli)
     (Apache-2.0) Ikonli (org.kordamp.ikonli:ikonli-fontawesome6-pack:12.4.0 - https://github.com/kordamp/ikonli)
     (Apache-2.0) Ikonli (org.kordamp.ikonli:ikonli-ionicons4-pack:12.4.0 - https://github.com/kordamp/ikonli)
     (Apache-2.0) Ikonli (org.kordamp.ikonli:ikonli-javafx:12.4.0 - https://github.com/kordamp/ikonli)
     (Apache-2.0) Ikonli (org.kordamp.ikonli:ikonli-material2-pack:12.4.0 - https://github.com/kordamp/ikonli)
     (Apache-2.0) Ikonli (org.kordamp.ikonli:ikonli-materialdesign2-pack:12.4.0 - https://github.com/kordamp/ikonli)
     (MIT) mockito-core (org.mockito:mockito-core:5.23.0 - https://github.com/mockito/mockito)
     (Apache License, Version 2.0) Objenesis (org.objenesis:objenesis:3.3 - http://objenesis.org/objenesis)
     (GPLv2+CE) javafx base (org.openjfx:javafx-base:26.0.1 - https://openjdk.java.net/projects/openjfx/)
     (GPLv2+CE) javafx controls (org.openjfx:javafx-controls:26.0.1 - https://openjdk.java.net/projects/openjfx/)
     (GPLv2+CE) javafx fxml (org.openjfx:javafx-fxml:26.0.1 - https://openjdk.java.net/projects/openjfx/)
     (GPLv2+CE) javafx graphics (org.openjfx:javafx-graphics:26.0.1 - https://openjdk.java.net/projects/openjfx/)
     (GPLv2+CE) javafx media (org.openjfx:javafx-media:26.0.1 - https://openjdk.java.net/projects/openjfx/)
     (GPLv2+CE) javafx swing (org.openjfx:javafx-swing:26.0.1 - https://openjdk.java.net/projects/openjfx/)
     (The Apache License, Version 2.0) org.opentest4j:opentest4j (org.opentest4j:opentest4j:1.3.0 - https://github.com/ota4j-team/opentest4j)
     (The MIT License) Project Lombok (org.projectlombok:lombok:1.18.38 - https://projectlombok.org)
     (MIT) SLF4J API Module (org.slf4j:slf4j-api:2.0.17 - http://www.slf4j.org)
     (The Apache Software License, Version 2.0) SQLite JDBC (org.xerial:sqlite-jdbc:3.53.0.0 - https://github.com/xerial/sqlite-jdbc)
     (GPL v3) vlcj (uk.co.caprica:vlcj:4.8.0 - http://capricasoftware.co.uk/projects/vlcj)
     (GPL v3) vlcj-javafx (uk.co.caprica:vlcj-javafx:1.2.0 - http://capricasoftware.co.uk/projects/vlcj-javafx)
     (GPL v3) vlcj-natives (uk.co.caprica:vlcj-natives:4.8.0 - http://capricasoftware.co.uk/projects/vlcj)
```

