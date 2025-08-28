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

public class MeowTheCat {

    public static void main(String[] args) {
        ConsoleUI ui = new ConsoleUI();
        FileStore store = new FileStore(Paths.get("SaveFile.txt"));
        TaskCollection tasks;


        try {
            List<Task> loaded = store.load();
            tasks = new TaskCollection(loaded);
        } catch (IOException | MeowException e) {
            ui.showLoadingError(e.getMessage());
            tasks = new TaskCollection();
        }

        ui.showGreeting();

        Scanner scanner = new Scanner(System.in);
        while (true) {
            String line = ui.readLine(scanner);
            if (line == null) {
                break;
            }
            try {
                String cmd = CommandParser.commandType(line);
                if ("bye".equalsIgnoreCase(cmd)) {
                    ui.showGoodbye();
                    break;
                } else if ("list".equalsIgnoreCase(cmd)) {
                    ui.showTaskList(tasks.getAll());
                } else if ("mark".equalsIgnoreCase(cmd)) {
                    int idx = CommandParser.parseIndex(line, "mark");
                    Task t = tasks.markDone(idx);
                    storeSafeSave(store, tasks, ui, "mark");
                    ui.showMarked(t);
                } else if ("unmark".equalsIgnoreCase(cmd)) {
                    int idx = CommandParser.parseIndex(line, "unmark");
                    Task t = tasks.markUndone(idx);
                    storeSafeSave(store, tasks, ui, "unmark");
                    ui.showUnmarked(t);
                } else if ("delete".equalsIgnoreCase(cmd)) {
                    int idx = CommandParser.parseIndex(line, "delete");
                    Task removed = tasks.delete(idx);
                    storeSafeSave(store, tasks, ui, "delete");
                    ui.showDeleted(removed, tasks.size());
                } else if ("todo".equalsIgnoreCase(cmd)) {
                    String desc = CommandParser.parseTodoDesc(line);
                    Task t = new ToDo(desc);
                    tasks.add(t);
                    storeSafeSave(store, tasks, ui, "add-todo");
                    ui.showAdded(t, tasks.size());
                } else if ("deadline".equalsIgnoreCase(cmd)) {
                    String[] parts = CommandParser.parseDeadlineParts(line);
                    String desc = parts[0];
                    String dateRaw = parts[1];
                    LocalDateTimeHolder holder = DateTimeUtil.obtainValuesDate(dateRaw);
                    Task t = new Deadline(desc, holder);
                    tasks.add(t);
                    storeSafeSave(store, tasks, ui, "add-deadline");
                    ui.showAdded(t, tasks.size());
                } else if ("event".equalsIgnoreCase(cmd)) {
                    String[] parts = CommandParser.parseEventParts(line);
                    String desc = parts[0];
                    String fromRaw = parts[1];
                    String toRaw = parts[2];
                    LocalDateTimeHolder fromH = DateTimeUtil.obtainValuesDate(fromRaw);
                    LocalDateTimeHolder toH = DateTimeUtil.obtainValuesDate(toRaw);
                    Task t = new Event(desc, fromH, toH);
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
                    throw new MeowException("MEOW!! MEOW is Confused!!");
                }
            } catch (MeowException me) {
                ui.showError(me.getMessage());
            } catch (Exception e) {
                ui.showError("Something went wrong: " + e.getMessage());
            }
        }

        scanner.close();
    }

    private static void storeSafeSave(FileStore store, TaskCollection tasks, ConsoleUI ui, String action) {
        try {
            store.save(tasks.getAll());
        } catch (IOException e) {
            ui.showSaveError(action, e.getMessage());
        }
    }
}




class ConsoleUI {
    void showCleared() {
        System.out.println("____________________________________________________________");
        System.out.println("All tasks have been cleared!");
        System.out.println("____________________________________________________________");
    }
    void showGreeting() {
        System.out.println("____________________________________________________________");
        System.out.println("Hello! I'm MeowTheCat");
        System.out.println("What can I do for you?");
        System.out.println("____________________________________________________________");
    }

    String readLine(Scanner sc) {
        if (!sc.hasNextLine()) return null;
        return sc.nextLine().trim();
    }

    void showGoodbye() {
        System.out.println("____________________________________________________________");
        System.out.println("Bye. Hope to see you again soon!");
        System.out.println("____________________________________________________________");
    }

    void showTaskList(List<Task> tasks) {
        System.out.println("____________________________________________________________");
        System.out.println("Here are the tasks in your list:");
        for (int i = 0; i < tasks.size(); i++) {
            System.out.println((i + 1) + "." + tasks.get(i));
        }
        System.out.println("____________________________________________________________");
    }

