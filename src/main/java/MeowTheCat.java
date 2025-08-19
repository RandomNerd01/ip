import java.util.ArrayList;
import java.util.Scanner;

public class MeowTheCatLevel2 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        ArrayList<String> lol = new ArrayList<>(100);

        System.out.println("____________________________________________________________");
        System.out.println("Hello! I'm MeowTheCat");
        System.out.println("What can I do for you?");
        System.out.println("____________________________________________________________");

        while (true) {
            String line = sc.nextLine().trim();

            if (line.equalsIgnoreCase("bye")) {
                System.out.println("____________________________________________________________");
                System.out.println("Bye. Hope to see you again soon!");
                System.out.println("____________________________________________________________");
                break;
            } else if (line.equalsIgnoreCase("list")) {
                System.out.println("____________________________________________________________");
                if (lol.isEmpty()) {
                    System.out.println("Your task list is empty.");
                } else {
                    for (int i = 0; i < lol.size(); i++) {
                        System.out.printf("%d. %s%n", i + 1, lol.get(i));
                    }
                }
                System.out.println("____________________________________________________________");
            } else {
                lol.add(line);
                System.out.println("____________________________________________________________");
                System.out.println("added: " + line);
                System.out.println("____________________________________________________________");
            }
        }

        sc.close();
    }
}
