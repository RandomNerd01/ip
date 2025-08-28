package meowthecat;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;


public class MeowTheCat {

    public static void main(String[] args) {
        meowthecat.ConsoleUI ui = new meowthecat.ConsoleUI();
        meowthecat.FileStore store = new meowthecat.FileStore(Paths.get("SaveFile.txt"));
        meowthecat.TaskCollection tasks;


        try {
            List<meowthecat.Task> loaded = store.load();
            tasks = new meowthecat.TaskCollection(loaded);
        } catch (IOException | meowthecat.MeowException e) {
            // start with an empty collection on failure
            ui.showLoadingError(e.getMessage());
            tasks = new meowthecat.TaskCollection();
        }

        ui.showGreeting();
        //Process command
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String line = ui.readLine(scanner);
            if (line == null) {
                break;
            }
            try {
                String cmd = meowthecat.CommandParser.commandType(line);
                if ("bye".equalsIgnoreCase(cmd)) {
                    ui.showGoodbye();
                    break;
                } else if ("list".equalsIgnoreCase(cmd)) {
                    ui.showTaskList(tasks.getAll());
                } else if ("mark".equalsIgnoreCase(cmd)) {
                    int idx = meowthecat.CommandParser.parseIndex(line, "mark");
                    meowthecat.Task t = tasks.markDone(idx);
                    storeSafeSave(store, tasks, ui, "mark");
                    ui.showMarked(t);
                } else if ("unmark".equalsIgnoreCase(cmd)) {
                    int idx = meowthecat.CommandParser.parseIndex(line, "unmark");
                    meowthecat.Task t = tasks.markUndone(idx);
                    storeSafeSave(store, tasks, ui, "unmark");
                    ui.showUnmarked(t);
                } else if ("delete".equalsIgnoreCase(cmd)) {
                    int idx = meowthecat.CommandParser.parseIndex(line, "delete");
                    meowthecat.Task removed = tasks.delete(idx);
                    storeSafeSave(store, tasks, ui, "delete");
                    ui.showDeleted(removed, tasks.size());
                } else if ("todo".equalsIgnoreCase(cmd)) {
                    String desc = meowthecat.CommandParser.parseTodoDesc(line);
                    meowthecat.Task t = new meowthecat.ToDo(desc);
                    tasks.add(t);
                    storeSafeSave(store, tasks, ui, "add-todo");
                    ui.showAdded(t, tasks.size());
                } else if ("deadline".equalsIgnoreCase(cmd)) {
                    String[] parts = meowthecat.CommandParser.parseDeadlineParts(line);
                    String desc = parts[0];
                    String dateRaw = parts[1];
                    meowthecat.LocalDateTimeHolder holder = meowthecat.DateTimeUtil.obtainValuesDate(dateRaw);
                    meowthecat.Task t = new meowthecat.Deadline(desc, holder);
                    tasks.add(t);
                    storeSafeSave(store, tasks, ui, "add-deadline");
                    ui.showAdded(t, tasks.size());
                } else if ("event".equalsIgnoreCase(cmd)) {
                    String[] parts = meowthecat.CommandParser.parseEventParts(line);
                    String desc = parts[0];
                    String fromRaw = parts[1];
                    String toRaw = parts[2];
                    meowthecat.LocalDateTimeHolder fromH = meowthecat.DateTimeUtil.obtainValuesDate(fromRaw);
                    meowthecat.LocalDateTimeHolder toH = meowthecat.DateTimeUtil.obtainValuesDate(toRaw);
                    meowthecat.Task t = new meowthecat.Event(desc, fromH, toH);
                    tasks.add(t);
                    storeSafeSave(store, tasks, ui, "add-event");
                    ui.showAdded(t, tasks.size());
                }
                else if ("clear".equalsIgnoreCase(cmd)) {
                    tasks.clear();
                    storeSafeSave(store, tasks, ui, "clearing all tasks");
                    ui.showCleared();
                }
                else {
                    throw new meowthecat.MeowException("MEOW!! MEOW is Confused!!");
                }
            } catch (meowthecat.MeowException me) {
                ui.showError(me.getMessage());
            } catch (Exception e) {
                ui.showError("Something went wrong: " + e.getMessage());
            }
        }

