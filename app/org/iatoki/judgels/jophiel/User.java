package org.iatoki.judgels.jophiel;

public final class User {

    private long id;

    private String jid;

    private String username;

    private String name;

    private String email;

    public User(long id, String jid, String username, String name, String email) {
        this.id = id;
        this.jid = jid;
        this.username = username;
        this.name = name;
        this.email = email;
    }

    public long getId() {
        return id;
    }

    public String getJid() {
        return jid;
    }

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}
