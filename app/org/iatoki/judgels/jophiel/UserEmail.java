package org.iatoki.judgels.jophiel;

public final class UserEmail {

    private long id;

    private String email;

    private boolean emailVerified;

    private boolean primaryEmail;

    public UserEmail(long id, String email, boolean emailVerified) {
        this.id = id;
        this.email = email;
        this.emailVerified = emailVerified;
    }

    public UserEmail(long id, String email, boolean emailVerified, boolean primaryEmail) {
        this.id = id;
        this.email = email;
        this.emailVerified = emailVerified;
        this.primaryEmail = primaryEmail;
    }

    public long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public boolean isPrimaryEmail() {
        return primaryEmail;
    }

}