        scanner.close();
    }
    /**
     * Safely save tasks to the FileStore variable and report any errors via UI.
     *
     * @param store backing file store
     * @param tasks current tasks collection
     * @param ui    UI to show save errors
     * @param action string describing the action that triggered save
     */
    private static void storeSafeSave(meowthecat.FileStore store, meowthecat.TaskCollection tasks, meowthecat.ConsoleUI ui, String action) {
        try {
            store.save(tasks.getAll());
        } catch (IOException e) {
            ui.showSaveError(action, e.getMessage());
        }
    }
}



/**
 * Small console UI helper. Responsible for formatting and printing responses.
 */
class ConsoleUI {


    /**
     * Show that all tasks were cleared.
     */

    void showCleared() {
        System.out.println("____________________________________________________________");
        System.out.println("All tasks have been cleared!");
        System.out.println("____________________________________________________________");
    }
    /**
     * Print greeting message.
     */
    void showGreeting() {
        System.out.println("____________________________________________________________");
        System.out.println("Hello! I'm MeowTheCat");
        System.out.println("What can I do for you?");
        System.out.println("____________________________________________________________");
    }
    /**
     * Read a line from the scanner and trim it or return null if no input.
     *
     * @param sc scanner to read from
     * @return trimmed line or null
     */
    String readLine(Scanner sc) {
        if (!sc.hasNextLine()) return null;
        return sc.nextLine().trim();
    }
    /**
     * Print goodbye message.
     */
    void showGoodbye() {
        System.out.println("____________________________________________________________");
        System.out.println("Bye. Hope to see you again soon!");
        System.out.println("____________________________________________________________");
    }
    /**
     * Show the list of tasks.
     *
     * @param tasks list of tasks (read-only)
     */
    void showTaskList(List<meowthecat.Task> tasks) {
        System.out.println("____________________________________________________________");
        System.out.println("Here are the tasks in your list:");
        for (int i = 0; i < tasks.size(); i++) {
            System.out.println((i + 1) + "." + tasks.get(i));
        }
        System.out.println("____________________________________________________________");
    }
    /**
     * Show that a task was added.
     *
     * @param t     the task
     * @param total new total count
     */
    void showAdded(meowthecat.Task t, int total) {
        System.out.println("____________________________________________________________");
        System.out.println("Got it. I've added this task:");
        System.out.println("  " + t);
        System.out.println("Now you have " + total + " tasks in the list");
        System.out.println("____________________________________________________________");
    }
    /**
     * Show that a task was marked done.
     *
     * @param t the task
     */
    void showMarked(meowthecat.Task t) {
        System.out.println("____________________________________________________________");
        System.out.println("Nice! I've marked this task as done:");
        System.out.println("  " + t);
        System.out.println("____________________________________________________________");
    }
    /**
     * Show that a task was marked undone.
     *
     * @param t the task
     */
    void showUnmarked(meowthecat.Task t) {
        System.out.println("____________________________________________________________");
        System.out.println("OK, I've marked this task as not done yet:");
        System.out.println("  " + t);
        System.out.println("____________________________________________________________");
    }
    /**
     * Show deletion confirmation.
     *
     * @param t         the deleted task
     * @param remaining number of tasks remaining
     */
    void showDeleted(meowthecat.Task t, int remaining) {
        System.out.println("____________________________________________________________");
        System.out.println("Meow has Noted. I've removed this task:");
        System.out.println("  " + t);
        System.out.println("Now you have " + remaining + " tasks in the list");
        System.out.println("____________________________________________________________");
    }
    /**
     * Show an error message.
     *
     * @param msg message to display
     */
    void showError(String msg) {
        System.out.println("____________________________________________________________");
        System.out.println("MEOW OOPS!!! " + msg);
        System.out.println("____________________________________________________________");
    }
    /**
     * Show a file loading error and inform user that an empty list will be used.
     *
     * @param details error details
     */
    void showLoadingError(String details) {
        System.out.println("____________________________________________________________");
        System.out.println("MEOW OOPS!!! Could not read save file: " + details);
        System.out.println("Starting with an empty task list.");
        System.out.println("____________________________________________________________");
    }
    /**
     * Show save error message.
     *
     * @param action  action that triggered save
     * @param details error details
     */
    void showSaveError(String action, String details) {
        System.out.println("____________________________________________________________");
        System.out.println("MEOW OOPS!!! Could not save after " + action + ": " + details);
        System.out.println("____________________________________________________________");
    }
}



