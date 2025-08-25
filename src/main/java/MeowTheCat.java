import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class MeowTheCat {


    private static final Path dataPath = Paths.get("SaveFile.txt");

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        ArrayList<Task> tasks = new ArrayList<>();


        try {
            loadTasks(tasks);
        } catch (IOException e) {
            System.out.println("____________________________________________________________");
            System.out.println("MEOW OOPS!!! Could not read save file: " + e.getMessage());
            System.out.println("Starting with an empty task list.");
            System.out.println("____________________________________________________________");
        }

        System.out.println("____________________________________________________________");
        System.out.println("Hello! I'm MeowTheCat");
        System.out.println("What can I do for you?");
        System.out.println("____________________________________________________________");

        while (true) {
            String line = sc.nextLine().trim();
            try {
                if (line.equalsIgnoreCase("bye")) {
                    printGoodbye();
                    break;
                } else if (line.equalsIgnoreCase("list")) {
                    printList(tasks);
                } else if (line.toLowerCase().startsWith("mark ")) {
                    handleMark(line, tasks);
                } else if (line.toLowerCase().startsWith("unmark ")) {
                    handleUnmark(line, tasks);
                } else if (line.toLowerCase().startsWith("delete ")) {
                    handleDelete(line, tasks);
                } else if (line.toLowerCase().startsWith("todo")) {
                    handleTodo(line, tasks);
                } else if (line.toLowerCase().startsWith("deadline")) {
                    handleDeadline(line, tasks);
                } else if (line.toLowerCase().startsWith("event")) {
                    handleEvent(line, tasks);
                } else {
                    throw new MeowException("MEOW!! MEOW is Confused!!");
                }
            } catch (MeowException de) {
                System.out.println("____________________________________________________________");
                System.out.println("MEOW OOPS!!! " + de.getMessage());
                System.out.println("____________________________________________________________");
            } catch (Exception e) {
                System.out.println("____________________________________________________________");
                System.out.println("MEOW OOPS!!! Something went wrong: " + e.getMessage());
                System.out.println("____________________________________________________________");
            }
        }

        sc.close();
    }



    private static void loadTasks(List<Task> tasks) throws IOException {
        if (!Files.exists(dataPath)) {
            return;
        }
        List<String> lines = Files.readAllLines(dataPath, StandardCharsets.UTF_8);
        int lineNo = 0;
        for (String raw : lines) {
            lineNo++;
            if (raw.trim().isEmpty()) continue;
            try {
                Task t = Task.deserialize(raw);
                tasks.add(t);
            } catch (MeowException e) {
                System.out.println("____________________________________________________________");
                System.out.println("MEOW WARNING: Skipping corrupted line " + lineNo + " in save file: " + e.getMessage());
                System.out.println("____________________________________________________________");
            }
        }
    }

    private static void saveTasks(List<Task> tasks) throws IOException {
        try (BufferedWriter bw = Files.newBufferedWriter(
                dataPath, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (Task t : tasks) {
                bw.write(t.serialize());
                bw.newLine();
            }
        }
    }



    private static void handleDelete(String line, ArrayList<Task> tasks) throws MeowException {
        try {
            int idx = Integer.parseInt(line.substring(7).trim()) - 1;
            if (idx < 0 || idx >= tasks.size()) throw new MeowException("This number does not align with the tasks you have");
            Task removed = tasks.remove(idx);
            try {
                saveTasks(tasks);
            } catch (IOException e) {
                System.out.println("____________________________________________________________");
                System.out.println("MEOW OOPS!!! Could not save after delete: " + e.getMessage());
                System.out.println("____________________________________________________________");
            }
            System.out.println("____________________________________________________________");
            System.out.println("Meow has Noted. I've removed this task:");
            System.out.println("  " + removed);
            System.out.println("Now you have " + tasks.size() + " tasks in the list");
            System.out.println("____________________________________________________________");
        } catch (NumberFormatException e) {
            throw new MeowException("Please provide a valid task number after 'delete'.");
        }
    }

    private static void handleTodo(String line, ArrayList<Task> tasks) throws MeowException {
        String rest = line.length() > 4 ? line.substring(4).trim() : "";
        if (rest.isEmpty()) throw new MeowException("The description of a todo cannot be empty.");
        Task t = new ToDo(rest);
        tasks.add(t);
        try {
            saveTasks(tasks);
        } catch (IOException e) {
            System.out.println("____________________________________________________________");
            System.out.println("MEOW OOPS!!! Could not save after adding todo: " + e.getMessage());
            System.out.println("____________________________________________________________");
        }
        System.out.println("____________________________________________________________");
        System.out.println("Got it. I've added this task:");
        System.out.println("  " + t);
        System.out.println("Now you have " + tasks.size() + " tasks in the list");
        System.out.println("____________________________________________________________");
    }

    private static void handleDeadline(String line, ArrayList<Task> tasks) throws MeowException {
        int byIndex = indexOfIgnoreCase(line, "/by");
        if (line.length() <= 8 || byIndex == -1) throw new MeowException("The deadline command requires a description and '/by <time>'.");
        String desc = line.substring(8, byIndex).trim();
        String byRaw = line.substring(byIndex + 3).trim();
        if (desc.isEmpty()) throw new MeowException("The description of a deadline cannot be empty.");
        if (byRaw.isEmpty()) throw new MeowException("A deadline must have a '/by' time.");
        LocalDateTimeHolder ldt = DateTimeUtil.obtainValuesDate(byRaw);
        Task t = new Deadline(desc, ldt);
        tasks.add(t);
        try {
            saveTasks(tasks);
        } catch (IOException e) {
            System.out.println("____________________________________________________________");
            System.out.println("MEOW OOPS!!! Could not save after adding deadline: " + e.getMessage());
            System.out.println("____________________________________________________________");
        }
        System.out.println("____________________________________________________________");
        System.out.println("Got it. I've added this task:");
        System.out.println("  " + t);
        System.out.println("Now you have " + tasks.size() + " tasks in the list");
        System.out.println("____________________________________________________________");
    }

    private static void handleEvent(String line, ArrayList<Task> tasks) throws MeowException {
        int fromIndex = indexOfIgnoreCase(line, "/from");
        int toIndex = indexOfIgnoreCase(line, "/to");
        if (line.length() <= 5 || fromIndex == -1 || toIndex == -1) throw new MeowException("The event command requires '/from' and '/to'.");
        String desc = line.substring(5, fromIndex).trim();
        String fromRaw = line.substring(fromIndex + 5, toIndex).trim();
        String toRaw = line.substring(toIndex + 3).trim();
        if (desc.isEmpty()) throw new MeowException("The description of an event cannot be empty.");
        if (fromRaw.isEmpty() || toRaw.isEmpty()) throw new MeowException("An event must have both '/from' and '/to' values.");
        LocalDateTimeHolder fromLdt = DateTimeUtil.obtainValuesDate(fromRaw);
        LocalDateTimeHolder toLdt = DateTimeUtil.obtainValuesDate(toRaw);
        Task t = new Event(desc, fromLdt, toLdt);
        tasks.add(t);
        try {
            saveTasks(tasks);
        } catch (IOException e) {
            System.out.println("____________________________________________________________");
            System.out.println("MEOW OOPS!!! Could not save after adding event: " + e.getMessage());
            System.out.println("____________________________________________________________");
        }
        System.out.println("____________________________________________________________");
        System.out.println("Got it. I've added this task:");
        System.out.println("  " + t);
        System.out.println("Now you have " + tasks.size() + " tasks in the list");
        System.out.println("____________________________________________________________");
    }

    private static void handleMark(String line, ArrayList<Task> tasks) throws MeowException {
        try {
            int idx = Integer.parseInt(line.substring(5).trim()) - 1;
            if (idx < 0 || idx >= tasks.size()) throw new MeowException("This number does not align with the tasks you have");
            tasks.get(idx).markDone();
            try {
                saveTasks(tasks);
            } catch (IOException e) {
                System.out.println("____________________________________________________________");
                System.out.println("MEOW OOPS!!! Could not save after mark: " + e.getMessage());
                System.out.println("____________________________________________________________");
            }
            System.out.println("____________________________________________________________");
            System.out.println("Nice! I've marked this task as done:");
            System.out.println("  " + tasks.get(idx));
            System.out.println("____________________________________________________________");
        } catch (NumberFormatException e) {
            throw new MeowException("Please provide a valid task number after 'mark'.");
        }
    }

    private static void handleUnmark(String line, ArrayList<Task> tasks) throws MeowException {
        try {
            int idx = Integer.parseInt(line.substring(7).trim()) - 1;
            if (idx < 0 || idx >= tasks.size()) throw new MeowException("This number does not align with the tasks you have");
            tasks.get(idx).markUndone();
            try {
                saveTasks(tasks);
            } catch (IOException e) {
                System.out.println("____________________________________________________________");
                System.out.println("MEOW OOPS!!! Could not save after unmark: " + e.getMessage());
                System.out.println("____________________________________________________________");
            }
            System.out.println("____________________________________________________________");
            System.out.println("OK, I've marked this task as not done yet:");
            System.out.println("  " + tasks.get(idx));
            System.out.println("____________________________________________________________");
        } catch (NumberFormatException e) {
            throw new MeowException("Please provide a valid task number after 'unmark'.");
        }
    }



    private static void printList(ArrayList<Task> tasks) {
        System.out.println("____________________________________________________________");
        if (tasks.isEmpty()) {
            System.out.println("Here are the tasks in your list:");
        } else {
            System.out.println("Here are the tasks in your list:");
            for (int i = 0; i < tasks.size(); i++) {
                System.out.println((i + 1) + "." + tasks.get(i));
            }
        }
        System.out.println("____________________________________________________________");
    }

    private static void printGoodbye() {
        System.out.println("____________________________________________________________");
        System.out.println("Bye. Hope to see you again soon!");
        System.out.println("____________________________________________________________");
    }

    private static int indexOfIgnoreCase(String s, String sub) {
        return s.toLowerCase().indexOf(sub.toLowerCase());
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

        String yearStr = sb.toString();
        idx++;

        sb.setLength(0);
        while (idx < len && input.charAt(idx) != '-') {
            sb.append(input.charAt(idx));
            idx++;
        }

        String monthStr = sb.toString();
        idx++;

        sb.setLength(0);
        while (idx < len) {
            sb.append(input.charAt(idx));
            idx++;
        }
        String dayStr = sb.toString();

        int year = Integer.parseInt(yearStr);
        int month = Integer.parseInt(monthStr);
        int day = Integer.parseInt(dayStr);

        LocalDate ld = LocalDate.of(year, month, day);
        LocalDateTime dt = ld.atStartOfDay();
        return new LocalDateTimeHolder(dt, false);
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
        for (int i = 0; i < parts.length; i++) parts[i] = parts[i].trim();
        if (parts.length < 3) throw new MeowException("Not enough fields in saved line");
        String type = parts[0];
        String doneStr = parts[1];
        String desc = parts[2];
        boolean done;
        if (!("0".equals(doneStr) || "1".equals(doneStr))) throw new MeowException("Invalid done flag (should be 0 or 1)");
        done = "1".equals(doneStr);

        if ("T".equalsIgnoreCase(type)) {
            ToDo t = new ToDo(desc);
            if (done) t.markDone();
            return t;
        } else if ("D".equalsIgnoreCase(type)) {
            if (parts.length < 4) throw new MeowException("Deadline missing time field");
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
            if (parts.length < 5) throw new MeowException("Event missing from/to fields");
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
