package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Trainer extends User {

    private List<Trainee> roster;

    public Trainer(String userId, String name, String email, String passwordHash) {
        super(userId, name, email, passwordHash);
        this.roster = new ArrayList<>();
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
        return "TRAINER";
    }

    public void enrollTrainee(Trainee trainee) {
        if (trainee == null) {
            throw new IllegalArgumentException("Trainee cannot be null");
        }
        roster.add(trainee);
    }

    public List<Trainee> getRoster() {
        return Collections.unmodifiableList(roster);
    }


    @Override
    public String toString() {
        return String.format("Trainer{base=%s, roster=%d trainee(s)}", super.toString(), roster.size());
    }
}
