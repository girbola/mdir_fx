package common.utils;

import com.girbola.utils.CommonUserFolders;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Map;

public class CommonUserFoldersTest {

    @Test
    public void testCommonFolders() {
        Map<CommonUserFolders.Kind, Path> dirs = CommonUserFolders.resolve();
        for (Map.Entry<CommonUserFolders.Kind, Path> entry : dirs.entrySet()) {
            CommonUserFolders.Kind k = entry.getKey();
            Path p = entry.getValue();
            System.out.println(k + " -> " + p);
        }

//        Map<CommonUserFolders.Kind, Path> resolve = CommonUserFolders.getCommonOSUserFolders();

        Path candidate = Path.of(System.getProperty("user.home"), "Pictures", "Trip2026");
        System.out.println("Classify: " + CommonUserFolders.classify(candidate).orElse(null));
    }

}
