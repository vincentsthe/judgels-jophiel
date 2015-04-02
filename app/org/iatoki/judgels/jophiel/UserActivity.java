package org.iatoki.judgels.jophiel;

public final class UserActivity {

    private long id;

    private long time;

    private String userJid;

    private String clientJid;

    private String log;

    public UserActivity(long id, long time, String userJid, String clientJid, String log) {
        this.id = id;
        this.time = time;
        this.userJid = userJid;
        this.clientJid = clientJid;
        this.log = log;
    }

    public long getId() {
        return id;
    }

    public long getTime() {
        return time;
    }

    public String getUserJid() {
        return userJid;
    }

    public String getClientJid() {
        return clientJid;
    }

    public String getLog() {
        return log;
    }
}
