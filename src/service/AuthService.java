package service;

import db.UserDAO;
import model.Trainee;
import model.Trainer;
import model.User;

import java.sql.SQLException;

public class AuthService {

    private final UserDAO userDAO;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public User login(String email, String password) throws AuthException {
        try {
            Trainer trainer = userDAO.findTrainerByEmail(email);
            if (trainer != null) {
                if (!trainer.login(password)) {
                    throw new AuthException("Invalid credentials");
                }
                return trainer;
            }

            Trainee trainee = userDAO.findTraineeByEmail(email);
            if (trainee != null) {
                if (!trainee.login(password)) {
                    throw new AuthException("Invalid credentials");
                }
                return trainee;
            }

            throw new AuthException("User not found");

        } catch (SQLException e) {
            throw new AuthException("Login failed due to database error", e);
        }
    }
}
