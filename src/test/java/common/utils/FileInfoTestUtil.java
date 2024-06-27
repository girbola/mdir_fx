package common.utils;

import com.girbola.fileinfo.FileInfo;
import com.girbola.fileinfo.FileInfoUtils;
import lombok.extern.java.Log;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Log
public class FileInfoTestUtil {

    public static FileInfo createFileInfoForTesting() {
        Path filePath = Paths.get("src", "test", "resources", "in", "20220413_160023.jpg");
        return createFileInfoForTesting(filePath.toString());
    }

    public static FileInfo createFileInfoForTesting(String path) {
        try {
            return FileInfoUtils.createFileInfo(Paths.get(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