    void showAdded(Task t, int total) {
        System.out.println("____________________________________________________________");
        System.out.println("Got it. I've added this task:");
        System.out.println("  " + t);
        System.out.println("Now you have " + total + " tasks in the list");
        System.out.println("____________________________________________________________");
    }

    void showMarked(Task t) {
        System.out.println("____________________________________________________________");
        System.out.println("Nice! I've marked this task as done:");
        System.out.println("  " + t);
        System.out.println("____________________________________________________________");
    }

    void showUnmarked(Task t) {
        System.out.println("____________________________________________________________");
        System.out.println("OK, I've marked this task as not done yet:");
        System.out.println("  " + t);
        System.out.println("____________________________________________________________");
    }

    void showDeleted(Task t, int remaining) {
        System.out.println("____________________________________________________________");
        System.out.println("Meow has Noted. I've removed this task:");
        System.out.println("  " + t);
        System.out.println("Now you have " + remaining + " tasks in the list");
        System.out.println("____________________________________________________________");
    }

    void showError(String msg) {
        System.out.println("____________________________________________________________");
        System.out.println("MEOW OOPS!!! " + msg);
        System.out.println("____________________________________________________________");
    }

    void showLoadingError(String details) {
        System.out.println("____________________________________________________________");
        System.out.println("MEOW OOPS!!! Could not read save file: " + details);
        System.out.println("Starting with an empty task list.");
        System.out.println("____________________________________________________________");
    }

    void showSaveError(String action, String details) {
        System.out.println("____________________________________________________________");
        System.out.println("MEOW OOPS!!! Could not save after " + action + ": " + details);
        System.out.println("____________________________________________________________");
    }
}



class FileStore {
    private final Path path;

    FileStore(Path path) {
        this.path = path;
    }

    List<Task> load() throws IOException, MeowException {
        List<Task> tasks = new ArrayList<>();
        if (!Files.exists(path)) return tasks;
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        int lineNo = 0;
        for (String line : lines) {
            lineNo++;
            if (line.trim().isEmpty()) continue;
            Task t = Task.deserialize(line);
            tasks.add(t);
        }
        return tasks;
    }

    void save(List<Task> tasks) throws IOException {
        try (BufferedWriter bw = Files.newBufferedWriter(
                path, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (Task t : tasks) {
                bw.write(t.serialize());
                bw.newLine();
            }
        }
    }
}



class CommandParser {

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

    static int parseIndex(String line, String cmd) throws MeowException {
        try {
            String numStr = line.substring(cmd.length()).trim();
            int idx = Integer.parseInt(numStr) - 1;
            if (idx < 0) {
                throw new MeowException("This number does not align with the tasks you have");
            }
            return idx;
        } catch (NumberFormatException e) {
            throw new MeowException("Please provide a valid task number after '" + cmd + "'.");
        }
    }

    static String parseTodoDesc(String line) throws MeowException {
        String rest = line.length() > 4 ? line.substring(4).trim() : "";
        if (rest.isEmpty()) throw new MeowException("The description of a todo cannot be empty.");
        return rest;
    }

    static String[] parseDeadlineParts(String line) throws MeowException {
        int byIndex = indexOfIgnoreCase(line, "/by");
        if (line.length() <= 8 || byIndex == -1) {
            throw new MeowException("The deadline command requires a description and '/by <time>'.");
        }
        String desc = line.substring(8, byIndex).trim();
        String by = line.substring(byIndex + 3).trim();
        if (desc.isEmpty()) {
            throw new MeowException("The description of a deadline cannot be empty.");
        }
        if (by.isEmpty()) {
            throw new MeowException("A deadline must have a '/by' time.");
        }
        return new String[]{desc, by};
    }

    static String[] parseEventParts(String line) throws MeowException {
        int fromIndex = indexOfIgnoreCase(line, "/from");
        int toIndex = indexOfIgnoreCase(line, "/to");
        if (line.length() <= 5 || fromIndex == -1 || toIndex == -1) {
            throw new MeowException("The event command requires '/from' and '/to'.");
        }
        String desc = line.substring(5, fromIndex).trim();
        String from = line.substring(fromIndex + 5, toIndex).trim();
        String to = line.substring(toIndex + 3).trim();
        if (desc.isEmpty()) {
            throw new MeowException("The description of an event cannot be empty.");
        }
        if (from.isEmpty() || to.isEmpty()) {
            throw new MeowException("An event must have both '/from' and '/to' values.");
        }
        return new String[]{desc, from, to};
    }

    private static int indexOfIgnoreCase(String s, String sub) {
        return s.toLowerCase().indexOf(sub.toLowerCase());
    }
}



class TaskCollection {
    private final List<Task> tasks;

