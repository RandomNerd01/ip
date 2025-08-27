package meowthecat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MeowTheCatTest {

    private final InputStream originalIn = System.in;
    private final PrintStream originalOut = System.out;
    private Locale originalLocale;

    @BeforeEach
    void setUp() throws Exception {
        // Make date formatting deterministic (Dec vs localized month)
        originalLocale = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);

        // Ensure no leftover save file to make the run deterministic
        Files.deleteIfExists(Paths.get("SaveFile.txt"));
    }

    @AfterEach
    void tearDown() throws Exception {
        // restore streams and locale
        System.setIn(originalIn);
        System.setOut(originalOut);
        Locale.setDefault(originalLocale);

        // cleanup created save file
        Files.deleteIfExists(Paths.get("SaveFile.txt"));
    }

    @Test
    void testFullInteractionSequence() throws Exception {
        // Input sequence as specified
        String input = ""
                + "clear\n"
                + "todo borrow book\n"
                + "todo read book\n"
                + "deadline return book /by 2019-12-02\n"
                + "list\n"
                + "delete 2\n"
                + "list\n"
                + "bye\n";

        // Provide input to System.in
        ByteArrayInputStream testIn = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        System.setIn(testIn);

        // Capture System.out
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8.name());
        System.setOut(ps);

        // Run the application
        MeowTheCat.main(new String[0]);

        // Read output
        ps.flush();
        String actual = baos.toString(StandardCharsets.UTF_8.name());
        String actualNormalized = actual.replace("\r\n", "\n");

        // Expected output (must match ConsoleUI formatting)
        String expected = ""
                + "____________________________________________________________\n"
                + "Hello! I'm MeowTheCat\n"
                + "What can I do for you?\n"
                + "____________________________________________________________\n"
                + "____________________________________________________________\n"
                + "All tasks have been cleared!\n"
                + "____________________________________________________________\n"
                + "____________________________________________________________\n"
                + "Got it. I've added this task:\n"
                + "  [T][ ] borrow book\n"
                + "Now you have 1 tasks in the list\n"
                + "____________________________________________________________\n"
                + "____________________________________________________________\n"
                + "Got it. I've added this task:\n"
                + "  [T][ ] read book\n"
                + "Now you have 2 tasks in the list\n"
                + "____________________________________________________________\n"
                + "____________________________________________________________\n"
                + "Got it. I've added this task:\n"
                + "  [D][ ] return book (by: Dec 02 2019)\n"
                + "Now you have 3 tasks in the list\n"
                + "____________________________________________________________\n"
                + "____________________________________________________________\n"
                + "Here are the tasks in your list:\n"
                + "1.[T][ ] borrow book\n"
                + "2.[T][ ] read book\n"
                + "3.[D][ ] return book (by: Dec 02 2019)\n"
                + "____________________________________________________________\n"
                + "____________________________________________________________\n"
                + "Meow has Noted. I've removed this task:\n"
                + "  [T][ ] read book\n"
                + "Now you have 2 tasks in the list\n"
                + "____________________________________________________________\n"
                + "____________________________________________________________\n"
                + "Here are the tasks in your list:\n"
                + "1.[T][ ] borrow book\n"
                + "2.[D][ ] return book (by: Dec 02 2019)\n"
                + "____________________________________________________________\n"
                + "____________________________________________________________\n"
                + "Bye. Hope to see you again soon!\n"
                + "____________________________________________________________\n";

        String expectedNormalized = expected.replace("\r\n", "\n");

        assertEquals(expectedNormalized, actualNormalized);
    }
}
