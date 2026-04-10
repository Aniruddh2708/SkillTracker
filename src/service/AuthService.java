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

    /**
     * Authenticates a user by:
     * 1) checking trainer records first,
     * 2) checking trainee records second,
     * 3) validating password via each model's `login(...)` implementation.
     *
     * We wrap SQL failures in `AuthException` so UI/test layers don't need
     * to know database details.
     */
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
            // Include driver message so local setup issues (wrong password, DB missing) are visible.
            String detail = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            throw new AuthException("Login failed due to database error: " + detail, e);
        }
    }
}
