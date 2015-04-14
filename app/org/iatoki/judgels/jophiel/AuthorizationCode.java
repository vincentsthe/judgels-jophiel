package org.iatoki.judgels.jophiel;

import org.iatoki.judgels.commons.models.domains.AbstractModel;
import org.iatoki.judgels.jophiel.models.domains.AuthorizationCodeModel;

public final class AuthorizationCode extends AbstractModel {

    private final long id;

    private final String userJid;

    private final String clientJid;

    private final String code;

    private final String redirectURI;

    private final long expireTime;

    private final String scopes;

    public AuthorizationCode(AuthorizationCodeModel authorizationCodeModel) {
        this.id = authorizationCodeModel.id;
        this.userJid = authorizationCodeModel.userJid;
        this.clientJid = authorizationCodeModel.clientJid;
        this.code = authorizationCodeModel.code;
        this.redirectURI = authorizationCodeModel.redirectURI;
        this.expireTime = authorizationCodeModel.expireTime;
        this.scopes = authorizationCodeModel.scopes;
    }

    public long getId() {
        return id;
    }

    public String getUserJid() {
        return userJid;
    }

    public String getClientJid() {
        return clientJid;
    }

    public String getCode() {
        return code;
    }

    public String getRedirectURI() {
        return redirectURI;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public String getScopes() {
        return scopes;
    }

    public boolean isExpired() {
        return (expireTime < System.currentTimeMillis());
    }
}
