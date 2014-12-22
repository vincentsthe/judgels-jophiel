package org.iatoki.judgels.jophiel;

import org.iatoki.judgels.commons.models.domains.AbstractModel;
import org.iatoki.judgels.jophiel.models.domains.AuthorizationCodeModel;

public class AuthorizationCode extends AbstractModel {

    private long id;

    private String userJid;

    private String clientJid;

    private String code;

    private String redirectURI;

    private long expireTime;

    private String scopes;

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