/**
 * Simple file-backed store for tasks. Responsible only for reading/writing the
 * serialized lines.
 */
class FileStore {
    private final Path path;

    FileStore(Path path) {
        this.path = path;
    }
    /**
     * Load tasks from the file. Returns empty list if file does not exist.
     *
     * @return deserialized task list
     * @throws IOException or MeowException
     */
    List<meowthecat.Task> load() throws IOException, meowthecat.MeowException {
        List<meowthecat.Task> tasks = new ArrayList<>();
        if (!Files.exists(path)) return tasks;
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        int lineNo = 0;
        for (String line : lines) {
            lineNo++;
            if (line.trim().isEmpty()) continue;
            meowthecat.Task t = meowthecat.Task.deserialize(line);
            tasks.add(t);
        }
        return tasks;
    }
    /**
     * Save tasks to local directory and replaces the existing file if it exists
     *
     * @param tasks tasks to save
     * @throws IOException
     */
    void save(List<meowthecat.Task> tasks) throws IOException {
        try (BufferedWriter bw = Files.newBufferedWriter(
                path, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (meowthecat.Task t : tasks) {
                bw.write(t.serialize());
                bw.newLine();
            }
        }
    }
}


/**
 *  Parses user commands
 */
class CommandParser {
    /**
     * Identify the command type from the input line.
     * @param line user input line
     * @return a command as a string (e.g. "todo", "list", "bye")
     */
    static String commandType(String line) {
        if (line == null || line.trim().isEmpty()) {
            return "";
        }
        String lower = line.trim().toLowerCase();
        if (lower.equals("bye")) {
            return "bye";
        }
        if (lower.equals("list")) {
            return "list";
        }
        if (lower.equals("clear")) {
            return "clear";
        }
        if (lower.startsWith("mark ")) {
            return "mark";
        }
        if (lower.startsWith("unmark ")) {
            return "unmark";
        }
        if (lower.startsWith("delete ")) {
            return "delete";
        }
        if (lower.startsWith("todo")) {
            return "todo";
        }
        if (lower.startsWith("deadline")) {
            return "deadline";
        }
        if (lower.startsWith("event")) {
            return "event";
        }
        return "unknown";
    }

    /**
     * Parse index from a command
     *
     * @param line full command
     * @param cmd  command token
     * @return index
     * @throws meowthecat.MeowException
     */
    static int parseIndex(String line, String cmd) throws meowthecat.MeowException {
        try {
            String numStr = line.substring(cmd.length()).trim();
            int idx = Integer.parseInt(numStr) - 1;
            if (idx < 0) throw new meowthecat.MeowException("This number does not align with the tasks you have");
            return idx;
        } catch (NumberFormatException e) {
            throw new meowthecat.MeowException("Please provide a valid task number after '" + cmd + "'.");
        }
    }

    static String parseTodoDesc(String line) throws meowthecat.MeowException {
        String rest = line.length() > 4 ? line.substring(4).trim() : "";
        if (rest.isEmpty()) throw new meowthecat.MeowException("The description of a todo cannot be empty.");
        return rest;
    }

    static String[] parseDeadlineParts(String line) throws meowthecat.MeowException {
        int byIndex = indexOfIgnoreCase(line, "/by");
        if (line.length() <= 8 || byIndex == -1) throw new meowthecat.MeowException("The deadline command requires a description and '/by <time>'.");
        String desc = line.substring(8, byIndex).trim();
        String by = line.substring(byIndex + 3).trim();
        if (desc.isEmpty()) throw new meowthecat.MeowException("The description of a deadline cannot be empty.");
        if (by.isEmpty()) throw new meowthecat.MeowException("A deadline must have a '/by' time.");
        return new String[]{desc, by};
    }

    static String[] parseEventParts(String line) throws meowthecat.MeowException {
        int fromIndex = indexOfIgnoreCase(line, "/from");
        int toIndex = indexOfIgnoreCase(line, "/to");
        if (line.length() <= 5 || fromIndex == -1 || toIndex == -1) throw new meowthecat.MeowException("The event command requires '/from' and '/to'.");
        String desc = line.substring(5, fromIndex).trim();
        String from = line.substring(fromIndex + 5, toIndex).trim();
        String to = line.substring(toIndex + 3).trim();
        if (desc.isEmpty()) throw new meowthecat.MeowException("The description of an event cannot be empty.");
        if (from.isEmpty() || to.isEmpty()) throw new meowthecat.MeowException("An event must have both '/from' and '/to' values.");
        return new String[]{desc, from, to};
    }

