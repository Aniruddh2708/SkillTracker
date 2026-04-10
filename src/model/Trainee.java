package model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Trainee model tracked by a specific trainer.
 *
 * Notes:
 * - Implements Serializable so trainee snapshots can be stored/transferred.
 * - Progress state (`completionPercent`) drives certification status.
 */
public class Trainee extends User implements Serializable {

    private static final long serialVersionUID = 1L;

    private ArrayList<Skill> enrolledSkills;
    private String trainerId;
    private int completionPercent;
    private boolean isCertified;

    public Trainee(String userId, String name, String email,
                   String passwordHash, String trainerId) {
        super(userId, name, email, passwordHash);
        this.enrolledSkills   = new ArrayList<>();
        this.trainerId        = trainerId;
        this.completionPercent = 0;
        this.isCertified      = false;
    }

    @Override
    public boolean login(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        return password.equals(getPasswordHash());
    }

    @Override
    public String getRole() {
        return "TRAINEE";
    }

    public void enrollInSkill(Skill skill) {
        if (!enrolledSkills.contains(skill)) {
            enrolledSkills.add(skill);
            System.out.println(getName() + " enrolled in: " + skill.getSkillName());
        }
    }

    public void updateProgress(int percent) {
        // Clamp progress into valid range [0,100] to avoid inconsistent state.
        this.completionPercent = Math.max(0, Math.min(100, percent));
        if (this.completionPercent >= 100) {
            isCertified = true;
            System.out.println("[CERTIFIED] " + getName() + " is now certified!");
        }
    }

    public void exportPortfolio() {
        System.out.println("=== Portfolio: " + getName() + " ===");
        System.out.println("Email   : " + getEmail());
        System.out.println("Skills  : " + enrolledSkills);
        System.out.println("Progress: " + completionPercent + "%");
        System.out.println("Certified: " + isCertified);
    }


    public ArrayList<Skill> getEnrolledSkills()  { return enrolledSkills;      }
    public String            getTrainerId()        { return trainerId;           }
    public int               getCompletionPercent(){ return completionPercent;   }
    public boolean           isCertified()         { return isCertified;         }


    @Override
    public String toString() {
        return String.format("[TRAINEE] %s <%s> (ID: %s) - %d%% complete",
                getName(), getEmail(), getUserId(), completionPercent);
    }
}
