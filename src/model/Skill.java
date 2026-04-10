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
    private String     category;
    private SkillLevel level;
    private boolean    isCompleted;
    private int        score;

    public Skill(String skillId, String skillName, String category, SkillLevel level) {
        this.skillId     = skillId;
        this.skillName   = skillName;
        this.category    = category;
        this.level       = level;
        this.isCompleted = false;
        this.score       = 0;
    }

    public void markCompleted() {
        isCompleted = true;
        System.out.println("Skill '" + skillName + "' marked as completed.");
    }


    public String     getSkillId()   { return skillId;     }
    public String     getSkillName() { return skillName;   }
    public String     getCategory()  { return category;    }
    public SkillLevel getLevel()     { return level;       }
    public boolean    isCompleted()  { return isCompleted; }
    public int        getScore()     { return score;       }

    public void setScore(int score) { this.score = score; }
    public void setCompleted(boolean completed) { this.isCompleted = completed; }

    @Override
    public String toString() {
        return String.format("Skill{id='%s', name='%s', level=%s, completed=%b}",
                skillId, skillName, level, isCompleted);
    }
}
