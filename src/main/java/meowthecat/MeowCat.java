package meowthecat;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;



/**
 * Small wrapper that exposes MeowTheCat-like behavior as a single-step
 * request/response API for the GUI. It keeps a TaskCollection and FileStore
 * and returns the same user-visible strings that ConsoleUI would print.
 */
public class MeowCat {
    private final TaskCollection tasks;
    private final FileStore store;

    public MeowCat() {
        this.store = new FileStore(Paths.get("SaveFile.txt"));
        TaskCollection loadedTasks;
        try {
            List<Task> loaded = store.load();
            loadedTasks = new TaskCollection(loaded);
        } catch (IOException | MeowException e) {
            // on failure, start empty (GUI should still work)
            loadedTasks = new TaskCollection();
        }
        this.tasks = loadedTasks;
    }

    /**
     * Handle a single input line and produce the string that would be shown
     * to the user (matching ConsoleUI wording where practical).
     *
     * @param input user input
     * @return response string to display in GUI
     */
    public String getResponse(String input) {
        try {
            if (input == null) {
                return "MEOW OOPS!!! No input provided.";
            }
            String line = input.trim();
            String cmd = CommandParser.commandType(line);

            switch (cmd) {
            case "bye":
                return "____________________________________________________________\n"
                    + "Bye. Hope to see you again soon!\n"
                    + "____________________________________________________________";
            case "list": {
                StringBuilder sb = new StringBuilder();
                sb.append("____________________________________________________________\n");
                sb.append("Here are the tasks in your list:\n");
                List<Task> all = tasks.getAll();
                for (int i = 0; i < all.size(); i++) {
                    sb.append((i + 1)).append(".").append(all.get(i)).append("\n");
                }
                sb.append("____________________________________________________________");
                return sb.toString();
            }
            case "mark": {
                int idx = CommandParser.parseIndex(line, "mark");
                Task t = tasks.markDone(idx);
                String saveErr = safeSave("mark");
                StringBuilder sb = new StringBuilder();
                sb.append("____________________________________________________________\n");
                sb.append("Nice! I've marked this task as done:\n");
                sb.append("  ").append(t).append("\n");
                sb.append("____________________________________________________________");
                if (saveErr != null) {
                    sb.append("\n").append(saveErr);
                }
                return sb.toString();
            }
            case "unmark": {
                int idx = CommandParser.parseIndex(line, "unmark");
                Task t = tasks.markUndone(idx);
                String saveErr = safeSave("unmark");
                StringBuilder sb = new StringBuilder();
                sb.append("____________________________________________________________\n");
                sb.append("OK, I've marked this task as not done yet:\n");
                sb.append("  ").append(t).append("\n");
                sb.append("____________________________________________________________");
                if (saveErr != null) {
                    sb.append("\n").append(saveErr);
                }
                return sb.toString();
            }
            case "delete": {
                int idx = CommandParser.parseIndex(line, "delete");
                Task removed = tasks.delete(idx);
                String saveErr = safeSave("delete");
                StringBuilder sb = new StringBuilder();
                sb.append("____________________________________________________________\n");
                sb.append("Meow has Noted. I've removed this task:\n");
                sb.append("  ").append(removed).append("\n");
                sb.append("Now you have ").append(tasks.size()).append(" tasks in the list\n");
                sb.append("____________________________________________________________");
                if (saveErr != null) {
                    sb.append("\n").append(saveErr);
                }
                return sb.toString();
            }
            case "todo": {
                String desc = CommandParser.parseTodoDesc(line);
                Task t = new ToDo(desc);
                tasks.add(t);
                String saveErr = safeSave("add-todo");
                StringBuilder sb = new StringBuilder();
                sb.append("____________________________________________________________\n");
                sb.append("Got it. I've added this task:\n");
                sb.append("  ").append(t).append("\n");
                sb.append("Now you have ").append(tasks.size()).append(" tasks in the list\n");
                sb.append("____________________________________________________________");
                if (saveErr != null) {
                    sb.append("\n").append(saveErr);
                }
                return sb.toString();
            }
            case "deadline": {
                String[] parts = CommandParser.parseDeadlineParts(line);
                String desc = parts[0];
                String dateRaw = parts[1];
                LocalDateTimeHolder holder = DateTimeUtil.obtainValuesDate(dateRaw);
                Task t = new Deadline(desc, holder);
                tasks.add(t);
                String saveErr = safeSave("add-deadline");
                StringBuilder sb = new StringBuilder();
                sb.append("____________________________________________________________\n");
                sb.append("Got it. I've added this task:\n");
                sb.append("  ").append(t).append("\n");
                sb.append("Now you have ").append(tasks.size()).append(" tasks in the list\n");
                sb.append("____________________________________________________________");
                if (saveErr != null) {
                    sb.append("\n").append(saveErr);
                }
                return sb.toString();
            }
            case "event": {
                String[] parts = CommandParser.parseEventParts(line);
                String desc = parts[0];
                String fromRaw = parts[1];
                String toRaw = parts[2];
                LocalDateTimeHolder fromH = DateTimeUtil.obtainValuesDate(fromRaw);
                LocalDateTimeHolder toH = DateTimeUtil.obtainValuesDate(toRaw);
                Task t = new Event(desc, fromH, toH);
                tasks.add(t);
                String saveErr = safeSave("add-event");
                StringBuilder sb = new StringBuilder();
                sb.append("____________________________________________________________\n");
                sb.append("Got it. I've added this task:\n");
                sb.append("  ").append(t).append("\n");
                sb.append("Now you have ").append(tasks.size()).append(" tasks in the list\n");
                sb.append("____________________________________________________________");
                if (saveErr != null) {
                    sb.append("\n").append(saveErr);
                }
                return sb.toString();
            }
            case "clear": {
                tasks.clear();
                String saveErr = safeSave("clearing all tasks");
                StringBuilder sb = new StringBuilder();
                sb.append("____________________________________________________________\n");
                sb.append("All tasks have been cleared!\n");
                sb.append("____________________________________________________________");
                if (saveErr != null) {
                    sb.append("\n").append(saveErr);
                }
                return sb.toString();
            }
            case "find": {
                String keyword = CommandParser.parseFindQuery(line);
                List<Task> matches = tasks.find(keyword);
                StringBuilder sb = new StringBuilder();
                sb.append("____________________________________________________________\n");
                sb.append("Here are the matching tasks in your list:\n");
                for (int i = 0; i < matches.size(); i++) {
                    sb.append((i + 1)).append(".").append(matches.get(i)).append("\n");
                }
                sb.append("____________________________________________________________");
                return sb.toString();
            }
            default:
                throw new MeowException("MEOW!! MEOW is Confused!!");
            }
        } catch (MeowException me) {
            return "____________________________________________________________\nMEOW OOPS!!! " + me.getMessage()
                + "\n____________________________________________________________";
        } catch (Exception e) {
            return "____________________________________________________________\nMEOW OOPS!!! Something went wrong: "
                + e.getMessage() + "\n____________________________________________________________";
        }
    }

    /**
     * Save and return an error string if saving failed, otherwise null.
     */
    private String safeSave(String action) {
        try {
            store.save(tasks.getAll());
            return null;
        } catch (IOException e) {
            return "____________________________________________________________\nMEOW OOPS!!! Could not save after "
                + action + ": " + e.getMessage()
                + "\n____________________________________________________________";
        }
    }
}