    private static int indexOfIgnoreCase(String s, String sub) {
        return s.toLowerCase().indexOf(sub.toLowerCase());
    }
}



class TaskCollection {

    private final List<meowthecat.Task> tasks;

    TaskCollection() { this.tasks = new ArrayList<>(); }
    TaskCollection(List<meowthecat.Task> initial) { this.tasks = new ArrayList<>(initial); }



    void add(meowthecat.Task t) { tasks.add(t); }
    meowthecat.Task delete(int idx) throws meowthecat.MeowException {
        if (idx < 0 || idx >= tasks.size()) throw new meowthecat.MeowException("This number does not align with the tasks you have");
        return tasks.remove(idx);
    }
    meowthecat.Task markDone(int idx) throws meowthecat.MeowException {
        if (idx < 0 || idx >= tasks.size()) throw new meowthecat.MeowException("This number does not align with the tasks you have");
        meowthecat.Task t = tasks.get(idx);
        t.markDone();
        return t;
    }
    meowthecat.Task markUndone(int idx) throws meowthecat.MeowException {
        if (idx < 0 || idx >= tasks.size()) throw new meowthecat.MeowException("This number does not align with the tasks you have");
        meowthecat.Task t = tasks.get(idx);
        t.markUndone();
        return t;
    }
    List<meowthecat.Task> getAll() { return Collections.unmodifiableList(tasks); }
    int size() { return tasks.size(); }

    public void clear() {
        tasks.clear();
    }
}


class MeowException extends Exception {
    public MeowException(String msg) { super(msg); }
}

class LocalDateTimeHolder {
    final LocalDateTime dateTime;
    final boolean timeIncluded;
    LocalDateTimeHolder(LocalDateTime dt, boolean timeIncluded) {
        this.dateTime = dt;
        this.timeIncluded = timeIncluded;
    }
}


class DateTimeUtil {

    public static meowthecat.LocalDateTimeHolder obtainValuesDate(String input) {
        input = input.trim();
        int len = input.length();
        int idx = 0;

        StringBuilder sb = new StringBuilder();
        while (idx < len && input.charAt(idx) != '-') {
            sb.append(input.charAt(idx));
            idx++;
        }
        if (idx >= len || input.charAt(idx) != '-') {
            throw new IllegalArgumentException("Invalid date format: expected yyyy-MM-dd");
        }
        String yearStr = sb.toString();
        idx++;

        sb.setLength(0);
        while (idx < len && input.charAt(idx) != '-') {
            sb.append(input.charAt(idx));
            idx++;
        }
        if (idx >= len || input.charAt(idx) != '-') {
            throw new IllegalArgumentException("Invalid date format: expected yyyy-MM-dd");
        }
        String monthStr = sb.toString();
        idx++;

        sb.setLength(0);
        while (idx < len) {
            sb.append(input.charAt(idx));
            idx++;
        }
        String dayStr = sb.toString();

        try {
            int year = Integer.parseInt(yearStr);
            int month = Integer.parseInt(monthStr);
            int day = Integer.parseInt(dayStr);

            // LocalDate.of can throw DateTimeException for out-of-range fields;
            // catch it and rethrow an IllegalArgumentException to keep the API consistent.
            LocalDate ld;
            try {
                ld = LocalDate.of(year, month, day);
            } catch (java.time.DateTimeException dte) {
                throw new IllegalArgumentException("Invalid date format: expected yyyy-MM-dd");
            }
            LocalDateTime dt = ld.atStartOfDay();
            return new meowthecat.LocalDateTimeHolder(dt, false);
        } catch (NumberFormatException nfe) {
            // Let NumberFormatException bubble up for clearly non-numeric input (test expects this)
            throw nfe;
        }
    }


    public static String formatForDisplay(meowthecat.LocalDateTimeHolder holder) {
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("MMM dd yyyy");
        return holder.dateTime.toLocalDate().format(dateFmt);
    }
}


abstract class Task {
    protected final String description;
    protected boolean isDone;

    protected Task(String description) {
        this.description = description;
        this.isDone = false;
    }

    public void markDone() { isDone = true; }
    public void markUndone() { isDone = false; }
    public boolean isDone() { return isDone; }

