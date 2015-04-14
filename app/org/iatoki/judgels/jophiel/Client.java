package org.iatoki.judgels.jophiel;

import java.util.List;
import java.util.Set;

public final class Client {

    private final long id;

    private final String jid;

    private final String name;

    private final String secret;

    private final String applicationType;

    private final Set<String> scopes;

    private final List<String> redirectURIs;

    public Client(long id, String jid, String name, String secret, String applicationType, Set<String> scopes, List<String> redirectURIs) {
        this.id = id;
        this.jid = jid;
        this.name = name;
        this.secret = secret;
        this.applicationType = applicationType;
        this.scopes = scopes;
        this.redirectURIs = redirectURIs;
    }

    public long getId() {
        return id;
    }

    public String getJid() {
        return jid;
    }

    public String getName() {
        return name;
    }

    public String getSecret() {
        return secret;
    }

    public String getApplicationType() {
        return applicationType;
    }

    public Set<String> getScopes() {
        return scopes;
    }

    public List<String> getRedirectURIs() {
        return redirectURIs;
    }
}
