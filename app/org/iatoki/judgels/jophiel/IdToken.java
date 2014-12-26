package org.iatoki.judgels.jophiel;

import org.iatoki.judgels.jophiel.models.domains.IdTokenModel;

public final class IdToken {

    private long id;

    private String code;

    private String userJid;

    private String clientJid;

    private String token;

    private boolean redeemed;

    public IdToken(IdTokenModel idTokenModel) {
        this.id = idTokenModel.id;
        this.code = idTokenModel.code;
        this.userJid = idTokenModel.userJid;
        this.clientJid = idTokenModel.clientJid;
        this.token = idTokenModel.token;
        this.redeemed = idTokenModel.redeemed;
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

    public boolean isRedeemed() {
        return redeemed;
    }
}
