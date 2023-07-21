package common.utils.date;

import com.girbola.Main;
import com.girbola.messages.Messages;
import common.utils.Conversion;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DateUtilsTest {

    @Test
    public void testStringDateToLocalDateTime() {
        // yyyy-MM-dd HH.mm.ss
        String date = "2023-07-21 12.00.03";

        LocalDateTime localDateTime = DateUtils.stringDateToLocalDateTime(date);
        Messages.sprintf("testStringDateToLocalDateTime: " + localDateTime);

        assertEquals("2023-07-21 12.00.03", localDateTime.format(Main.simpleDates.getDtf_ymd_hms_minusDots_default()));
    }

}
