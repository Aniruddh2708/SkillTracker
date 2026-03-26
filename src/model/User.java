package model;

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

    public abstract boolean login(String password);

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
