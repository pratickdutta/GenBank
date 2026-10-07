import ui.MainMenu;

import java.util.Scanner;

/**
 * Main — application entry point.
 *
 * Launches the Mini Banking System console application.
 *
 * Run command:
 *   java -cp "src;lib/mysql-connector-j.jar" Main
 */
public class Main {

    public static void main(String[] args) {

        // Single Scanner instance shared across all menus
        try (Scanner scanner = new Scanner(System.in)) {
            new MainMenu(scanner).run();
        }
    }
}
