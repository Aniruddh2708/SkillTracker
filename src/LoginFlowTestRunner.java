import service.AuthException;
import service.AuthService;
import model.User;
import model.Trainee;
import model.Trainer;
import model.Skill;
import model.Skill.SkillLevel;

public class LoginFlowTestRunner {

    public static void main(String[] args) {
        System.out.println("=== Login Flow Tests ===\n");
        runLoginTests();

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
        Trainer trainer = new Trainer("TR-TEST", "Test Trainer", "t@test.com", "pass123");
        Trainee trainee = new Trainee("TN-TEST", "Test Trainee", "s@test.com", "abc123", "TR-TEST");

        trainer.enrollTrainee(trainee);
        trainee.enrollInSkill("Basic Wiring");
        trainee.updateProgress(85);

        System.out.println(trainer);
        System.out.println(trainee);
        trainee.exportPortfolio();

        System.out.println("\n--- Skill Assessment ---");
        Skill wiring = new Skill("SK-001", "Basic Wiring", SkillLevel.BEGINNER);
        System.out.println("Before: " + wiring);

        wiring.markCompleted();
        System.out.println("After:  " + wiring);
    }
}