    TaskCollection() { this.tasks = new ArrayList<>(); }
    TaskCollection(List<Task> initial) { this.tasks = new ArrayList<>(initial); }

    void add(Task t) { tasks.add(t); }
    Task delete(int idx) throws MeowException {
        if (idx < 0 || idx >= tasks.size()) {
            throw new MeowException("This number does not align with the tasks you have");
        }
        return tasks.remove(idx);
    }
    Task markDone(int idx) throws MeowException {
        if (idx < 0 || idx >= tasks.size()) {
            throw new MeowException("This number does not align with the tasks you have");
        }
        Task t = tasks.get(idx);
        t.markDone();
        return t;
    }
    Task markUndone(int idx) throws MeowException {
        if (idx < 0 || idx >= tasks.size()) {
            throw new MeowException("This number does not align with the tasks you have");
        }
        Task t = tasks.get(idx);
        t.markUndone();
        return t;
    }
    List<Task> getAll() {
        return Collections.unmodifiableList(tasks);
    }
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

    public static LocalDateTimeHolder obtainValuesDate(String input) {
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
            LocalDate ld;
            try {
                ld = LocalDate.of(year, month, day);
            } catch (java.time.DateTimeException dte) {
                throw new IllegalArgumentException("Invalid date format: expected yyyy-MM-dd");
            }
            LocalDateTime dt = ld.atStartOfDay();
            return new LocalDateTimeHolder(dt, false);
        } catch (NumberFormatException nfe) {
            throw nfe;
        }
    }


    public static String formatForDisplay(LocalDateTimeHolder holder) {
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

    public static Task deserialize(String line) throws MeowException {
        String[] parts = line.split("\\|", -1);
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }
        if (parts.length < 3) {
            throw new MeowException("Not enough fields in saved line");
        }
        String type = parts[0];
        String doneStr = parts[1];
        String desc = parts[2];
        boolean done;
        if (!("0".equals(doneStr) || "1".equals(doneStr))) {
            throw new MeowException("Invalid done flag (should be 0 or 1)");
        }
        done = "1".equals(doneStr);

        if ("T".equalsIgnoreCase(type)) {
            ToDo t = new ToDo(desc);
            if (done) t.markDone();
            return t;
        } else if ("D".equalsIgnoreCase(type)) {
            if (parts.length < 4) {
                throw new MeowException("Deadline missing time field");
            }
            String serializedDate = parts[3];
            try {
                LocalDateTimeHolder holder = DateTimeUtil.obtainValuesDate(serializedDate);
                Deadline d = new Deadline(desc, holder);
                if (done) d.markDone();
                return d;
            } catch (Exception e) {
                throw new MeowException("Invalid date format for deadline: " + serializedDate);
            }
        } else if ("E".equalsIgnoreCase(type)) {
            if (parts.length < 5) {
                throw new MeowException("Event missing from/to fields");
            }
            String fromSer = parts[3];
            String toSer = parts[4];
            try {
                LocalDateTimeHolder fromH = DateTimeUtil.obtainValuesDate(fromSer);
                LocalDateTimeHolder toH = DateTimeUtil.obtainValuesDate(toSer);
                Event ev = new Event(desc, fromH, toH);
                if (done) ev.markDone();
                return ev;
            } catch (Exception e) {
                throw new MeowException("Invalid date/time format for event: " + e.getMessage());
            }
        } else {
            throw new MeowException("Unknown task type: " + type);
        }
    }

    protected String doneFlag() { return isDone ? "[X]" : "[ ]"; }

    @Override
    public abstract String toString();
}

class ToDo extends Task {
    public ToDo(String desc) {
        super(desc);
    }

    @Override
    public String serialize() {
        return String.join(" | ", "T", (isDone ? "1" : "0"), description);
    }

    @Override
    public String toString() {
        return "[T]" + doneFlag() + " " + description;
    }
}

class Deadline extends Task {
    private final LocalDateTimeHolder byHolder;

    public Deadline(String desc, LocalDateTimeHolder byHolder) {
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
        String formatted = DateTimeUtil.formatForDisplay(byHolder);
        return "[D]" + doneFlag() + " " + description + " (by: " + formatted + ")";
    }
}

class Event extends Task {
    private final LocalDateTimeHolder fromHolder;
    private final LocalDateTimeHolder toHolder;

    public Event(String desc, LocalDateTimeHolder fromHolder, LocalDateTimeHolder toHolder) {
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
        String formattedFrom = DateTimeUtil.formatForDisplay(fromHolder);
        String formattedTo = DateTimeUtil.formatForDisplay(toHolder);
        return "[E]" + doneFlag() + " " + description + " (from: " + formattedFrom + " to: " + formattedTo + ")";
    }
}
