package common.utils.folderscanner;

import com.girbola.utils.folderscanner.FolderScanner;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.Test;

public class FolderScannerTest {

    @Test
    public void testFolderScanner() {
        Path path = Paths.get("C:\\Users\\");
        List<Path> paths = FolderScanner.scanFolders(path);
        for(Path path1 : paths) {
            System.out.println("Path found: " + path1.toString());
        }


    }
}