    public abstract String serialize();

    public static meowthecat.Task deserialize(String line) throws meowthecat.MeowException {
        String[] parts = line.split("\\|", -1);
        for (int i = 0; i < parts.length; i++) parts[i] = parts[i].trim();
        if (parts.length < 3) throw new meowthecat.MeowException("Not enough fields in saved line");
        String type = parts[0];
        String doneStr = parts[1];
        String desc = parts[2];
        boolean done;
        if (!("0".equals(doneStr) || "1".equals(doneStr))) throw new meowthecat.MeowException("Invalid done flag (should be 0 or 1)");
        done = "1".equals(doneStr);

        if ("T".equalsIgnoreCase(type)) {
            meowthecat.ToDo t = new meowthecat.ToDo(desc);
            if (done) t.markDone();
            return t;
        } else if ("D".equalsIgnoreCase(type)) {
            if (parts.length < 4) throw new meowthecat.MeowException("Deadline missing time field");
            String serializedDate = parts[3];
            try {
                meowthecat.LocalDateTimeHolder holder = meowthecat.DateTimeUtil.obtainValuesDate(serializedDate);
                meowthecat.Deadline d = new meowthecat.Deadline(desc, holder);
                if (done) d.markDone();
                return d;
            } catch (Exception e) {
                throw new meowthecat.MeowException("Invalid date format for deadline: " + serializedDate);
            }
        } else if ("E".equalsIgnoreCase(type)) {
            if (parts.length < 5) throw new meowthecat.MeowException("Event missing from/to fields");
            String fromSer = parts[3];
            String toSer = parts[4];
            try {
                meowthecat.LocalDateTimeHolder fromH = meowthecat.DateTimeUtil.obtainValuesDate(fromSer);
                meowthecat.LocalDateTimeHolder toH = meowthecat.DateTimeUtil.obtainValuesDate(toSer);
                meowthecat.Event ev = new meowthecat.Event(desc, fromH, toH);
                if (done) ev.markDone();
                return ev;
            } catch (Exception e) {
                throw new meowthecat.MeowException("Invalid date/time format for event: " + e.getMessage());
            }
        } else {
            throw new meowthecat.MeowException("Unknown task type: " + type);
        }
    }

    protected String doneFlag() { return isDone ? "[X]" : "[ ]"; }

    @Override
    public abstract String toString();
}

class ToDo extends meowthecat.Task {
    public ToDo(String desc) { super(desc); }

    @Override
    public String serialize() {
        return String.join(" | ", "T", (isDone ? "1" : "0"), description);
    }

    @Override
    public String toString() {
        return "[T]" + doneFlag() + " " + description;
    }
}

class Deadline extends meowthecat.Task {
    private final meowthecat.LocalDateTimeHolder byHolder;

    public Deadline(String desc, meowthecat.LocalDateTimeHolder byHolder) {
        super(desc);
        this.byHolder = byHolder;
    }

    @Override
    public String serialize() {
        String iso = byHolder.dateTime.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
        return String.join(" | ", "D", (isDone ? "1" : "0"), description, iso);
    }

    @Override
    public String toString() {
        String formatted = meowthecat.DateTimeUtil.formatForDisplay(byHolder);
        return "[D]" + doneFlag() + " " + description + " (by: " + formatted + ")";
    }
}

class Event extends meowthecat.Task {
    private final meowthecat.LocalDateTimeHolder fromHolder;
    private final meowthecat.LocalDateTimeHolder toHolder;

    public Event(String desc, meowthecat.LocalDateTimeHolder fromHolder, meowthecat.LocalDateTimeHolder toHolder) {
        super(desc);
        this.fromHolder = fromHolder;
        this.toHolder = toHolder;
    }

    @Override
    public String serialize() {
        String fromIso = fromHolder.dateTime.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String toIso = toHolder.dateTime.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
        return String.join(" | ", "E", (isDone ? "1" : "0"), description, fromIso, toIso);
    }

    @Override
    public String toString() {
        String formattedFrom = meowthecat.DateTimeUtil.formatForDisplay(fromHolder);
        String formattedTo = meowthecat.DateTimeUtil.formatForDisplay(toHolder);
        return "[E]" + doneFlag() + " " + description + " (from: " + formattedFrom + " to: " + formattedTo + ")";
    }
}
