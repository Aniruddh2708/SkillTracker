package model;

/**
 * Abstraction for skill assessment strategies.
 *
 * `assess(...)` provides shared workflow while `evaluate(...)` lets each
 * strategy decide how to score a trainee.
 */
public abstract class SkillAssessor {
    protected String assessorId;
    protected String assessorName;

    protected SkillAssessor(String assessorId, String assessorName) {
        this.assessorId   = assessorId;
        this.assessorName = assessorName;
    }
    /**
     * Strategy method implemented by concrete assessors.
     */
    public abstract int evaluate(Trainee trainee, Skill skill);
    public int assess(Trainee trainee, Skill skill) {
        int score = evaluate(trainee, skill);

        if (score >= 80) {
            skill.markCompleted();
            trainee.updateProgress(score);
        }

        System.out.printf("Assessment by %s | Trainee: %s | Skill: %s | Score: %d/100%n",
                assessorName, trainee.getName(), skill.getSkillName(), score);
        return score;
    }
    public String getAssessorId()   { 
        return assessorId;   
    }
    public String getAssessorName() { 
        return assessorName; 
    }
    @Override
    public String toString() {
        return String.format("SkillAssessor{id='%s', name='%s'}", assessorId, assessorName);
    }
}



class BasicAssessor extends SkillAssessor {
    private final int fixedScore;

    public BasicAssessor(String assessorId, String assessorName, int fixedScore) {
        super(assessorId, assessorName);
        this.fixedScore = Math.max(0, Math.min(100, fixedScore));
    }
    @Override
    public int evaluate(Trainee trainee, Skill skill) {
        return fixedScore;
    }

    @Override
    public String toString() {
        return String.format("BasicAssessor{id='%s', name='%s', fixedScore=%d}",
                assessorId, assessorName, fixedScore);
    }
}



class AdvancedAssessor extends SkillAssessor {

    public AdvancedAssessor(String assessorId, String assessorName) {
        super(assessorId, assessorName);
    }

    @Override
    public int evaluate(Trainee trainee, Skill skill) {
        double base = trainee.getCompletionPercent();

        double multiplier;
        switch (skill.getLevel()) {
            case BEGINNER:
                multiplier = 1.0;
                break;
            case INTERMEDIATE:
                multiplier = 0.9;
                break;
            case ADVANCED:
                multiplier = 0.8;
                break;
            default:
                multiplier = 1.0;
        }

        return (int) (base * multiplier);
    }

    @Override
    public String toString() {
        return String.format("AdvancedAssessor{id='%s', name='%s'}", assessorId, assessorName);
    }
}
