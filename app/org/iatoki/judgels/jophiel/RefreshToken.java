package org.iatoki.judgels.jophiel;

import org.iatoki.judgels.jophiel.models.domains.RefreshTokenModel;

public class RefreshToken {

    private long id;

    private String code;

    private String userJid;

    private String clientJid;

    private String token;

    private String scopes;

    private boolean redeemed;

    public RefreshToken(RefreshTokenModel refreshTokenModel) {
        this.id = refreshTokenModel.id;
        this.code = refreshTokenModel.code;
        this.userJid = refreshTokenModel.userJid;
        this.clientJid = refreshTokenModel.clientJid;
        this.token = refreshTokenModel.token;
        this.scopes = refreshTokenModel.scopes;
        this.redeemed = refreshTokenModel.redeemed;
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

    public String getScopes() {
        return scopes;
    }

    public boolean isRedeemed() {
        return redeemed;
    }
}
