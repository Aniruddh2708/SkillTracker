import service.AuthException;
import service.AuthService;
import model.User;
import model.Trainee;
import model.Trainer;
import model.Skill;
import model.Skill.SkillLevel;

/**
 * Console-only sanity test runner.
 *
 * Purpose:
 * - Quickly validate login/auth wiring against seeded DB users.
 * - Exercise core model behavior without launching JavaFX UI.
 */
public class LoginFlowTestRunner {

    public static void main(String[] args) {
        // Phase 1: integration-ish auth checks (depends on DB seed data).
        System.out.println("=== Login Flow Tests ===\n");
        runLoginTests();

        // Phase 2: pure model checks (works offline, no DB required).
        System.out.println("\n=== Offline Model Tests (no DB needed) ===\n");
        runModelTests();
    }


    private static void runLoginTests() {
        AuthService authService = new AuthService();

        try {
            User user = authService.login("riya@skillbridge.com", "abc123");
            System.out.println("SUCCESS (valid trainee): " + user);
        } catch (AuthException e) {
            System.out.println("FAIL (valid case): " + e.getMessage());
            if (e.getCause() != null) {
                System.out.println("Root cause:");
                e.getCause().printStackTrace();
            }
        }

        try {
            authService.login("noone@skillbridge.com", "abc123");
            System.out.println("FAIL: unknown email should not pass");
        } catch (AuthException e) {
            System.out.println("EXPECTED (unknown email): " + e.getMessage());
        }

        try {
            authService.login("riya@skillbridge.com", "");
            System.out.println("FAIL: empty password should not pass");
        } catch (AuthException e) {
            System.out.println("EXPECTED (empty password): " + e.getMessage());
        }
    }


    private static void runModelTests() {
        // Build in-memory sample objects to test OOP model interactions.
        Trainer trainer = new Trainer("TR-TEST", "Test Trainer", "t@test.com", "pass123");
        Trainee trainee = new Trainee("TN-TEST", "Test Trainee", "s@test.com", "abc123", "TR-TEST");

        trainer.enrollTrainee(trainee);
        Skill wiring = new Skill("SK-001", "Basic Wiring", "ELECTRICAL", SkillLevel.BEGINNER);
        trainee.enrollInSkill(wiring);
        trainee.updateProgress(85);

        System.out.println(trainer);
        System.out.println(trainee);
        trainee.exportPortfolio();

        System.out.println("\n--- Skill Assessment ---");
        System.out.println("Before: " + wiring);

        wiring.markCompleted();
        System.out.println("After:  " + wiring);
    }
}
