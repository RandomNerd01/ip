import java.util.ArrayList;
import java.util.Scanner;

public class MeowTheCat {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        ArrayList<Task> lol = new ArrayList<>();

        System.out.println("____________________________________________________________");
        System.out.println("Hello! I'm MeowTheCat");
        System.out.println("What can I do for you?");
        System.out.println("____________________________________________________________");

        while (true) {
            String line = sc.nextLine().trim();
            try {
                if (line.equals("bye")) {
                    printGoodbye();
                    break;
                } else if (line.equals("list")) {
                    printList(lol);
                } else if (line.toLowerCase().startsWith("mark ")) {
                    handleMark(line, lol);
                } else if (line.toLowerCase().startsWith("unmark ")) {
                    handleUnmark(line, lol);
                } else if (line.toLowerCase().startsWith("todo")) {
                    handleTodo(line, lol);
                } else if (line.toLowerCase().startsWith("deadline")) {
                    handleDeadline(line, lol);
                } else if (line.toLowerCase().startsWith("event")) {
                    handleEvent(line, lol);
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

    private static void handleTodo(String line, ArrayList<Task> tasks) throws MeowException {
        String rest = line.length() > 4 ? line.substring(4).trim() : "";
        if (rest.isEmpty()) throw new MeowException("The description of a todo cannot be empty. MEOW!!");
        Task t = Task.createToDo(rest);
        tasks.add(t);
        System.out.println("____________________________________________________________");
        System.out.println("Got it. I've added this task:");
        System.out.println("  " + t);
        System.out.println("Now you have " + tasks.size() + " tasks in the list");
        System.out.println("____________________________________________________________");
    }

    private static void handleDeadline(String line, ArrayList<Task> tasks) throws MeowException {
        int byIndex = indexOfIgnoreCase(line, "/by");
        if (line.length() <= 8 || byIndex == -1) throw new MeowException("The deadline command requires a description and '/by <time>'. MEOW!!");
        String desc = line.substring(8, byIndex).trim();
        String by = line.substring(byIndex + 3).trim();
        if (desc.isEmpty()) throw new MeowException("The description of a deadline cannot be empty. MEOW!!");
        if (by.isEmpty()) throw new MeowException("A deadline must have a '/by' time. MEOW!! ");
        Task t = Task.createDeadline(desc, by);
        tasks.add(t);
        System.out.println("____________________________________________________________");
        System.out.println("Got it. I've added this task:");
        System.out.println("  " + t);
        System.out.println("Now you have " + tasks.size() + " tasks in the list");
        System.out.println("____________________________________________________________");
    }

    private static void handleEvent(String line, ArrayList<Task> tasks) throws MeowException {
        int fromIndex = indexOfIgnoreCase(line, "/from");
        int toIndex = indexOfIgnoreCase(line, "/to");
        if (line.length() <= 5 || fromIndex == -1 || toIndex == -1) throw new MeowException("The event command requires '/from' and '/to'. MEOW!!");
        String desc = line.substring(5, fromIndex).trim();
        String from = line.substring(fromIndex + 5, toIndex).trim();
        String to = line.substring(toIndex + 3).trim();
        if (desc.isEmpty()) throw new MeowException("The description of an event cannot be empty. MEOW!!");
        if (from.isEmpty() || to.isEmpty()) throw new MeowException("An event must have both '/from' and '/to' values. MEOW!!");
        Task t = Task.createEvent(desc, from, to);
        tasks.add(t);
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
            System.out.println("____________________________________________________________");
            System.out.println("Nice! I've marked this task as done:");
            System.out.println("  " + tasks.get(idx));
            System.out.println("____________________________________________________________");
        } catch (NumberFormatException e) {
            throw new MeowException("Please provide a valid task number after 'mark'. MEOW!!");
        }
    }

    private static void handleUnmark(String line, ArrayList<Task> tasks) throws MeowException {
        try {
            int idx = Integer.parseInt(line.substring(7).trim()) - 1;
            if (idx < 0 || idx >= tasks.size()) throw new MeowException("This number does not align with the tasks you have");
            tasks.get(idx).markUndone();
            System.out.println("____________________________________________________________");
            System.out.println("OK, I've marked this task as not done yet:");
            System.out.println("  " + tasks.get(idx));
            System.out.println("____________________________________________________________");
        } catch (NumberFormatException e) {
            throw new MeowException("MEOW!! Please provide a valid task number after 'unmark'.");
        }
    }

    private static void printList(ArrayList<Task> tasks) {
        System.out.println("____________________________________________________________");
        if (tasks.isEmpty()) {
            System.out.println("Here are the tasks in your list:");
            System.out.println("(no tasks yet)");
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

class Task {
    private String kind;
    private String description;
    private boolean isDone;
    private String by;
    private String from;
    private String to;

    private Task(String kind, String desc) {
        this.kind = kind;
        this.description = desc;
        this.isDone = false;
    }

    public static Task createToDo(String desc) { return new Task("T", desc); }
    public static Task createDeadline(String desc, String by) {
        Task t = new Task("D", desc);
        t.by = by;
        return t;
    }
    public static Task createEvent(String desc, String from, String to) {
        Task t = new Task("E", desc);
        t.from = from;
        t.to = to;
        return t;
    }

    public void markDone() { isDone = true; }
    public void markUndone() { isDone = false; }

    private String status() { return isDone ? "[X]" : "[ ]"; }

    @Override
    public String toString() {
        if ("D".equals(kind)) {
            return "[D]" + status() + " " + description + " (by: " + by + ")";
        } else if ("E".equals(kind)) {
            return "[E]" + status() + " " + description + " (from: " + from + " to: " + to + ")";
        } else {
            return "[T]" + status() + " " + description;
        }
    }
}
