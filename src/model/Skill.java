package model;

/**
 * Represents one vocational skill unit/course tracked by the platform.
 */
public class Skill {
    public enum SkillLevel {
        BEGINNER,
        INTERMEDIATE,
        ADVANCED
    }
    private String     skillId;
    private String     skillName;
    private SkillLevel level;
    private boolean    isCompleted;

    public Skill(String skillId, String skillName, SkillLevel level) {
        this.skillId     = skillId;
        this.skillName   = skillName;
        this.level       = level;
        this.isCompleted = false;
    }

    public void markCompleted() {
        isCompleted = true;
        System.out.println("Skill '" + skillName + "' marked as completed.");
    }


    public String     getSkillId()   { return skillId;     }
    public String     getSkillName() { return skillName;   }
    public SkillLevel getLevel()     { return level;       }
    public boolean    isCompleted()  { return isCompleted; }

    @Override
    public String toString() {
        return String.format("Skill{id='%s', name='%s', level=%s, completed=%b}",
                skillId, skillName, level, isCompleted);
    }
}
