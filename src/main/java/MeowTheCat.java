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
            if (line.equals("bye")) {
                printGoodbye();
                break;
            } else if (line.equals("list")) {
                printList(lol);
            } else if (line.startsWith("mark ")) {

                    int idx = Integer.parseInt(line.substring(5).trim()) - 1;
                    if (idx < 0 || idx >= lol.size()) {
                        System.out.println("You put a number that doesn't align with the tasks you have.");
                    } else {
                        lol.get(idx).markDone();
                        System.out.println("____________________________________________________________");
                        System.out.println("Nice! I've marked this task as done:");
                        System.out.println("  " + lol.get(idx));
                        System.out.println("____________________________________________________________");
                    }

            } else if (line.startsWith("unmark ")) {

                    int idx = Integer.parseInt(line.substring(7).trim()) - 1;
                    if (idx < 0 || idx >= lol.size()) {
                        System.out.println("You put a number that doesn't align with the tasks you have.");
                    } else {
                        lol.get(idx).markUndone();
                        System.out.println("____________________________________________________________");
                        System.out.println("OK, I've marked this task as not done yet:");
                        System.out.println("  " + lol.get(idx));
                        System.out.println("____________________________________________________________");
                    }

            } else if (line.startsWith("todo ")) {
                String desc = line.substring(5).trim();
                    Task t = Task.createToDo(desc);
                    lol.add(t);
                    System.out.println("____________________________________________________________");
                    System.out.println("Got it. I've added this task:");
                    System.out.println("  " + t);
                    System.out.println("Now you have " + lol.size() + " tasks in the list.");
                    System.out.println("____________________________________________________________");

            } else if (line.startsWith("deadline ")) {
                int byIndex = indexOfIgnoreCase(line, "/by");
                    String desc = line.substring(9, byIndex).trim();
                    String by = line.substring(byIndex + 3).trim();
                    if (desc.isEmpty()) {
                        System.out.println("The description of a deadline cannot be empty.");
                    } else if (by.isEmpty()) {
                        System.out.println("A deadline must have a '/by' time.");
                    } else {
                        Task t = Task.createDeadline(desc, by);
                        lol.add(t);
                        System.out.println("____________________________________________________________");
                        System.out.println("Got it. I've added this task:");
                        System.out.println("  " + t);
                        System.out.println("Now you have " + lol.size() + " tasks in the list.");
                        System.out.println("____________________________________________________________");
                    }

            } else if (line.startsWith("event ")) {
                int fromIndex = indexOfIgnoreCase(line, "/from");
                int toIndex = indexOfIgnoreCase(line, "/to");
                    String desc = line.substring(6, fromIndex).trim();
                    String from = line.substring(fromIndex + 5, toIndex).trim();
                    String to = line.substring(toIndex + 3).trim();
                    if (desc.isEmpty()) {
                        System.out.println("The description of an event cannot be empty.");
                    } else if (from.isEmpty() || to.isEmpty()) {
                        System.out.println("An event must have both '/from' and '/to' values.");
                    } else {
                        Task t = Task.createEvent(desc, from, to);
                        lol.add(t);
                        System.out.println("____________________________________________________________");
                        System.out.println("Got it. I've added this task:");
                        System.out.println("  " + t);
                        System.out.println("Now you have " + lol.size() + " tasks in the list.");
                        System.out.println("____________________________________________________________");
                    }

            } else {
                    Task t = Task.createToDo(line);
                    lol.add(t);
                    System.out.println("____________________________________________________________");
                    System.out.println("added: " + line);
                    System.out.println("____________________________________________________________");

            }
        }

        sc.close();
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
