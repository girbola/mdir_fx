package common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.girbola.Main;
import common.utils.date.DateUtils;
import org.junit.jupiter.api.Test;

class FileNameParseUtilsTest {

	@Test
	void testHasFileNameDate() throws IOException {
		File file = new File(".");
		Path theFile = Paths.get(file.getAbsolutePath() + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "2023-01-01 12.03.50-TestFile.jpg");

		System.out.println("theFile: " + theFile.toFile().getCanonicalPath() + " EXISTS? " + Files.exists(theFile)
				+ " file: " + Files.size(theFile));

		long start = System.currentTimeMillis();
		long actual = FileNameParseUtils.hasFileNameDate(theFile);
		String longToLocalDateTime = null;
		try {
			longToLocalDateTime = DateUtils.longToLocalDateTime(actual)
					.format(Main.simpleDates.getDtf_ymd_hms_minusDots_default());
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out
				.println("Testing if file name contain date and/or time took: " + (System.currentTimeMillis() - start));
		System.out.println("hasFileNameDate: " + file + " And it is date? " + longToLocalDateTime);

		assertTrue(Files.exists(theFile));
		assertTrue(Files.size(theFile) > 0);
		assertEquals("2023-01-01 12.03.50", longToLocalDateTime);
	}
}
