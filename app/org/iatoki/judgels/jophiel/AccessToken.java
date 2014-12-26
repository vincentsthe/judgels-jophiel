package org.iatoki.judgels.jophiel;

import org.iatoki.judgels.jophiel.models.domains.AccessTokenModel;

public final class AccessToken {

    private long id;

    private String code;

    private String userJid;

    private String clientJid;

    private String token;

    private long expireTime;

    private boolean redeemed;

    private String scopes;

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
