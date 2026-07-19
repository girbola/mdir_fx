package com.girbola.utils;

// File: WindowsKnownFolders.java
// Requires JNA: com.sun.jna:jna and com.sun.jna:platform

import com.sun.jna.platform.win32.Guid.GUID;
import com.sun.jna.platform.win32.Ole32;
import com.sun.jna.platform.win32.Shell32;
import com.sun.jna.platform.win32.WinNT.HRESULT;
import com.sun.jna.platform.win32.WinReg.HKEY;
import com.sun.jna.ptr.PointerByReference;

import java.nio.file.Path;
import java.nio.file.Paths;

final class WindowsKnownFolders {

    // Well-known folder IDs (GUIDs)
    // Documents
    static final GUID FOLDERID_Documents = new GUID("{FDD39AD0-238F-46AF-ADB4-6C85480369C7}");
    // Pictures
    static final GUID FOLDERID_Pictures  = new GUID("{33E28130-4E1E-4676-835A-98395C3BC3BB}");
    // Downloads
    static final GUID FOLDERID_Downloads = new GUID("{374DE290-123F-4565-9164-39C4925E467B}");
    // Videos
    static final GUID FOLDERID_Videos    = new GUID("{18989B1D-99B5-455B-841C-AB7C74E4DDFC}");

    static Path getKnownFolder(GUID id) {
        PointerByReference ppszPath = new PointerByReference();
        // Flags: 0 = default; can also use Shell32.KF_FLAG_DEFAULT
        HRESULT hr = Shell32.INSTANCE.SHGetKnownFolderPath(id, 0, (HKEY) null, ppszPath);
        if (hr.intValue() != 0) {
            throw new IllegalStateException("SHGetKnownFolderPath failed: " + hr.intValue());
        }
        String path = ppszPath.getValue().getWideString(0);
        // Free the memory allocated by the shell (CoTaskMemFree)
        Ole32.INSTANCE.CoTaskMemFree(ppszPath.getValue());
        return Paths.get(path);
    }

    private WindowsKnownFolders() {}
}
