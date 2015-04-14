package org.iatoki.judgels.jophiel;

import org.iatoki.judgels.jophiel.models.domains.AccessTokenModel;

public final class AccessToken {

    private final long id;

    private final String code;

    private final String userJid;

    private final String clientJid;

    private final String token;

    private final long expireTime;

    private final boolean redeemed;

    private final String scopes;

    public AccessToken(AccessTokenModel accessTokenModel) {
        this.id = accessTokenModel.id;
        this.code = accessTokenModel.code;
        this.userJid = accessTokenModel.userJid;
        this.clientJid = accessTokenModel.clientJid;
        this.token = accessTokenModel.token;
        this.expireTime = accessTokenModel.expireTime;
        this.redeemed = accessTokenModel.redeemed;
        this.scopes = accessTokenModel.scopes;
    }

    public long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getUserJid() {
        return userJid;
    }

    public String getClientJid() {
        return clientJid;
    }

    public String getToken() {
        return token;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public boolean isRedeemed() {
        return redeemed;
    }

    public String getScopes() {
        return scopes;
    }
}
