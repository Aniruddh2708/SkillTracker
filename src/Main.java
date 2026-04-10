import gui.LoginApp;
import javafx.application.Application;

/**
 * Main bootstrap class for SkillBridge.
 *
 * Why this class exists:
 * - Keeps a stable and simple entry point (`Main`) for IDE run configs and CLI usage.
 * - Delegates full UI lifecycle management to JavaFX's `Application.launch(...)`.
 * - Matches the structure described in the README (`src/Main.java`).
 */
public class Main {

    /**
     * Application entry point.
     *
     * This method does not create windows manually. Instead, it asks JavaFX to
     * start the `LoginApp` class, which then builds the login screen and routes
     * users to role-specific dashboards.
     */
    public static void main(String[] args) {
        Application.launch(LoginApp.class, args);
    }
}
