package org.iatoki.judgels.jophiel;

import java.net.URL;
import java.util.List;

public final class User {

    private long id;

    private String jid;

    private String username;

    private String name;

    private String primaryEmail;

    private List<UserEmail> emailList;

    private URL profilePictureUrl;

    private List<String> roles;

    public User(String jid, String username, String name, URL profilePictureUrl) {
        this.jid = jid;
        this.username = username;
        this.name = name;
        this.profilePictureUrl = profilePictureUrl;
    }

    public User(long id, String jid, String username, String name, String primaryEmail, URL profilePictureUrl, List<String> roles) {
        this.id = id;
        this.jid = jid;
        this.username = username;
        this.name = name;
        this.primaryEmail = primaryEmail;
        this.profilePictureUrl = profilePictureUrl;
        this.roles = roles;
    }

    public User(long id, String jid, String username, String name, String primaryEmail, List<UserEmail> emailList, URL profilePictureUrl, List<String> roles) {
        this.id = id;
        this.jid = jid;
        this.username = username;
        this.name = name;
        this.primaryEmail = primaryEmail;
        this.emailList = emailList;
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

    public String getPrimaryEmail() {
        return primaryEmail;
    }

    public URL getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public List<String> getRoles() {
        return roles;
    }

    public List<UserEmail> getEmailList() {
        return emailList;
    }
}
