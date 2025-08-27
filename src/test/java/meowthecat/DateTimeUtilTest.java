package meowthecat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class DateTimeUtilTest {

    private Locale original;

    @BeforeEach
    void before() {
        // ensure month name formatting is deterministic
        original = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
    }

    @AfterEach
    void after() {
        Locale.setDefault(original);
    }

    @Test
    void obtainValuesDate_validDate() {
        LocalDateTimeHolder holder = DateTimeUtil.obtainValuesDate("2019-12-02");
        assertNotNull(holder);
        assertFalse(holder.timeIncluded);
        LocalDateTime expected = LocalDate.of(2019, 12, 2).atStartOfDay();
        assertEquals(expected, holder.dateTime);
    }

    @Test
    void obtainValuesDate_invalidFormat_throws() {
        assertThrows(IllegalArgumentException.class, () -> DateTimeUtil.obtainValuesDate("02-12-2019"));
        assertThrows(IllegalArgumentException.class, () -> DateTimeUtil.obtainValuesDate("2019/12/02"));
        assertThrows(NumberFormatException.class, () -> DateTimeUtil.obtainValuesDate("abcd-ef-gh"));
    }

    @Test
    void formatForDisplay_monthShortNameEnglish() {
        LocalDateTimeHolder holder = DateTimeUtil.obtainValuesDate("2019-12-02");
        String formatted = DateTimeUtil.formatForDisplay(holder);
        assertEquals("Dec 02 2019", formatted);
    }
}
