import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MeowTheCat {

    static String pathStr = "SaveFile.txt";
    static Path dataPath = Path.of(pathStr);

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
        Task t = Task.createToDo(rest);
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
        String by = line.substring(byIndex + 3).trim();
        if (desc.isEmpty()) throw new MeowException("The description of a deadline cannot be empty.");
        if (by.isEmpty()) throw new MeowException("A deadline must have a '/by' time.");
        Task t = Task.createDeadline(desc, by);
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
        String from = line.substring(fromIndex + 5, toIndex).trim();
        String to = line.substring(toIndex + 3).trim();
        if (desc.isEmpty()) throw new MeowException("The description of an event cannot be empty.");
        if (from.isEmpty() || to.isEmpty()) throw new MeowException("An event must have both '/from' and '/to' values.");
        Task t = Task.createEvent(desc, from, to);
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

enum TaskType {
    TODO, DEADLINE, EVENT
}

class Task {

    private TaskType type;
    private String description;
    private boolean isDone;
    private String by;
    private String from;
    private String to;

    private Task(TaskType type, String desc) {
        this.type = type;
        this.description = desc;
        this.isDone = false;
    }

    public static Task createToDo(String desc) { return new Task(TaskType.TODO, desc); }
    public static Task createDeadline(String desc, String by) {
        Task t = new Task(TaskType.DEADLINE, desc);
        t.by = by;
        return t;
    }
    public static Task createEvent(String desc, String from, String to) {
        Task t = new Task(TaskType.EVENT, desc);
        t.from = from;
        t.to = to;
        return t;
    }

    public void markDone() { isDone = true; }
    public void markUndone() { isDone = false; }
    public boolean isDone() { return isDone; }

    public String serialize() {
        String done = isDone ? "1" : "0";
        if (type == TaskType.DEADLINE) {
            return String.join(" | ", "D", done, description, by);
        } else if (type == TaskType.EVENT) {
            return String.join(" | ", "E", done, description, from, to);
        } else {
            return String.join(" | ", "T", done, description);
        }
    }

    public static Task deserialize(String line) throws MeowException {
        String[] parts = line.split("\\|", -1);
        for (int i = 0; i < parts.length; i++) parts[i] = parts[i].trim();
        if (parts.length < 3) throw new MeowException("Not enough fields");
        String type = parts[0];
        String doneStr = parts[1];
        String desc = parts[2];

        boolean done;
        if (!("0".equals(doneStr) || "1".equals(doneStr))) {
            throw new MeowException("Invalid done flag (should be 0 or 1)");
        }
        done = "1".equals(doneStr);

        if ("T".equalsIgnoreCase(type)) {
            Task t = Task.createToDo(desc);
            if (done) t.markDone();
            return t;
        } else if ("D".equalsIgnoreCase(type)) {
            if (parts.length < 4) throw new MeowException("Deadline missing '/by' field");
            String by = parts[3];
            Task t = Task.createDeadline(desc, by);
            if (done) t.markDone();
            return t;
        } else if ("E".equalsIgnoreCase(type)) {
            if (parts.length < 5) throw new MeowException("Event missing '/from' or '/to' field");
            String from = parts[3];
            String to = parts[4];
            Task t = Task.createEvent(desc, from, to);
            if (done) t.markDone();
            return t;
        } else {
            throw new MeowException("Unknown task type: " + type);
        }
    }

    private String status() { return isDone ? "[X]" : "[ ]"; }

    @Override
    public String toString() {
        if (type == TaskType.DEADLINE) {
            return "[D]" + status() + " " + description + " (by: " + by + ")";
        } else if (type == TaskType.EVENT) {
            return "[E]" + status() + " " + description + " (from: " + from + " to: " + to + ")";
        } else {
            return "[T]" + status() + " " + description;
        }
    }
}
