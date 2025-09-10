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
    private static final String BORDER = "____________________________________________________________";
    private static final String NEWLINE = "\n";

    private final TaskCollection tasks;
    private final FileStore store;

    public MeowCat() {
        this.store = new FileStore(Paths.get("SaveFile.txt"));
        // store should be there
        assert this.store != null : "FileStore should not be null after construction";
        TaskCollection loadedTasks;
        try {
            List<Task> loaded = store.load();
            loadedTasks = new TaskCollection(loaded);
        } catch (IOException | MeowException e) {
            // on failure, start empty (GUI should still work)
            loadedTasks = new TaskCollection();
            // sanity: tasks collection must be non-null
            assert loadedTasks != null : "loadedTasks must not be null";
        }

        assert loadedTasks != null : "loadedTasks must not be null";
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
        // tasks should be non-null
        assert tasks != null : "tasks must not be null when handling input";
        try {
            String line = input.trim();
            String cmd = CommandParser.commandType(line);

            switch (cmd) {
            case "bye":
                return borderedMessage("Bye. Hope to see you again soon!");
            case "list":
                return handleList();
            case "mark":
                return handleMark(line);
            case "unmark":
                return handleUnmark(line);
            case "delete":
                return handleDelete(line);
            case "todo":
                return handleTodo(line);
            case "deadline":
                return handleDeadline(line);
            case "event":
                return handleEvent(line);
            case "clear":
                return handleClear();
            case "find":
                return handleFind(line);
            default:
                throw new MeowException("MEOW!! MEOW is Confused!!");
            }
        } catch (MeowException me) {
            return borderedMessage("MEOW OOPS!!! " + me.getMessage());
        } catch (Exception e) {
            return borderedMessage("MEOW OOPS!!! Something went wrong: " + e.getMessage());
        }
    }

    /* -------------------------
       Command handler helpers
       ------------------------- */

    private String handleList() {
        StringBuilder sb = new StringBuilder();
        sb.append("Here are the tasks in your list:").append(NEWLINE);
        List<Task> all = tasks.getAll();
        for (int i = 0; i < all.size(); i++) {
            sb.append((i + 1)).append(".").append(all.get(i)).append(NEWLINE);
        }
        return borderedMessage(sb.toString());
    }

    private String handleMark(String line) throws MeowException {
        int idx = CommandParser.parseIndex(line, "mark");
        Task t = tasks.markDone(idx);
        String saveErr = safeSave("mark");
        String body = "Nice! I've marked this task as done:" + NEWLINE + "  " + t;
        return appendSaveErrAndBorder(body, saveErr);
    }

    private String handleUnmark(String line) throws MeowException {
        int idx = CommandParser.parseIndex(line, "unmark");
        Task t = tasks.markUndone(idx);
        String saveErr = safeSave("unmark");
        String body = "OK, I've marked this task as not done yet:" + NEWLINE + "  " + t;
        return appendSaveErrAndBorder(body, saveErr);
    }

    private String handleDelete(String line) throws MeowException {
        int idx = CommandParser.parseIndex(line, "delete");
        Task removed = tasks.delete(idx);
        String saveErr = safeSave("delete");
        StringBuilder sb = new StringBuilder();
        sb.append("Meow has Noted. I've removed this task:").append(NEWLINE);
        sb.append("  ").append(removed).append(NEWLINE);
        sb.append("Now you have ").append(tasks.size()).append(" tasks in the list").append(NEWLINE);
        return appendSaveErrAndBorder(sb.toString(), saveErr);
    }

    private String handleTodo(String line) throws MeowException {
        String desc = CommandParser.parseTodoDesc(line);
        Task t = new ToDo(desc);
        tasks.add(t);
        String saveErr = safeSave("add-todo");
        String body = "Got it. I've added this task:" + NEWLINE + "  " + t
            + NEWLINE + "Now you have " + tasks.size() + " tasks in the list";
        return appendSaveErrAndBorder(body, saveErr);
    }

    private String handleDeadline(String line) throws MeowException {
        String[] parts = CommandParser.parseDeadlineParts(line);
        String desc = parts[0];
        String dateRaw = parts[1];
        LocalDateTimeHolder holder = DateTimeUtil.obtainValuesDate(dateRaw);
        Task t = new Deadline(desc, holder);
        tasks.add(t);
        String saveErr = safeSave("add-deadline");
        String body = "Got it. I've added this task:" + NEWLINE + "  " + t
            + NEWLINE + "Now you have " + tasks.size() + " tasks in the list";
        return appendSaveErrAndBorder(body, saveErr);
    }

    private String handleEvent(String line) throws MeowException {
        String[] parts = CommandParser.parseEventParts(line);
        String desc = parts[0];
        String fromRaw = parts[1];
        String toRaw = parts[2];
        LocalDateTimeHolder fromH = DateTimeUtil.obtainValuesDate(fromRaw);
        LocalDateTimeHolder toH = DateTimeUtil.obtainValuesDate(toRaw);
        Task t = new Event(desc, fromH, toH);
        tasks.add(t);
        String saveErr = safeSave("add-event");
        String body = "Got it. I've added this task:" + NEWLINE + "  " + t
            + NEWLINE + "Now you have " + tasks.size() + " tasks in the list";
        return appendSaveErrAndBorder(body, saveErr);
    }

    private String handleClear() {
        tasks.clear();
        String saveErr = safeSave("clearing all tasks");
        String body = "All tasks have been cleared!";
        return appendSaveErrAndBorder(body, saveErr);
    }

    private String handleFind(String line) throws MeowException {
        String keyword = CommandParser.parseFindQuery(line);
        List<Task> matches = tasks.find(keyword);
        StringBuilder sb = new StringBuilder();
        sb.append("Here are the matching tasks in your list:").append(NEWLINE);
        for (int i = 0; i < matches.size(); i++) {
            sb.append((i + 1)).append(".").append(matches.get(i)).append(NEWLINE);
        }
        return borderedMessage(sb.toString());
    }

    /* -------------------------
       Small formatting helpers
       ------------------------- */

    private String borderedMessage(String body) {
        return BORDER + NEWLINE + body + NEWLINE + BORDER;
    }

    private String appendSaveErrAndBorder(String body, String saveErr) {
        StringBuilder sb = new StringBuilder();
        sb.append(body);
        if (saveErr != null) {
            sb.append(NEWLINE).append(saveErr);
        }
        return borderedMessage(sb.toString());
    }

    /**
     * Save and return an error string if saving failed, otherwise null.
     */
    private String safeSave(String action) {
        assert action != null : "safeSave action must not be null";
        try {
            store.save(tasks.getAll());
            return null;
        } catch (IOException e) {
            return "MEOW OOPS!!! Could not save after " + action + ": " + e.getMessage();
        }
    }
}
