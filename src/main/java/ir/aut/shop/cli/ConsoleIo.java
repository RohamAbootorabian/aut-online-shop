package ir.aut.shop.cli;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * The only place in the program that reads the keyboard and writes the screen.
 *
 * <p>Keeping the terminal behind one class means the rest of the presentation layer is
 * written in terms of "ask for a line" rather than Scanner and System.out, and no other
 * layer touches either.
 */
public class ConsoleIo {

    private static final String PROMPT_SUFFIX = ": ";

    private final Scanner scanner;

    /**
     * REQUIRES: scanner is non-null.
     * EFFECTS: Builds the console over the given input source.
     *
     * @param scanner where typed input is read from
     */
    public ConsoleIo(Scanner scanner) {
        if (scanner == null) {
            throw new IllegalArgumentException("Scanner must not be null");
        }
        this.scanner = scanner;
    }

    /**
     * EFFECTS: Writes one line to the screen.
     *
     * @param message what to show
     */
    public void print(String message) {
        System.out.println(message);
    }

    /**
     * EFFECTS: Writes a blank line to the screen.
     */
    public void printBlankLine() {
        System.out.println();
    }

    /**
     * EFFECTS: Writes a heading with a rule under it.
     *
     * @param title the heading text
     */
    public void printTitle(String title) {
        System.out.println();
        System.out.println("=== " + title + " ===");
    }

    /**
     * EFFECTS: Writes a message marked as a problem.
     *
     * @param message what went wrong
     */
    public void printError(String message) {
        System.out.println("! " + message);
    }

    /**
     * EFFECTS: Writes a message marked as a success.
     *
     * @param message what happened
     */
    public void printSuccess(String message) {
        System.out.println("> " + message);
    }

    /**
     * EFFECTS: Writes a numbered list, or a note when the list is empty.
     *
     * @param items the lines to show
     * @param emptyNote what to say when there is nothing to show
     */
    public void printNumberedList(List<String> items, String emptyNote) {
        if (items.isEmpty()) {
            print(emptyNote);
            return;
        }
        for (int i = 0; i < items.size(); i++) {
            print("  " + (i + 1) + ") " + items.get(i));
        }
    }

    /**
     * REQUIRES: prompt is non-null.
     * EFFECTS: Asks for a line and returns it, repeating the question until something
     * other than blank is typed.
     *
     * @param prompt what to ask for
     * @return the typed text, trimmed
     */
    public String readLine(String prompt) {
        while (true) {
            System.out.print(prompt + PROMPT_SUFFIX);
            String typed = scanner.nextLine().trim();
            if (!typed.isEmpty()) {
                return typed;
            }
            printError("This cannot be left empty.");
        }
    }

    /**
     * REQUIRES: prompt is non-null.
     * EFFECTS: Asks for a line that may be left blank.
     *
     * @param prompt what to ask for
     * @return the typed text, or empty when nothing was typed
     */
    public Optional<String> readOptionalLine(String prompt) {
        System.out.print(prompt + " (press enter to skip)" + PROMPT_SUFFIX);
        String typed = scanner.nextLine().trim();
        return typed.isEmpty() ? Optional.empty() : Optional.of(typed);
    }

    /**
     * REQUIRES: prompt is non-null and lowest is at most highest.
     * EFFECTS: Asks for a whole number inside the range, repeating the question until one
     * is typed.
     *
     * @param prompt what to ask for
     * @param lowest smallest acceptable number
     * @param highest largest acceptable number
     * @return the typed number
     */
    public int readInt(String prompt, int lowest, int highest) {
        while (true) {
            String typed = readLine(prompt + " [" + lowest + "-" + highest + "]");
            try {
                int value = Integer.parseInt(typed);
                if (value >= lowest && value <= highest) {
                    return value;
                }
                printError("Please type a number between " + lowest + " and " + highest + ".");
            } catch (NumberFormatException ignored) {
                printError("\"" + typed + "\" is not a whole number.");
            }
        }
    }

    /**
     * REQUIRES: prompt is non-null.
     * EFFECTS: Asks a yes or no question.
     *
     * @param prompt what to ask
     * @return true when the answer starts with y
     */
    public boolean confirm(String prompt) {
        String typed = readLine(prompt + " (y/n)").toLowerCase();
        return typed.startsWith("y");
    }

    /**
     * REQUIRES: items is non-null and prompt is non-null.
     * EFFECTS: Shows a numbered list and asks which entry to use.
     *
     * @param <T> what kind of thing is being chosen
     * @param prompt what to ask
     * @param items the things to choose between
     * @param labels how each thing should read on screen
     * @return the chosen thing, or empty when the list was empty
     */
    public <T> Optional<T> chooseFrom(String prompt, List<T> items, List<String> labels) {
        if (items.isEmpty()) {
            return Optional.empty();
        }
        printNumberedList(labels, "Nothing to choose from.");
        int chosen = readInt(prompt, 1, items.size());
        return Optional.of(items.get(chosen - 1));
    }
}
