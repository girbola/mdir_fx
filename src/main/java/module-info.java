/**
 * Module descriptor for MDir Image & Video Organizer.
 *
 * NOTE: Several third-party libraries used here are "automatic modules" (they do not ship
 * with their own module-info.class). Their module names are derived from the JAR filename
 * or the Automatic-Module-Name manifest attribute. If the build fails with
 * "module not found" errors, double-check the exact module name by running:
 *   jar --describe-module --file=<path-to-jar>
 *
 * NOTE 2: Full jlink support requires every dependency to be a proper named module.
 * Automatic-module libraries (vlcj, thumbnailator, javacv, etc.) block jlink.
 * As a workaround you can use jpackage in "classpath" mode or replace those
 * libraries with modular alternatives.
 */
module com.girbola {

    // ── Java SE ─────────────────────────────────────────────────────────────
    // java.base is required implicitly; list only additional platform modules.
    requires java.desktop;           // java.awt.*, javax.imageio.*
    requires java.logging;           // java.util.logging.*
    requires java.sql;               // java.sql.*
    requires java.naming;            // needed transitively by some logging impls

    // ── JavaFX ──────────────────────────────────────────────────────────────
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.media;
    requires javafx.swing;           // javafx.embed.swing.SwingFXUtils

    // ── Image / Metadata ─────────────────────────────────────────────────────
    // metadata-extractor: automatic module, name derived from JAR → metadata.extractor
    requires metadata.extractor;
    // Apache Commons Imaging (multi-release named module)
    requires org.apache.commons.imaging;
    // TwelveMonkeys ImageIO plug-ins (automatic modules)
    requires com.twelvemonkeys.imageio.jpeg;
    requires com.twelvemonkeys.imageio.tiff;
    // Thumbnailator (automatic module – Automatic-Module-Name: net.coobird.thumbnailator)
    requires net.coobird.thumbnailator;

    // ── Video / OpenCV (JavaCV / JavaCPP) ───────────────────────────────────
    // JavaCPP runtime (multi-release named module)
    requires org.bytedeco.javacpp;
    // JavaCV (multi-release named module)
    requires org.bytedeco.javacv;
    // OpenCV via JavaCV (multi-release named module)
    requires org.bytedeco.opencv;

    // ── VLC (vlcj) ──────────────────────────────────────────────────────────
    requires uk.co.caprica.vlcj;
    requires uk.co.caprica.vlcj.natives;   // uk.co.caprica.vlcj.binding.*
    requires uk.co.caprica.vlcj.javafx;

    // ── Database ─────────────────────────────────────────────────────────────
    // SQLite JDBC – Xerial publishes Automatic-Module-Name: org.xerial.sqlitejdbc
    requires org.xerial.sqlitejdbc;

    // ── System / Hardware info (OSHI + JNA) ──────────────────────────────────
    requires com.github.oshi;        // oshi-core named module
    requires com.sun.jna;            // JNA core (transitive from oshi-core)
    requires com.sun.jna.platform;   // JNA platform (com.sun.jna.platform.win32.*)

    // ── Icons (Ikonli) ───────────────────────────────────────────────────────
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome6;
    requires org.kordamp.ikonli.bootstrapicons;
    requires org.kordamp.ikonli.fluentui;
    requires org.kordamp.ikonli.material2;
    requires org.kordamp.ikonli.materialdesign2;
    requires org.kordamp.ikonli.ionicons4;

    // ── Bootstrap FX ─────────────────────────────────────────────────────────
    requires org.kordamp.bootstrapfx.core;

    // ── Logging ──────────────────────────────────────────────────────────────
    requires org.slf4j;
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.slf4j2.impl;

    // ── Lombok (annotation processor – compile-time only) ────────────────────
    requires static lombok;

    // ── Opens – required for JavaFX FXML reflection ───────────────────────────
    // Every package whose classes are loaded reflectively by FXMLLoader or
    // JavaFX property bindings must be opened to javafx.fxml (and/or javafx.base).

    // Root application package
    opens com.girbola to javafx.fxml, javafx.graphics, javafx.base;

    // Concurrency
    opens com.girbola.concurrency to javafx.fxml;

    // Configuration
    opens com.girbola.configuration to javafx.fxml;

