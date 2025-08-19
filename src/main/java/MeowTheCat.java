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
            } else {
                if (!line.isEmpty()) {
                    Task t = Task.createToDo(line);
                    lol.add(t);
                    System.out.println("____________________________________________________________");
                    System.out.println("added: " + line);
                    System.out.println("____________________________________________________________");
                }
            }
        }

        sc.close();
    }

    private static void printList(ArrayList<Task> lol) {
        System.out.println("____________________________________________________________");
        if (lol.isEmpty()) {
            System.out.println("Here are the tasks in your list:");
        } else {
            System.out.println("Here are the tasks in your list:");
            for (int i = 0; i < lol.size(); i++) {
                System.out.printf("%d.%s%n", i + 1, lol.get(i));
            }
        }
        System.out.println("____________________________________________________________");
    }

    private static void printGoodbye() {
        System.out.println("____________________________________________________________");
        System.out.println("Bye. Hope to see you again soon!");
        System.out.println("____________________________________________________________");
    }
}

class Task {
    private String description;
    private boolean isDone = false;

    private Task(String desc) {
        this.description = desc;
    }

    public static Task createToDo(String desc) {
        return new Task(desc);
    }

    public void markDone() { isDone = true; }
    public void markUndone() { isDone = false; }

    private String status() { return isDone ? "[X]" : "[ ]"; }

    @Override
    public String toString() {
        return "[T]" + status() + " " + description;
    }
}
