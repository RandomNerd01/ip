package meowthecat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommandParserTest {

    @Test
    void parseDeadlineParts_valid() throws MeowException {
        String line = "deadline return book /by 2019-12-02";
        String[] parts = CommandParser.parseDeadlineParts(line);
        assertEquals(2, parts.length);
        assertEquals("return book", parts[0]);
        assertEquals("2019-12-02", parts[1]);
    }

    @Test
    void parseDeadlineParts_missingBy_throws() {
        String line = "deadline something without by";
        MeowException ex = assertThrows(MeowException.class,
                () -> CommandParser.parseDeadlineParts(line));
        assertTrue(ex.getMessage().toLowerCase().contains("/by") || ex.getMessage().toLowerCase().contains("deadline"),
                "Expected helpful message about missing '/by'");
    }

    @Test
    void parseDeadlineParts_emptyDescription_throws() {
        String line = "deadline    /by 2019-12-02";
        MeowException ex = assertThrows(MeowException.class,
                () -> CommandParser.parseDeadlineParts(line));
        assertTrue(ex.getMessage().toLowerCase().contains("description"),
                "Expected message that description cannot be empty");
    }

    @Test
    void parseIndex_validAndInvalid() {
        // valid
        try {
            int idx = CommandParser.parseIndex("delete 3", "delete");
            assertEquals(2, idx);
        } catch (MeowException e) {
            fail("Should have parsed index 3");
        }

        // invalid number
        assertThrows(MeowException.class, () -> CommandParser.parseIndex("delete abc", "delete"));

        // negative or zero -> parseIndex translates to zero-based and throws for < 1 input
        assertThrows(MeowException.class, () -> CommandParser.parseIndex("delete 0", "delete"));
    }
}