    // Controllers
    opens com.girbola.controllers.closerlook to javafx.fxml;
    opens com.girbola.controllers.conflicttableview to javafx.fxml;
    opens com.girbola.controllers.copyfiles to javafx.fxml;
    opens com.girbola.controllers.datefixer to javafx.fxml;
    opens com.girbola.controllers.datefixer.table to javafx.fxml;
    opens com.girbola.controllers.datefixer.tasks to javafx.fxml;
    opens com.girbola.controllers.datefixer.utils to javafx.fxml;
    opens com.girbola.controllers.folderscanner to javafx.fxml;
    opens com.girbola.controllers.folderscanner.choosefolders to javafx.fxml;
    opens com.girbola.controllers.folderscanner.folderpicker to javafx.fxml;
    opens com.girbola.controllers.folderscanner.mediafolderscanner to javafx.fxml;
    opens com.girbola.controllers.folderscanner.searchservice to javafx.fxml;
    opens com.girbola.controllers.imageViewer to javafx.fxml;
    opens com.girbola.controllers.importimages to javafx.fxml;
    opens com.girbola.controllers.loading to javafx.fxml;
    opens com.girbola.controllers.main to javafx.fxml;
    opens com.girbola.controllers.main.collect to javafx.fxml;
    opens com.girbola.controllers.main.enums to javafx.fxml;
    opens com.girbola.controllers.main.folderinfoscan to javafx.fxml;
    opens com.girbola.controllers.main.merge to javafx.fxml;
    opens com.girbola.controllers.main.options to javafx.fxml;
    opens com.girbola.controllers.main.populatetableview to javafx.fxml;
    opens com.girbola.controllers.main.selectedfolder to javafx.fxml;
    opens com.girbola.controllers.main.sql to javafx.fxml;
    opens com.girbola.controllers.main.tables to javafx.fxml, javafx.base;
    opens com.girbola.controllers.main.tables.cell to javafx.fxml;
    opens com.girbola.controllers.main.tables.model to javafx.fxml, javafx.base;
    opens com.girbola.controllers.main.tables.tabletype to javafx.fxml;
    opens com.girbola.controllers.main.tasks to javafx.fxml;
    opens com.girbola.controllers.misc to javafx.fxml;
    opens com.girbola.controllers.move to javafx.fxml;
    opens com.girbola.controllers.operate to javafx.fxml;
    opens com.girbola.controllers.possiblefolderchooser to javafx.fxml;
    opens com.girbola.controllers.viewimages to javafx.fxml;
    opens com.girbola.controllers.workdir to javafx.fxml;

    // Domain / model packages
    opens com.girbola.dialogs to javafx.fxml;
    opens com.girbola.drive to javafx.fxml, javafx.base;
    opens com.girbola.eventinfo to javafx.fxml;
    opens com.girbola.events to javafx.fxml;
    opens com.girbola.fileinfo to javafx.fxml, javafx.base;
    opens com.girbola.filelisting to javafx.fxml;
    opens com.girbola.fxml.alertdialog to javafx.fxml;
    opens com.girbola.imagehandling to javafx.fxml;
    opens com.girbola.media to javafx.fxml;
    opens com.girbola.media.collector to javafx.fxml;
    opens com.girbola.messages to javafx.fxml;
    opens com.girbola.messages.html to javafx.fxml;
    opens com.girbola.misc to javafx.fxml;
    opens com.girbola.persistence.configuration to javafx.fxml;
    opens com.girbola.persistence.drive to javafx.fxml;
    opens com.girbola.persistence.fileinfo to javafx.fxml;
    opens com.girbola.persistence.folderinfo to javafx.fxml;
    opens com.girbola.persistence.migration to javafx.fxml;
    opens com.girbola.persistence.selectedfolderinfo to javafx.fxml;
    opens com.girbola.persistence.thumbinfo to javafx.fxml;
    opens com.girbola.rotate to javafx.fxml;
    opens com.girbola.sql to javafx.fxml;
    opens com.girbola.thumbinfo to javafx.fxml, javafx.base;
    opens com.girbola.thumbnailator to javafx.fxml;
    opens com.girbola.utils to javafx.fxml;
    opens com.girbola.utils.folderscanner to javafx.fxml;
    opens com.girbola.utils.imagehash to javafx.fxml;
    opens com.girbola.videothumbnailing to javafx.fxml;
    opens com.girbola.vlcj to javafx.fxml;

    // Shared utility packages (common.*)
    opens common.media to javafx.fxml;
    opens common.utils to javafx.fxml;
    opens common.utils.date to javafx.fxml;
    opens common.utils.regexdynamically to javafx.fxml;
    opens common.utils.ui to javafx.fxml;

    // ── Exports ───────────────────────────────────────────────────────────────
    // This is an application module, so we only export the entry-point package.
    exports com.girbola;
}

