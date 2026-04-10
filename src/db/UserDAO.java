package db;

import model.Skill;
import model.Skill.SkillLevel;
import model.Trainee;
import model.Trainer;
import model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for User-related entities (Trainers and Trainees).
 * 
 * Handles all database interactions using the Singleton DBConnection.
 */
public class UserDAO {

    // --- SQL CONSTANTS ---
    private static final String SELECT_TRAINER_BY_EMAIL = "SELECT user_id, name, email, password_hash FROM trainers WHERE email = ?";
    private static final String SELECT_TRAINEE_BY_EMAIL = "SELECT user_id, name, email, password_hash, trainer_id, completion_percent FROM trainees WHERE email = ?";
    private static final String SELECT_SKILLS_BY_TRAINEE = "SELECT s.skill_id, s.skill_name, s.category, s.level, ts.completed, ts.score " +
                                                           "FROM skills s JOIN trainee_skills ts ON s.skill_id = ts.skill_id " +
                                                           "WHERE ts.trainee_id = ?";
    private static final String INSERT_TRAINER = "INSERT INTO trainers (user_id, name, email, password_hash) VALUES (?, ?, ?, ?)";
    private static final String INSERT_TRAINEE = "INSERT INTO trainees (user_id, name, email, password_hash, trainer_id) VALUES (?, ?, ?, ?, ?)";
    private static final String SELECT_ALL_TRAINERS = "SELECT user_id, name, email, password_hash FROM trainers";
    private static final String SELECT_ALL_TRAINEES = "SELECT user_id, name, email, password_hash, trainer_id, completion_percent FROM trainees";
    private static final String SELECT_ALL_SKILLS = "SELECT skill_id, skill_name, category, level FROM skills";
    private static final String ASSIGN_SKILL = "INSERT IGNORE INTO trainee_skills (trainee_id, skill_id) VALUES (?, ?)";
    private static final String UPDATE_SKILL_PROGRESS = "UPDATE trainee_skills SET completed = ?, score = ? WHERE trainee_id = ? AND skill_id = ?";

    /**
     * Finds a Trainer by email.
     */
    public Trainer findTrainerByEmail(String email) throws SQLException {
        try (PreparedStatement ps = DBConnection.getInstance().prepareStatement(SELECT_TRAINER_BY_EMAIL)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapTrainer(rs) : null;
            }
        }
    }

    /**
     * Finds a Trainee by email, fully hydrating their skill list.
     */
    public Trainee findTraineeByEmail(String email) throws SQLException {
        try (PreparedStatement ps = DBConnection.getInstance().prepareStatement(SELECT_TRAINEE_BY_EMAIL)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Trainee trainee = mapTrainee(rs);
                    loadSkillsToTrainee(trainee);
                    return trainee;
                }
            }
        }
        return null;
    }

    /**
     * Hydrates a Trainee object with their assigned skills from the database.
     */
    private void loadSkillsToTrainee(Trainee trainee) throws SQLException {
        try (PreparedStatement ps = DBConnection.getInstance().prepareStatement(SELECT_SKILLS_BY_TRAINEE)) {
            ps.setString(1, trainee.getUserId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Skill skill = mapSkill(rs);
                    skill.setCompleted(rs.getBoolean("completed"));
                    skill.setScore(rs.getInt("score"));
                    trainee.enrollInSkill(skill);
                }
            }
        }
    }

    public void saveTrainer(Trainer trainer) throws SQLException {
        try (PreparedStatement ps = DBConnection.getInstance().prepareStatement(INSERT_TRAINER)) {
            ps.setString(1, trainer.getUserId());
            ps.setString(2, trainer.getName());
            ps.setString(3, trainer.getEmail());
            ps.setString(4, trainer.getPasswordHash());
            ps.executeUpdate();
        }
    }

    public void saveTrainee(Trainee trainee) throws SQLException {
        try (PreparedStatement ps = DBConnection.getInstance().prepareStatement(INSERT_TRAINEE)) {
            ps.setString(1, trainee.getUserId());
            ps.setString(2, trainee.getName());
            ps.setString(3, trainee.getEmail());
            ps.setString(4, trainee.getPasswordHash());
            ps.setString(5, trainee.getTrainerId());
            ps.executeUpdate();
        }
    }

    /**
     * Fetches all registered users as a polymorphic list.
     */
    public List<User> findAll() throws SQLException {
        List<User> users = new ArrayList<>();
        try (PreparedStatement ps = DBConnection.getInstance().prepareStatement(SELECT_ALL_TRAINERS);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) users.add(mapTrainer(rs));
        }
        try (PreparedStatement ps = DBConnection.getInstance().prepareStatement(SELECT_ALL_TRAINEES);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) users.add(mapTrainee(rs));
        }
        return users;
    }

    /**
     * Lists all vocational skills defined in the system.
     */
    public List<Skill> getAllAvailableSkills() throws SQLException {
        List<Skill> skills = new ArrayList<>();
        try (PreparedStatement ps = DBConnection.getInstance().prepareStatement(SELECT_ALL_SKILLS);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) skills.add(mapSkill(rs));
        }
        return skills;
    }

    public void assignSkillToTrainee(String traineeId, String skillId) throws SQLException {
        try (PreparedStatement ps = DBConnection.getInstance().prepareStatement(ASSIGN_SKILL)) {
            ps.setString(1, traineeId);
            ps.setString(2, skillId);
            ps.executeUpdate();
        }
    }

    public void updateTraineeSkill(String traineeId, String skillId, boolean completed, int score) throws SQLException {
        try (PreparedStatement ps = DBConnection.getInstance().prepareStatement(UPDATE_SKILL_PROGRESS)) {
            ps.setBoolean(1, completed);
            ps.setInt(2, score);
            ps.setString(3, traineeId);
            ps.setString(4, skillId);
            ps.executeUpdate();
            updateOverallProgress(traineeId);
        }
    }

    private void updateOverallProgress(String traineeId) throws SQLException {
        String sql = "UPDATE trainees SET completion_percent = " +
                     " (SELECT (COUNT(*) * 100 / NULLIF((SELECT COUNT(*) FROM trainee_skills WHERE trainee_id = ?), 0)) " +
                     "  FROM trainee_skills WHERE trainee_id = ? AND completed = TRUE) " +
                     " WHERE user_id = ?";
        
        try (PreparedStatement ps = DBConnection.getInstance().prepareStatement(sql)) {
            ps.setString(1, traineeId);
            ps.setString(2, traineeId);
            ps.setString(3, traineeId);
            ps.executeUpdate();
        }
    }

    // --- MAPPING HELPERS ---

    private Trainer mapTrainer(ResultSet rs) throws SQLException {
        return new Trainer(
            rs.getString("user_id"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getString("password_hash")
        );
    }

    private Trainee mapTrainee(ResultSet rs) throws SQLException {
        Trainee t = new Trainee(
            rs.getString("user_id"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getString("password_hash"),
            rs.getString("trainer_id")
        );
        t.updateProgress(rs.getInt("completion_percent"));
        return t;
    }

    private Skill mapSkill(ResultSet rs) throws SQLException {
        return new Skill(
            rs.getString("skill_id"),
            rs.getString("skill_name"),
            rs.getString("category"),
            SkillLevel.valueOf(rs.getString("level").toUpperCase())
        );
    }
}
