package model;

/**
 * Abstract base model for every authenticated person in the system.
 *
 * Common fields (id, name, email, password hash) are centralized here so
 * trainer/trainee classes only implement role-specific behavior.
 */
public abstract class User {

    private String userId;
    private String name;
    private String email;
    private String passwordHash;

    protected User(String userId, String name, String email, String passwordHash) {
        this.userId=userId;
        this.name=name;
        this.email=email;
        this.passwordHash=passwordHash;
    }

    /**
     * Validates the incoming password according to child-type policy.
     */
    public abstract boolean login(String password);

    /**
     * Returns role marker used for routing and display.
     */
    public abstract String getRole();

    public String getUserId()  { return userId; }
    public String getName()    { return name;   }
    public String getEmail()   { return email;  }

    public String getPasswordHash() { return passwordHash; }

    public void setName(String name)   { this.name  = name;  }
    public void setEmail(String email) { this.email = email; }

    @Override
    public String toString() {
        return String.format("User{id='%s', name='%s', role='%s'}", userId, name, getRole());
    }
}
