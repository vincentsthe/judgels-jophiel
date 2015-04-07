package org.iatoki.judgels.jophiel;

import java.net.URL;
import java.util.List;

public final class User {

    private long id;

    private String jid;

    private String username;

    private String name;

    private String email;

    private URL profilePictureUrl;

    private List<String> roles;

    public User(String jid, String username, String name, URL profilePictureUrl) {
        this.jid = jid;
        this.username = username;
        this.name = name;
        this.profilePictureUrl = profilePictureUrl;
    }

    public User(long id, String jid, String username, String name, String email, URL profilePictureUrl, List<String> roles) {
        this.id = id;
        this.jid = jid;
        this.username = username;
        this.name = name;
        this.email = email;
        this.profilePictureUrl = profilePictureUrl;
        this.roles = roles;
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

    public URL getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public List<String> getRoles() {
        return roles;
    }
}
