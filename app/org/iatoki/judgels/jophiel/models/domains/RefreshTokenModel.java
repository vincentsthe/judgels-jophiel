package org.iatoki.judgels.jophiel.models.domains;

import org.iatoki.judgels.commons.models.domains.AbstractModel;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "jophiel_refresh_token")
public final class RefreshTokenModel extends AbstractModel {

    @Id
    @GeneratedValue
    public long id;

    public String code;

    public String userJid;

    public String clientJid;

    public String token;

    public boolean redeemed;

    public String scopes;

    public RefreshTokenModel() {

    }

}
