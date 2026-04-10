package db;

import model.Trainee;
import model.Trainer;
import model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    /**
     * Looks up a trainer by unique email.
     * Returns null instead of throwing when no match exists so callers can
     * compose role lookup logic naturally.
     */
    public Trainer findTrainerByEmail(String email) throws SQLException {
        String sql = "SELECT user_id, name, email, password_hash FROM trainers WHERE email = ?";

        try (PreparedStatement ps = DBConnection.getInstance().prepareStatement(sql)) {
            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Trainer(
                            rs.getString("user_id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("password_hash")
                    );
                }
            }
        }
        return null;
    }

    public Trainee findTraineeByEmail(String email) throws SQLException {
        String sql = "SELECT user_id, name, email, password_hash, trainer_id FROM trainees WHERE email = ?";

        try (PreparedStatement ps = DBConnection.getInstance().prepareStatement(sql)) {
            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Trainee(
                            rs.getString("user_id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("password_hash"),
                            rs.getString("trainer_id")
                    );
                }
            }
        }
        return null;
    }
    /**
     * Persists a trainer row using a prepared statement to avoid SQL injection.
     */
    public void saveTrainer(Trainer trainer) throws SQLException {
        String sql = "INSERT INTO trainers (user_id, name, email, password_hash) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = DBConnection.getInstance().prepareStatement(sql)) {
            ps.setString(1, trainer.getUserId());
            ps.setString(2, trainer.getName());
            ps.setString(3, trainer.getEmail());
            ps.setString(4, trainer.getPasswordHash());
            ps.executeUpdate();
        }
    }
    /**
     * Persists a trainee and links to the owning trainer via trainer_id.
     */
    public void saveTrainee(Trainee trainee) throws SQLException {
        String sql = "INSERT INTO trainees (user_id, name, email, password_hash, trainer_id) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = DBConnection.getInstance().prepareStatement(sql)) {
            ps.setString(1, trainee.getUserId());
            ps.setString(2, trainee.getName());
            ps.setString(3, trainee.getEmail());
            ps.setString(4, trainee.getPasswordHash());
            ps.setString(5, trainee.getTrainerId());
            ps.executeUpdate();
        }
    }
    /**
     * Loads both trainers and trainees as `User` polymorphic instances.
     * This is convenient for dashboards or admin views that need all users.
     */
    public List<User> findAll() throws SQLException {
        List<User> users = new ArrayList<>();

        String trainerSql = "SELECT user_id, name, email, password_hash FROM trainers";
        try (PreparedStatement ps = DBConnection.getInstance().prepareStatement(trainerSql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(new Trainer(
                        rs.getString("user_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password_hash")
                ));
            }
        }

        String traineeSql = "SELECT user_id, name, email, password_hash, trainer_id FROM trainees";
        try (PreparedStatement ps = DBConnection.getInstance().prepareStatement(traineeSql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(new Trainee(
                        rs.getString("user_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password_hash"),
                        rs.getString("trainer_id")
                ));
            }
        }
        return users;
    }
}
